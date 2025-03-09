package com.xkball.xorlib.batch_ap;

import com.sun.tools.javac.api.JavacTrees;
import com.xkball.xorlib.api.annotation.internal.SupportMCVersion;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Set;

public class BaseAnnoProcessor extends AbstractProcessor {
    
    protected ProcessingEnvironment processingEnv;
    protected RoundEnvironment roundEnv;
    protected Elements elementUtils;
    protected Filer filer;
    protected JavacTrees trees;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.trees = JavacTrees.instance(processingEnv);
    }
    
    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.roundEnv = roundEnv;
        var versionAP = this.getClass().getAnnotation(SupportMCVersion.class);
//        if(versionAP == null || versionAP.value().s)
        return process();
    }
    
    public boolean process(){
        return false;
    }
}
