package com.xkball.xorlib.annotation_processor.xlap;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.xkball.xorlib.api.annotation.DataGenProvider;
import com.xkball.xorlib.api.internal.IExtendedProcessingEnv;
import com.xkball.xorlib.api.internal.IJCParamAdapter;
import com.xkball.xorlib.api.internal.IXLAnnotationProcessor;
import com.xkball.xorlib.common.LocalVarParamAdapter;
import com.xkball.xorlib.common.data.ModEnvData;
import com.xkball.xorlib.util.StringUtils;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.*;

public abstract class BaseDataGenProviderProcessor implements IXLAnnotationProcessor {
    
    public static final String LOG_HEAD = "[XorLib.DataGenProviderProcessor] ";
    
    protected static final Set<IJCParamAdapter> paramAdapters = new HashSet<>();
    
    public abstract String gatherDataEventName();
    
    public abstract JCTree.JCExpression makeAddProvider(JCTree.JCExpression providerInstance);
    
    @Override
    public void afterProcess() {
        paramAdapters.clear();
    }
    
    @Override
    public void process(ModEnvData modEnv, IExtendedProcessingEnv env) {
        ReplaceTranslatableProcessor.generateLangProviderClass();
        var jcTrees = env.getJavacTrees();
        
        Set<? extends Element> classes_ = modEnv.getElementsAnnotatedWith(DataGenProvider.class);
        java.util.List<Element> classes = new ArrayList<>(classes_);
        assert classes.stream().allMatch(element -> element.getKind().isClass());
        
        var classTrees = classes.stream().map(c -> jcTrees.getTree((Symbol.ClassSymbol)c)).toList();
        classTrees = new ArrayList<>(classTrees);
        classTrees.addAll(ReplaceTranslatableProcessor.generatedClasses);
        var firstClassTree = classTrees.getFirst();
        JCTreeUtils.setPos(firstClassTree);
        List<JCTree.JCStatement> regMethodBody = List.nil();
        boolean changed = false;
        while (!classTrees.isEmpty()){
            var left = new ArrayList<JCTree.JCClassDecl>();
            for(var classTree : classTrees) {
                //System.out.println(LOG_HEAD + "Processing " + classTree.name.toString());
                var constructors = new ArrayList<>(JCTreeUtils.Finder.findConstructors(classTree));
                constructors.sort(Comparator.comparing(m -> m.params.size()));
                var constr = constructors.stream().filter(c -> c.params.stream().allMatch(p -> paramAdapters.stream().anyMatch(adapter -> adapter.match(p)))).findFirst();
                if (constr.isEmpty()) {
                    left.add(classTree);
                }
                else {
                    changed = true;
                    regMethodBody = appendRegEventBody(regMethodBody,classTree,constr.get());
                }
            }
            if(!changed){
                throw new RuntimeException(LOG_HEAD + "No available constructor for " + left);
            }
            classTrees = left;
        }
        JCTreeUtils.Adder.addModBusSubscriber(firstClassTree,modEnv.modid());
        JCTreeUtils.Adder.addEventListener2Class(firstClassTree,gatherDataEventName(),treeMaker.Block(0,regMethodBody));
    }
    
    public List<JCTree.JCStatement> appendRegEventBody(List<JCTree.JCStatement> self, JCTree.JCClassDecl classTree, JCTree.JCMethodDecl constructor){
        List<JCTree.JCExpression> initParams = List.nil();
        var maker = treeMaker;
        for(var param : constructor.params){
            var adapter = paramAdapters.stream().filter(p -> p.match(param)).findFirst().orElseThrow();
            initParams = initParams.append(adapter.makeParam());
            increaseTreeMakerPos();
        }
        var providerName = StringUtils.toSmallCamelCase(classTree.getSimpleName().toString());
        var providerInstance = maker.NewClass(null, emptyList(),makeIdent(getClassFullName(classTree)),initParams,null);
        var statement1 = maker.VarDef(noMods(),name(providerName),null,providerInstance);
        var statement2 = maker.Exec(makeAddProvider(makeIdent(providerName)));
        paramAdapters.add(new LocalVarParamAdapter(classTree.name.toString(),"",providerName));
        return self.append(statement1).append(statement2);
    }
}
