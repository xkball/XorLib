package com.xkball.xorlib.api.internal;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.Enter;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;

public interface IExtendedProcessingEnv {
    
    ProcessingEnvironment getProcessingEnvironment();
    RoundEnvironment getRoundEnvironment();
    Elements getElementUtils();
    Filer getFiler();
    JavacTrees getJavacTrees();
    Symtab getSymtab();
    Enter getEnter();
    Attr getAttr();
}
