package com.xkball.xorlib.api.internal;

import com.xkball.xorlib.common.data.ModEnvData;

public interface IXLAnnotationProcessor {
    
    default void beforeProcess() {}
    
    default void afterProcess() {}
    
    void process(ModEnvData modEnv, IExtendedProcessingEnv processingEnv);
}
