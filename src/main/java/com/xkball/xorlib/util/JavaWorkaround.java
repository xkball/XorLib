package com.xkball.xorlib.util;

import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

public class JavaWorkaround {
    
    public static final Unsafe UNSAFE = getUnsafe();
    
    @Nullable
    public static final MethodHandles.Lookup TRUSTED_LOOKUP = ThrowableSupplier.getOrNull(JavaWorkaround::getTrustedLookup);
    
    static {
        var complierModule = getComplierModule();
        exportOrOpenToUnnamed(complierModule,"com.sun.tools.javac.code", false);
        exportOrOpenToUnnamed(complierModule,"com.sun.tools.javac.api", false);
        exportOrOpenToUnnamed(complierModule,"com.sun.tools.javac.tree", false);
        exportOrOpenToUnnamed(complierModule,"com.sun.tools.javac.util", false);
        exportOrOpenToUnnamed(complierModule,"com.sun.tools.javac.model", false);
        exportOrOpenToUnnamed(complierModule,"com.sun.tools.javac.comp", false);
        exportOrOpenToUnnamed(complierModule,"com.sun.tools.javac.processing", false);
        exportOrOpenToUnnamed(complierModule,"com.sun.tools.javac.processing", true);
    }
    
    public static void init(){}
    
    public static MethodHandles.Lookup getTrustedLookupOrThrow(){
        return Objects.requireNonNull(TRUSTED_LOOKUP);
    }
    
    private static Unsafe getUnsafe() {
        for (var field : Unsafe.class.getDeclaredFields()) {
            if (field.getType().equals(Unsafe.class)) {
                field.setAccessible(true);
                try {
                    return (Unsafe) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can not get unsafe instance.", e);
                }
            }
        }
        throw new RuntimeException("Can not get unsafe instance.");
    }
    
    @SuppressWarnings("deprecation")
    //Adapted from https://github.com/Lenni0451/Reflect under MIT license.
    private static MethodHandles.Lookup getTrustedLookup() throws Throwable {
        MethodHandles.Lookup lookup;
        MethodHandles.lookup();
        
        var lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        long lookupFieldOffset = UNSAFE.staticFieldOffset(lookupField);
        lookup = (MethodHandles.Lookup) UNSAFE.getObject(UNSAFE.staticFieldBase(lookupField), lookupFieldOffset);
        if (lookup != null) return lookup;
        
        var theLookup = (MethodHandles.Lookup) ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(Class.class))
                .newInstance(MethodHandles.Lookup.class);
        return (MethodHandles.Lookup) theLookup.findStaticGetter(MethodHandles.Lookup.class, "IMPL_LOOKUP", MethodHandles.Lookup.class).invokeExact();
    }
    
    public static Object getComplierModule() {
        try {
            var moduleClass = Class.forName("java.lang.Module");
            var getModuleMethod = getTrustedLookupOrThrow().findVirtual(Class.class, "getModule", MethodType.methodType(moduleClass));
            return getModuleMethod.invoke(Class.forName("com.sun.tools.javac.Main"));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to init xor lib.",e);
        }
    }
    
    public static void exportOrOpenToUnnamed(Object module, String pn, boolean open){
        try {
            var moduleClass = Class.forName("java.lang.Module");
            var addExportsMethod = getTrustedLookupOrThrow().findVirtual(moduleClass,"implAddExportsOrOpens",MethodType.methodType(void.class,String.class, moduleClass,boolean.class,boolean.class));
            var allUnnamedModules = getTrustedLookupOrThrow().findStaticVarHandle(moduleClass,"ALL_UNNAMED_MODULE",moduleClass).get();
            addExportsMethod.invoke(module,pn,allUnnamedModules,open,true);
        }catch (Throwable e){
            throw new RuntimeException("Failed to init xor lib.",e);
        }
    }
    
    
}
