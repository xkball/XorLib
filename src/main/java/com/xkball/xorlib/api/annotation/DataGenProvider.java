package com.xkball.xorlib.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@SuppressWarnings("unused")
/**
 * Automatically create class instances and subscribe DataGen event.
 */
public @interface DataGenProvider {
}
