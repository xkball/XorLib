package com.xkball.xorlib.util.jctree;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.xkball.xorlib.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationUtils {
    
    @Nullable
    public static Attribute.Compound findAnnotation(Symbol classElement, Class<?> name){
        return findAnnotation(classElement,name.getName());
    }
    
    @Nullable
    public static Attribute.Compound findAnnotation(Symbol classElement, String name){
        return classElement.getMetadata().getDeclarationAttributes().stream().filter(a -> a.type.equals(JCTreeUtils.elementUtils.getTypeElement(name).asType())).findFirst().orElse(null);
    }
    
    @Nullable
    public static Object getAnnotationValue(Attribute.Compound annotation, String name){
        return annotation.values.stream().filter(p -> name.equals(p.fst.name.toString())).findFirst().map(p -> p.snd.getValue()).orElse(null);
    }
    
    @Nullable
    @SuppressWarnings("unchecked")
    public static List<String> getAnnotationValueAsStringList(Attribute.Compound annotation, String name){
        var rawList = (com.sun.tools.javac.util.List<Attribute.Constant>) AnnotationUtils.getAnnotationValue(annotation, name);
        if(rawList == null) return null;
        return rawList.stream().map(Attribute.Constant::toString).map(StringUtils::removeDoubleQuotes).collect(Collectors.toList());
    }
    
    @Nullable
    public static <T extends Enum<T>> T getAnnotationValueAsEnum(Attribute.Compound annotation, String name, Class<T> clazz){
        return annotation.values.stream().filter(p -> name.equals(p.fst.name.toString())).findFirst().map(p -> {
            if (p.snd instanceof Attribute.Enum) {
                return Enum.valueOf(clazz, ((Symbol.VarSymbol) p.snd.getValue()).name.toString());
            }
            else throw new RuntimeException("result is not an Enum");
            
        } ).orElse(null);
    }
    
    @Nullable
    public static <T extends Enum<T>> List<T> getAnnotationValueAsEnumList(Attribute.Compound annotation, String name, Class<T> clazz){
        return annotation.values.stream().filter(p -> name.equals(p.fst.name.toString())).findFirst().map(p -> {
            if (p.snd instanceof Attribute.Array attrArray) {
                var result = new ArrayList<T>();
                for(var attr : attrArray.values){
                    assert attr instanceof Attribute.Enum;
                    result.add(Enum.valueOf(clazz,((Attribute.Enum) attr).value.name.toString()));
                }
                return result;
            }
            else throw new RuntimeException("result is not an Enum");
        } ).orElse(null);
    }
}
