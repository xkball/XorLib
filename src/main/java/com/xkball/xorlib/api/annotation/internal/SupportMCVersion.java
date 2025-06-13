package com.xkball.xorlib.api.annotation.internal;

import com.xkball.xorlib.api.annotation.ModMeta;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@ApiStatus.Internal
public @interface SupportMCVersion {
    String loader();
    String[] version();
    ModMeta.Feature feature();
}
