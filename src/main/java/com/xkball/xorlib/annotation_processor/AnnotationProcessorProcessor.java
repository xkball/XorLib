package com.xkball.xorlib.annotation_processor;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacFiler;
import com.xkball.xorlib.api.annotation.internal.AnnotationProcessor;
import com.xkball.xorlib.util.JavaWorkaround;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Set;

@AnnotationProcessor
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes({AnnotationProcessorProcessor.ANNOTATION_PROCESSOR})
public class AnnotationProcessorProcessor extends AbstractProcessor {
    
    static {
        JavaWorkaround.init();
    }
    
    public static final String ANNOTATION_PROCESSOR = "com.xkball.xorlib.api.annotation.internal.AnnotationProcessor";
    private static final String PATH = "META-INF/services/javax.annotation.processing.Processor";
    private Filer filer;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) return false;
        var classes = roundEnv.getElementsAnnotatedWith(AnnotationProcessor.class);
        try {
            var file = filer.getResource(StandardLocation.CLASS_OUTPUT,"",PATH);
            file.delete();
            file = filer.createResource(StandardLocation.CLASS_OUTPUT,"",PATH);
            try (var writer = file.openWriter()) {
                for (var clazz : classes) {
                    if(clazz instanceof Symbol.ClassSymbol classSymbol){
                        writer.write(classSymbol.getQualifiedName().toString()+'\n');
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return true;
    }
}
