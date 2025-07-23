package com.xkball.xorlib.annotation_processor.xlap;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.xkball.xorlib.XorLib;
import com.xkball.xorlib.annotation_processor.ModMetaProcessor;
import com.xkball.xorlib.api.annotation.ModMeta;
import com.xkball.xorlib.api.annotation.internal.SupportMCVersion;
import com.xkball.xorlib.api.internal.IExtendedProcessingEnv;
import com.xkball.xorlib.api.internal.IXLAnnotationProcessor;
import com.xkball.xorlib.common.JCTreeVisitor;
import com.xkball.xorlib.common.MethodBuilder;
import com.xkball.xorlib.common.data.ModEnvData;
import com.xkball.xorlib.util.LogHelper;
import com.xkball.xorlib.util.StringUtils;
import com.xkball.xorlib.util.jctree.ImportHelper;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.*;

@SupportMCVersion(loader = XorLib.NEO_FORGE, version = {"1.21.1","1.21.4"}, feature = ModMeta.Feature.EMBEDDED_L10N_COMPONENT)
public class ReplaceTranslatableProcessor extends JCTreeVisitor implements IXLAnnotationProcessor {
    
    public static final List<JCTree.JCClassDecl> generatedClasses = new ArrayList<>();
    private ModEnvData modEnv;
    private TreeMaker maker;
    private ImportHelper importHelper;
    private final Deque<String> identStack = new ArrayDeque<>();
    private final Deque<Integer> countStack = new ArrayDeque<>();
    private final Map<String,List<String>> i18nMap = new HashMap<>();
    
    public String nextI18nKey() {
        var sb = new StringBuilder();
        sb.append(modEnv.modid());
        sb.append(".xorlib_generate");
        for(var str : identStack.reversed()){
            sb.append('.');
            sb.append(str);
        }
        sb.append('.');
        sb.append(countStack.peek());
        countStack.push(countStack.pop() + 1);
        return sb.toString();
    }
    
    @Override
    public void visitClassDecl(JCTree.JCClassDecl classDecl) {
        identStack.push(StringUtils.toValidIdent(classDecl.name.toString()));
        countStack.push(0);
        super.visitClassDecl(classDecl);
        identStack.pop();
        countStack.pop();
    }
    
    @Override
    public void visitMethodDecl(JCTree.JCMethodDecl methodDecl) {
        identStack.push(StringUtils.toValidIdent(methodDecl.name.toString()));
        countStack.push(0);
        super.visitMethodDecl(methodDecl);
        identStack.pop();
        countStack.pop();
    }
    
    @Override
    public void visitJCMethodInvocation(JCTree.JCMethodInvocation jcMethodInvocation) {
        boolean flag = false;
        String raw = null;
        if(isXL_tr(jcMethodInvocation)) {
            raw = jcMethodInvocation.toString();
            var arg = jcMethodInvocation.args;
            if(arg.size() != modEnv.useLanguages().size()) throw new RuntimeException("Localizations count don't match the mod meta.");
            if(arg.stream().anyMatch(e -> !(e instanceof JCTree.JCLiteral))) throw new RuntimeException("Localizations must be LITERAL String.");
            var i18n = arg.stream().map(e -> (JCTree.JCLiteral)e).map(l -> l.value.toString()).toList();
            var key = nextI18nKey();
            i18nMap.put(key, i18n);
            jcMethodInvocation.meth = maker.Select(makeIdent("net.minecraft.network.chat.Component"),name("translatable"));
            jcMethodInvocation.args = com.sun.tools.javac.util.List.of(maker.Literal(key));
        }
        else if(isXL_trWithKey(jcMethodInvocation)) {
            raw = jcMethodInvocation.toString();
            var arg = jcMethodInvocation.args;
            if(arg.size() != modEnv.useLanguages().size()+1) throw new RuntimeException("Localizations count don't match the mod meta.");
            if(arg.stream().anyMatch(e -> !(e instanceof JCTree.JCLiteral))) throw new RuntimeException("Localizations must be LITERAL String.");
            var i18n = arg.stream().map(e -> (JCTree.JCLiteral)e).map(l -> l.value.toString()).toList();
            var key = i18n.getFirst();
            i18nMap.put(key, i18n.subList(1, i18n.size()));
            jcMethodInvocation.meth = maker.Select(makeIdent("net.minecraft.network.chat.Component"),name("translatable"));
            jcMethodInvocation.args = com.sun.tools.javac.util.List.of(maker.Literal(key));
        }
        if(isComponentImpl_Format(jcMethodInvocation)) {
            raw = jcMethodInvocation.toString();
            flag = true;
        }
        super.visitJCMethodInvocation(jcMethodInvocation);
        if(flag) {
            var inner = ((JCTree.JCMethodInvocation)((JCTree.JCFieldAccess) jcMethodInvocation.meth).selected);
            jcMethodInvocation.meth = inner.meth;
            jcMethodInvocation.args = inner.args.appendList(jcMethodInvocation.args);
        }
        if(raw != null) {
            LogHelper.INSTANCE.debug("Processing: \n" + raw);
            LogHelper.INSTANCE.debug("result: \n" + jcMethodInvocation);
        }
    }
    
