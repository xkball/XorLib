package com.xkball.xorlib.util;

import com.sun.tools.javac.util.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringUtils {
    
    public static String toSmallCamelCase(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
    
    public static String toSnakeCase(String str) {
        var builder = new StringBuilder();
        for(var c : str.toCharArray()) {
            if(Character.isUpperCase(c) && !builder.isEmpty()) {
                builder.append('_');
            }
            builder.append(Character.toLowerCase(c));
        }
        return builder.toString();
    }
    
    //类似 Pair<String,String> 的名称
    public static Pair<String,String> splitTypeNameWithArg(String typeName) {
        if(!typeName.contains("<")) return Pair.of(typeName, "");
        var index = typeName.indexOf('<');
        return Pair.of(typeName.substring(0,index),typeName.substring(index+1,typeName.length()-1));
    }
    
    public static String removeDoubleQuotes(String str) {
        if(str.startsWith("\"") && str.endsWith("\"")) return str.substring(1, str.length()-1);
        return str;
    }
    
    public static String removeAnnotation(String str){
        while (str.contains("@")){
            var startIndex = str.indexOf("@");
            var temp = str.substring(startIndex);
            var endIndex = temp.indexOf(" ");
            str = str.substring(0,startIndex) + str.substring(endIndex);
        }
        return str;
    }
    
    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "true", "false", "null"
    ));
    
    public static String toValidIdent(String input) {
        if (input == null || input.isEmpty()) {
            return "_";
        }
        
        StringBuilder sb = new StringBuilder();
        
        char firstChar = input.charAt(0);
        if (Character.isJavaIdentifierStart(firstChar)) {
            sb.append(firstChar);
        } else {
            sb.append('_');
        }
        
        for (int i = 1; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        
        String result = sb.toString();
        
        // 检查是否为关键字
        if (JAVA_KEYWORDS.contains(result)) {
            result += "_";
        }
        
        return result;
    }
    
//    public static String fixTypeNameWithArgRecursive(String className,String typeName) {
//        typeName = removeAnnotation(typeName);
//        var pair = splitTypeNameWithArg(typeName);
//        var fstName = fixTypeNameWithWildcard(className,pair.fst);
//        if(pair.snd.isEmpty()) return fstName;
//        return fstName + "<" + fixTypeNameWithArgRecursive(className,pair.snd) + ">";
//    }
//
//    public static String fixTypeNameWithWildcard(String className,String typeName) {
//        var fst = "";
//        var snd = typeName;
//        if(typeName.contains("extends")){
//            var sa = typeName.split("extends");
//            fst = sa[0] + " extends ";
//            snd = sa[1];
//        }
//        else if(typeName.contains("super")){
//            var sa = typeName.split("super");
//            fst = sa[0] + " super ";
//            snd = sa[1];
//        }
//        return fst + CodecManager.FIXER.fixTypeName(className,snd.trim());
//    }
}
