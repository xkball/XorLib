package com.xkball.xorlib.annotation_processor.xlap;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.xkball.xorlib.XorLib;
import com.xkball.xorlib.api.annotation.ModMeta;
import com.xkball.xorlib.api.annotation.internal.SupportMCVersion;
import com.xkball.xorlib.api.internal.IExtendedProcessingEnv;
import com.xkball.xorlib.api.internal.IXLAnnotationProcessor;
import com.xkball.xorlib.common.JCTreeVisitor;
import com.xkball.xorlib.common.data.ModEnvData;
import com.xkball.xorlib.util.LogHelper;
import com.xkball.xorlib.util.jctree.ImportHelper;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.*;

@SupportMCVersion(loader = XorLib.NEO_FORGE, version = {"1.21.1","1.21.4"}, feature = ModMeta.Feature.EMBEDDED_STATIC_LAZY)
public class StaticLazyProcessor extends JCTreeVisitor implements IXLAnnotationProcessor {
    
    private static final String LAZY_CLASS_NAME = "net.neoforged.neoforge.common.util.Lazy";
    
    private IExtendedProcessingEnv env;
    private JCTree.JCClassDecl currentClassTree;
    private ImportHelper importHelper;
    private int counter;
    
    @Override
    public void visitJCMethodInvocation(JCTree.JCMethodInvocation jcMethodInvocation) {
        super.visitJCMethodInvocation(jcMethodInvocation);
        if(importHelper.matchMethodInvoke(jcMethodInvocation,"com.xkball.xorlib.XL","staticLazy")){
            var raw = jcMethodInvocation.toString();
            var lazyName = "XORLIB_GENERATED_LAZY_" + counter;
            counter += 1;
            var attrEnv = env.getEnter().getEnv(currentClassTree.sym);
            var type = env.getAttr().attribExpr(jcMethodInvocation, attrEnv);
            createLazy(lazyName, jcMethodInvocation.args.getFirst(),type.toString());
            jcMethodInvocation.meth = treeMaker.Select(makeIdent(currentClassTree.sym.toString() + "." + lazyName),name("get"));
            jcMethodInvocation.args = List.nil();
            LogHelper.INSTANCE.debug("Processing: \n" + raw);
            LogHelper.INSTANCE.debug("result: \n" + jcMethodInvocation);
        }
    }
    
    private void createLazy(String name, JCTree.JCExpression expr, String type) {
        var init = treeMaker.Apply(List.nil(),treeMaker.Select(makeIdent(LAZY_CLASS_NAME),name("of")),
                List.of(treeMaker.Lambda(List.nil(),expr)));
        Adder.addField2Class(currentClassTree,treeMaker.TypeApply(makeIdent(LAZY_CLASS_NAME),List.of(makeIdent(type))),name,JCTreeUtils.Modifiers.privateStaticFinal(),init);
    }
    
    @Override
    public void process(ModEnvData modEnv, IExtendedProcessingEnv processingEnv) {
        this.env = processingEnv;
        
        for(var classSymbol : modEnv.getAllClassSymbols()){
            this.currentClassTree = processingEnv.getJavacTrees().getTree(classSymbol);
            this.importHelper = new ImportHelper(processingEnv,classSymbol);
            this.counter = 0;
            posStack.pushPos(currentClassTree);
            this.visitJCTree(currentClassTree);
            posStack.popPos();
        }
    }
    
}
