package com.xkball.xorlib.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Need to be used with one and only one Codec and Handler annotation.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@SuppressWarnings("unused")
public @interface NetworkPacket {
    
    Type type();
    
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    @interface Codec{ }
    
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    @interface Handler{ }
    
    enum Type{
        COMMON_CLIENT_TO_SERVER("commonToServer"),
        COMMON_SERVER_TO_CLIENT("commonToClient"),
        PLAY_CLIENT_TO_SERVER("playToServer"),
        PLAY_SERVER_TO_CLIENT("playToClient");
        
        private final String methodName;
        
        Type(String methodName) {
            this.methodName = methodName;
        }
        
        public String getMethodName() {
            return methodName;
        }
    }
}
