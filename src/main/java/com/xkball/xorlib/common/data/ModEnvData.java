package com.xkball.xorlib.common.data;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.xkball.xorlib.annotation_processor.ModMetaProcessor;
import com.xkball.xorlib.api.annotation.ModMeta;
import com.xkball.xorlib.api.internal.IExtendedProcessingEnv;
import com.xkball.xorlib.api.internal.IXLAnnotationProcessor;
import com.xkball.xorlib.util.Types;
import com.xkball.xorlib.util.Utils;
import com.xkball.xorlib.util.jctree.AnnotationUtils;
import com.xkball.xorlib.util.jctree.JCTreeUtils;
import com.xkball.xorlib.util.jctree.SymbolUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record ModEnvData(String modid, String modLoader, String mcVersion, List<String> packages, List<String> useLanguages, List<IXLAnnotationProcessor> processors) {
    public static ModEnvData create(JCTree.JCClassDecl classTree, ModMetaProcessor env) {
        var annoAttrMod = Objects.requireNonNull(JCTreeUtils.Finder.findAnnotation(classTree,Types.MOD)).attribute;
        var annoAttrModMeta = Objects.requireNonNull(JCTreeUtils.Finder.findAnnotation(classTree, Types.MOD_META)).attribute;
        
        var modid = (String) Objects.requireNonNull(AnnotationUtils.getAnnotationValue(annoAttrMod,"value"));
        
        var modLoader = (String) AnnotationUtils.getAnnotationValue(annoAttrModMeta, "modLoaderName");
        if (modLoader == null) modLoader = env.getModLoader();
        
        var mcVersion = (String) AnnotationUtils.getAnnotationValue(annoAttrModMeta, "minecraftVersion");
        if (mcVersion == null) mcVersion = env.getMCVersion();
        
        List<String> packages = AnnotationUtils.getAnnotationValueAsStringList(annoAttrModMeta, "packages");
        if (packages == null) {
            packages = List.of(Objects.requireNonNull(SymbolUtils.findPackage(classTree.sym)).toString());
        }
        
        List<String> useLanguages = AnnotationUtils.getAnnotationValueAsStringList(annoAttrModMeta, "useLanguages");
        if (useLanguages == null) {
            useLanguages = List.of("en_us");
        }
        
        return new ModEnvData(modid,modLoader, mcVersion, packages, useLanguages, env.filterProcessors(modLoader, mcVersion));
    }
    
    private <T extends Element> Set<T> filterElements(Set<T> elements) {
        var result = new HashSet<T>();
        for(var element : elements) {
            if(element instanceof Symbol symbol){
                if(packages.stream().anyMatch(p -> {
                    var pkgSymbol = SymbolUtils.findPackage(symbol);
                    if(pkgSymbol == null) return false;
                    return pkgSymbol.toString().startsWith(p);
                })){
                    result.add(element);
                }
            }
        }
        return result;
    }
    
    public Set<? extends Element> getElementsAnnotatedWith(TypeElement a){
        var rawResult = ModMetaProcessor.staticEnv.getRoundEnvironment().getElementsAnnotatedWith(a);
        return filterElements(rawResult);
    }
    
    public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a){
        var rawResult = ModMetaProcessor.staticEnv.getRoundEnvironment().getElementsAnnotatedWith(a);
        return filterElements(rawResult);
    }
    
    public Set<Symbol.ClassSymbol> getAllClassSymbols(){
        var root = ModMetaProcessor.staticEnv.getRoundEnvironment().getRootElements();
        return filterElements(root.stream().filter(r -> r instanceof Symbol.ClassSymbol).map(r -> (Symbol.ClassSymbol) r).collect(Collectors.toSet()));
    }
    
    public Symbol.ClassSymbol getMainClass(){
        var rawResult = this.getElementsAnnotatedWith(ModMeta.class);
        return (Symbol.ClassSymbol) Utils.assertSingleAndGet(rawResult,"Find more than one class with @ModMeta in it's packages.");
    }
    
    public void runProcessor(IExtendedProcessingEnv env){
        ModMetaProcessor.staticModEnv = this;
        for(var processor : processors){
            processor.beforeProcess();
            processor.process(this,env);
            processor.afterProcess();
        }
    }
}