    public boolean isXL_tr(JCTree.JCMethodInvocation jcMethodInvocation) {
        return importHelper.matchMethodInvoke(jcMethodInvocation,"com.xkball.xorlib.XL","tr");
    }
    
    public boolean isXL_trWithKey(JCTree.JCMethodInvocation jcMethodInvocation) {
        return importHelper.matchMethodInvoke(jcMethodInvocation,"com.xkball.xorlib.XL","trWithKey");
    }
    
    public boolean isComponentImpl_Format(JCTree.JCMethodInvocation jcMethodInvocation) {
        if(jcMethodInvocation.meth instanceof JCTree.JCFieldAccess jcFieldAccess) {
            if(jcFieldAccess.name.toString().equals("format")) {
                if(jcFieldAccess.selected instanceof JCTree.JCMethodInvocation jcMethodInvocation2) {
                    return isXL_tr(jcMethodInvocation2) || isXL_trWithKey(jcMethodInvocation2);
                }
            }
        }
        return false;
    }
    
    @Override
    public void afterProcess() {
        generatedClasses.clear();
    }
    
    @Override
    public void process(ModEnvData modEnv, IExtendedProcessingEnv processingEnv) {
        this.modEnv = modEnv;
        this.maker = JCTreeUtils.treeMaker;
        generateLangProviderClass();
        for(var classSymbol : modEnv.getAllClassSymbols()){
            var classTree = processingEnv.getJavacTrees().getTree(classSymbol);
            this.importHelper = new ImportHelper(processingEnv,classSymbol);
            visitJCTree(classTree);
        }
        LogHelper.INSTANCE.log(i18nMap);
        for(int i = 0; i < modEnv.useLanguages().size(); i++){
            Adder.addMethod2Class(generatedClasses.get(i),generateAddTranslationsMethod(i));
        }
    }
    
    public JCTree.JCMethodDecl generateAddTranslationsMethod(int index){
        var addTranslations = MethodBuilder.builder("addTranslations")
                .flag(JCTreeUtils.Modifiers.public_())
                .returnType(TypeTag.VOID);
        
        for(var entry : i18nMap.entrySet()){
            addTranslations.exec(maker.Apply(emptyList(),makeIdent("add"),
                    com.sun.tools.javac.util.List.of(maker.Literal(entry.getKey()),maker.Literal(entry.getValue().get(index)))));
        }
        
        return addTranslations.build();
    }
    
    public static void generateLangProviderClass(){
        if(!generatedClasses.isEmpty()) return;
        var modEnv = ModMetaProcessor.staticModEnv;
        var maker = JCTreeUtils.treeMaker;
        var mainClassSymbol = modEnv.getMainClass();
        var mainClassTree = ModMetaProcessor.staticEnv.getJavacTrees().getTree(mainClassSymbol);
        
        for(var lang : modEnv.useLanguages()){
            var constructor = MethodBuilder.builder("<init>")
                    .flag(JCTreeUtils.Modifiers.public_())
                    .addParam("net.minecraft.data.PackOutput","output")
                    .exec(maker.Apply(emptyList(),makeIdent("super"),
                            com.sun.tools.javac.util.List.of(makeIdent("output"),maker.Literal(modEnv.modid()),maker.Literal(lang))));

            var clazz = maker.ClassDef(Modifiers.publicStatic(),
                    name("XorLibGenerate$"+lang.toUpperCase(Locale.ROOT)+"Provider"),
                    emptyList(),
                    makeIdent("net.neoforged.neoforge.common.data.LanguageProvider"),
                    emptyList(),
                    com.sun.tools.javac.util.List.of(constructor.build()));
            
            Adder.addClass2Class(mainClassTree, clazz);
            generatedClasses.add(clazz);
        }
    }
}
