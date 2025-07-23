package com.xkball.xorlib.annotation_processor.xlap;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.xkball.xorlib.XorLib;
import com.xkball.xorlib.api.annotation.ModMeta;
import com.xkball.xorlib.api.annotation.SubscribeEventEnhanced;
import com.xkball.xorlib.api.annotation.internal.SupportMCVersion;
import com.xkball.xorlib.api.internal.IExtendedProcessingEnv;
import com.xkball.xorlib.api.internal.IXLAnnotationProcessor;
import com.xkball.xorlib.common.BlockBuilder;
import com.xkball.xorlib.common.Expressions;
import com.xkball.xorlib.common.MethodBuilder;
import com.xkball.xorlib.common.data.ModEnvData;
import com.xkball.xorlib.common.data.SubscribeEventEnhancedData;
import com.xkball.xorlib.util.LogHelper;
import com.xkball.xorlib.util.jctree.AnnotationUtils;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

import java.util.function.Consumer;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.*;

@SupportMCVersion(loader = XorLib.NEO_FORGE, version = {"1.21.1","1.21.4"}, feature = ModMeta.Feature.SUBSCRIBE_EVENT_ENHANCED)
public class SubscribeEventEnhancedProcessor implements IXLAnnotationProcessor {
    
    public static final String MAIN_CLASS_HOOK_NAME = "xorLibGenerate$onModInit";
    public static final String RUN_SUBSCRIBER_NAME = "xorLibGenerate$runSubscribeEventEnhanced";
    
    @Override
    public void process(ModEnvData modEnv, IExtendedProcessingEnv processingEnv) {
        var jcTrees = processingEnv.getJavacTrees();
        var mainClassSymbol = modEnv.getMainClass();
        var listenersSymbol = modEnv.getElementsAnnotatedWith(SubscribeEventEnhanced.class);
        var maker = treeMaker;
        
        var mainClassTree = jcTrees.getTree(mainClassSymbol);
        posStack.pushPos(mainClassTree);
        
        var regMethod = MethodBuilder.builder(RUN_SUBSCRIBER_NAME)
                .flag(Modifiers.publicStatic())
                .typeArg(maker.TypeParameter(name("T"),List.of(makeIdent( "net.neoforged.bus.api.Event"))))
                .returnType(TypeTag.VOID)
                //0-client 1-server 2-both
                .addParam(TypeTag.INT,"distFlag")
                .addParam("net.neoforged.bus.api.EventPriority","priority")
                .addParam(TypeTag.BOOLEAN,"receiveCanceled")
                .addParam(java.util.List.class,String.class,"requireModLoaded")
                .addParam(Class.class,"?","eventClass")
                .addParam(Consumer.class,"T","listener")
                .addParam("net.neoforged.bus.api.IEventBus","modBus")
                .addParam("net.neoforged.bus.api.IEventBus","gameBus")
                .varDef("flag",maker.Literal(TypeTag.BOOLEAN,0))
                .exec(maker.Assignop(JCTree.Tag.BITOR_ASG,makeIdent("flag"),
                        maker.Binary(JCTree.Tag.AND,
                                maker.Binary(JCTree.Tag.LT,makeIdent("distFlag"),maker.Literal(2)),
                                maker.Binary(JCTree.Tag.EQ,
                                        Expressions.getter("net.neoforged.fml.loading.FMLLoader","getDist").thenGet("isClient").applyGet(),
                                        maker.Binary(JCTree.Tag.EQ,makeIdent("distFlag"),maker.Literal(1))))))
                .forEach("modid_",makeIdent("requireModLoaded"),BlockBuilder.of()
                        .exec(maker.Assignop(JCTree.Tag.BITOR_ASG,
                                makeIdent("flag"),
                                maker.Unary(JCTree.Tag.NOT, Expressions.getter("net.neoforged.fml.ModList","get").thenApply("isLoaded").applyFunction(makeIdent("modid_"))))))
                .if_(makeIdent("flag"), BlockBuilder.of().return_())
                .varDef("bus",makeIdent("gameBus"))
                .varDef("clazz",maker.Select(makeIdent("net.neoforged.fml.event.IModBusEvent"),name("class")))
                .if_(Expressions.apply("clazz","isAssignableFrom").applyFunction(makeIdent("eventClass")),
                        BlockBuilder.of().assign("bus",makeIdent("modBus")))
                .exec(maker.Apply(List.nil(),maker.Select(makeIdent("bus"),name("addListener")),
                        List.of(makeIdent("priority"),makeIdent("receiveCanceled"),makeIdent("listener"))));
        
        var hookMethodBuilder = MethodBuilder.builder(MAIN_CLASS_HOOK_NAME)
                        .flag(Modifiers.publicStatic())
                        .returnType(TypeTag.VOID)
                        .varDef("modBus",Expressions.getter("net.neoforged.fml.ModList","get")
                                .thenApply("getModContainerById")
                                .accept(Expressions.literal(modEnv::modid))
                                .thenGet("orElseThrow")
                                .thenGet("getEventBus"));
        
        for(var listener : listenersSymbol) {
            if(!(listener instanceof Symbol.MethodSymbol methodSymbol)) throw new RuntimeException("Symbol not a method");
            LogHelper.INSTANCE.log("Processing: " + methodSymbol.owner.flatName() + "." +methodSymbol);
            var attr = AnnotationUtils.findAnnotation(methodSymbol, SubscribeEventEnhanced.class);
            if(attr == null) throw new RuntimeException("Cannot find @SubscribeEventEnhanced annotation.");
            var data = SubscribeEventEnhancedData.create(attr);
            var eventClass = methodSymbol.params.head.type.tsym.toString();
            hookMethodBuilder.exec(maker.Apply(List.nil(),makeIdent(RUN_SUBSCRIBER_NAME),
                    List.of(maker.Literal(data.getDistFlag()),data.getEventPriority(),data.getReceiveCanceled(),
                            data.getRequireModLoaded(),maker.Select(makeIdent(eventClass),name("class")),
                            treeMaker.Reference(MemberReferenceTree.ReferenceMode.INVOKE,methodSymbol.name,makeIdent(methodSymbol.owner.flatName().toString()),null),
                            makeIdent("modBus"),maker.Select(makeIdent("net.neoforged.neoforge.common.NeoForge"),name("EVENT_BUS")))));
        }
        
        JCTreeUtils.Adder.addMethod2Class(mainClassTree,regMethod.build());
        JCTreeUtils.Adder.addMethod2Class(mainClassTree,hookMethodBuilder.build());
        JCTreeUtils.Adder.appendStatement2Method(JCTreeUtils.Finder.findSingleMethod(mainClassTree,"<init>"),
                List.of(maker.Exec(maker.Apply(List.nil(),makeIdent(MAIN_CLASS_HOOK_NAME),List.nil()))));
        posStack.popPos();
        LogHelper.INSTANCE.debug("Current Main Class: \n" + mainClassTree);
    }
}
