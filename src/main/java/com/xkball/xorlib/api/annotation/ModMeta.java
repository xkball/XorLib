package com.xkball.xorlib.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Some metadata of a mod and some configuration of XorLib annotation processor.
 * <p>
 * The annotation should apply to EVERY class with {@code net.neoforged.fml.common.Mod}.Any missing match of these two annotation will cause compilation exception.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@SuppressWarnings("unused")
public @interface ModMeta {
    /**
     * The name of mod loader. Keep empty for auto estimate. Should like "NeoForge" (And only NeoForge now) .
     * @return mod lader name.
     */
    String modLoaderName() default "";
    
    /**
     * Minecraft Version. Keep empty for auto estimate. Should like "1.21.1".
     * @return minecraft version.
     */
    String minecraftVersion() default "";
    
    /**
     * Packages of your mod. Keep empty for auto estimate.
     * <p>
     * Notes:
     * <ul>
     *  <li> Recursively contains all subpackages. Means contains {@code foo.bar.baz} if annotation values have {@code foo.bar}.
     *  <li> Use the package of class that applied this annotation as default. Means the value is {@code ["foo.bar"]} if you use this annotation on {@code foo.bar.YourMod}.
     * </ul>
     * @return mod packages.
     */
    String[] packages() default {};
    
    /**
     * Languages used in localization.
     * @see com.xkball.xorlib.XL#tr(String...)
     * @return mod localization languages.
     */
    String[] useLanguages() default {"en_us"};
    
    Feature[] disabledFeatures() default {};
    
    enum Feature{
        NETWORK_PACKET,
        SUBSCRIBE_EVENT_ENHANCED,
        DATA_GEN_PROVIDER,
        EMBEDDED_L10N_COMPONENT
    }
    
}
