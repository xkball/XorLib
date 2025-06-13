package com.xkball.xorlib.annotation_processor.xlap;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.xkball.xorlib.XorLib;
import com.xkball.xorlib.annotation_processor.ModMetaProcessor;
import com.xkball.xorlib.api.annotation.ModMeta;
import com.xkball.xorlib.api.annotation.internal.SupportMCVersion;
import com.xkball.xorlib.api.internal.IJCParamAdapter;
import com.xkball.xorlib.common.Expressions;
import com.xkball.xorlib.common.VarGetterParamAdapter;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

import static com.xkball.xorlib.common.VarGetterParamAdapter.ofEvent;
import static com.xkball.xorlib.util.jctree.JCTreeUtils.name;

@SupportMCVersion(loader = XorLib.NEO_FORGE, version = {"1.21.1"}, feature = ModMeta.Feature.DATA_GEN_PROVIDER)
public class DataGenProviderProcessor1211 extends BaseDataGenProviderProcessor {
    
    public static final String EVENT_NAME = "net.neoforged.neoforge.data.event.GatherDataEvent";
    
    private static final IJCParamAdapter GENERATOR_GETTER = ofEvent("net.minecraft.data.DataGenerator","getGenerator");
    
    @Override
    public void beforeProcess() {
        paramAdapters.add(ofEvent("net.neoforged.fml.ModContainer","getModContainer"));
        paramAdapters.add(GENERATOR_GETTER);
        paramAdapters.add(ofEvent("net.neoforged.neoforge.common.data.ExistingFileHelper","getExistingFileHelper"));
        paramAdapters.add(new VarGetterParamAdapter("java.util.Collection","java.nio.file.Path","event_","getInputs"));
        paramAdapters.add(new VarGetterParamAdapter("java.util.concurrent.CompletableFuture","net.minecraft.core.HolderLookup.Provider","event_","getLookupProvider"));
        paramAdapters.add(new VarGetterParamAdapter("net.minecraft.data.PackOutput",Expressions.getter("event_","getGenerator").thenGet("getPackOutput")));
        paramAdapters.add(new VarGetterParamAdapter.MatchParamNameIgnoreCase("java.lang.String","","modid", Expressions.literal(() -> ModMetaProcessor.staticModEnv.modid())));
    }
    
    @Override
    public String gatherDataEventName() {
        return EVENT_NAME;
    }
    
    @Override
    public JCTree.JCExpression makeAddProvider(JCTree.JCExpression providerInstance) {
        var maker = JCTreeUtils.treeMaker;
        return maker.Apply(List.nil(), maker.Select(GENERATOR_GETTER.makeParam(),name("addProvider")), List.of(Expressions.getter("event_","includeClient").applyGet(),providerInstance));
    }
}
