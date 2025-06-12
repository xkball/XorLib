package com.xkball.xorlib.annotation_processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.util.Pair;
import com.xkball.xorlib.api.annotation.ModMeta;
import com.xkball.xorlib.api.annotation.internal.AnnotationProcessor;
import com.xkball.xorlib.api.annotation.internal.SupportMCVersion;
import com.xkball.xorlib.api.internal.IXLAnnotationProcessor;
import com.xkball.xorlib.api.internal.IExtendedProcessingEnv;
import com.xkball.xorlib.common.data.ModEnvData;
import com.xkball.xorlib.common.data.APVersionData;
import com.xkball.xorlib.util.FileUtils;
import com.xkball.xorlib.util.JavaWorkaround;
import com.xkball.xorlib.util.Types;
import com.xkball.xorlib.util.Utils;
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AnnotationProcessor
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes({ModMetaProcessor.MOD_META})
public class ModMetaProcessor extends AbstractProcessor implements IExtendedProcessingEnv {
    
    static {
        JavaWorkaround.init();
    }
    
    public static final String MOD_META = "com.xkball.xorlib.api.annotation.ModMeta";
    
    public static IExtendedProcessingEnv staticEnv = null;
    public static ModEnvData staticModEnv = null;
    
    protected ProcessingEnvironment processingEnv;
    protected RoundEnvironment roundEnv;
    protected Elements elementUtils;
    protected Filer filer;
    protected JavacTrees trees;
    protected Symtab symtab;
    protected Enter enter;
    
    protected final List<Pair<IXLAnnotationProcessor, APVersionData>> annoProcessors = new ArrayList<>();
    protected final List<ModEnvData> modEnvData = new ArrayList<>();
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.trees = JavacTrees.instance(processingEnv);
        this.symtab = Symtab.instance(JCTreeUtils.getContext(processingEnv));
        this.enter = Enter.instance(JCTreeUtils.getContext(processingEnv));
        JCTreeUtils.setup(processingEnv);
    }
    
    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(roundEnv.processingOver()) return false;
        this.roundEnv = roundEnv;
        this.loadXLAnnoProcessors();
        this.setupBatchProcessorEnv();
        staticEnv = this;
        modEnvData.forEach(e -> e.runProcessor(this));
        return true;
    }
    
    public void loadXLAnnoProcessors() {
        this.annoProcessors.clear();
        var apClasses = FileUtils.readResourcesAllLines(SupportMCVersionProcessor.PATH);
        for(var className : apClasses) {
            try {
                var clazz = Class.forName(className);
                var anno = clazz.getAnnotation(SupportMCVersion.class);
                if(anno == null){
                    throw new RuntimeException("@SupportMCVersion annotation not found");
                }
                var ap = (IXLAnnotationProcessor)clazz.getDeclaredConstructor().newInstance();
                var versionData = new APVersionData(anno.loader(), List.of(anno.version()));
                annoProcessors.add(new Pair<>(ap, versionData));
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public List<IXLAnnotationProcessor> filterProcessors(String modLoader, String mcVersion) {
        return annoProcessors.stream()
                .filter(p -> p.snd.loader().equals(modLoader) && p.snd.versions().contains(mcVersion))
                .map(p -> p.fst).toList();
    }
    
    public void setupBatchProcessorEnv(){
        this.modEnvData.clear();
        var classWithModMeta = this.roundEnv.getElementsAnnotatedWith(ModMeta.class);
        var classWithMod = this.roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(Types.MOD));
        if(!classWithMod.equals(classWithModMeta)){
            throw new RuntimeException("@Mod must annotated with @ModMeta!");
        }
        for(var element : classWithModMeta){
            if(!(element instanceof Symbol.ClassSymbol clazz)){
                throw new RuntimeException("Annotated with not a class!");
            }
            var classTree = trees.getTree(clazz);
            modEnvData.add(ModEnvData.create(classTree,this));
        }
    }
    
    public String getModLoader(){
        var neoForge = Utils.iterableToList(symtab.getClassesForName(JCTreeUtils.name("net.neoforged.neoforge.common.NeoForge")));
        if(!neoForge.isEmpty()){
            return "NeoForge";
        }
        return "unknown";
    }
    
    public String getMCVersion(){
        var sharedConstants = Utils.assertSingleAndGet(
                symtab.getClassesForName(JCTreeUtils.name("net.minecraft.SharedConstants")),
                "Can't find net.minecraft.SharedConstants. This should never happen.");
        var members = sharedConstants.members();
        var version = Utils.assertSingleAndGet(
                members.getSymbolsByName(JCTreeUtils.name("VERSION_STRING")),
                "Can't find VERSION_STRING. This should never happen.");
        if(version instanceof Symbol.VarSymbol varSymbol){
            return varSymbol.getConstantValue().toString();
        }
        return "unknown";
    }
    
    @Override
    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnv;
    }
    
    @Override
    public RoundEnvironment getRoundEnvironment() {
        return roundEnv;
    }
    
    @Override
    public Elements getElementUtils() {
        return elementUtils;
    }
    
    @Override
    public Filer getFiler() {
        return filer;
    }
    
    @Override
    public JavacTrees getJavacTrees() {
        return trees;
    }
    
    @Override
    public Symtab getSymtab() {
        return symtab;
    }
    
    @Override
    public Enter getEnter() {
        return enter;
    }
    
}
