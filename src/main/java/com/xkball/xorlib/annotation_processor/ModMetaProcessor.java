package com.xkball.xorlib.annotation_processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.comp.Enter;
import com.xkball.xorlib.XorLib;
import com.xkball.xorlib.api.annotation.internal.AnnotationProcessor;
import com.xkball.xorlib.api.annotation.internal.SupportMCVersion;
import com.xkball.xorlib.util.FileClassSearcher;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Set;

@AnnotationProcessor
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes({ModMetaProcessor.MOD_META})
public class ModMetaProcessor extends AbstractProcessor {
    
    public static final String MOD_META = "com.xkball.xorlib.api.annotation.ModMeta";
    
    protected ProcessingEnvironment processingEnv;
    protected RoundEnvironment roundEnv;
    protected Elements elementUtils;
    protected Filer filer;
    protected JavacTrees trees;
    protected Symtab symtab;
    protected Enter enter;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.trees = JavacTrees.instance(processingEnv);
        this.symtab = Symtab.instance(JCTreeUtils.getContext(processingEnv));
        this.enter = Enter.instance(JCTreeUtils.getContext(processingEnv));
        
    }
    
    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.roundEnv = roundEnv;
        
        return false;
    }
    
    
}
