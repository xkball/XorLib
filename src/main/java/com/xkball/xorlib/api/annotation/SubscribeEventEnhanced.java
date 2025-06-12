package com.xkball.xorlib.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
@SuppressWarnings("unused")
/**
 * Only support static method. Don't need annotated with @EventBusSubscriber.
 */
public @interface SubscribeEventEnhanced {
    
    Dist[] value() default { Dist.CLIENT, Dist.DEDICATED_SERVER };
    
    EventPriority priority() default EventPriority.NORMAL;
    
    boolean receiveCanceled() default false;
    
    String[] requireModLoaded() default {};
    
    enum Dist{
        CLIENT,
        DEDICATED_SERVER
    }
    
    enum EventPriority{
        HIGHEST, //First to execute
        HIGH,
        NORMAL,
        LOW,
        LOWEST  //Last to execute
    }
    
}
