package com.xkball.xorlib;

import com.xkball.xorlib.util.ComponentImpl;

@SuppressWarnings("unused")
public class XL {
    
    public static ComponentImpl tr(String... i18n){
        return new ComponentImpl();
    }
    
    public static ComponentImpl trWithKey(String key, String ... i18n){
        return new ComponentImpl();
    }
    
    public static <T> T staticLazy(T obj){
        return obj;
    }
}
