package com.xkball.xorlib.common.data;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.xkball.xorlib.api.annotation.SubscribeEventEnhanced;
import com.xkball.xorlib.util.jctree.AnnotationUtils;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

import java.util.List;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.*;

public record SubscribeEventEnhancedData(List<SubscribeEventEnhanced.Dist> dist, SubscribeEventEnhanced.EventPriority priority, boolean receiveCanceled, List<String> requireModLoaded) {
    
    public static SubscribeEventEnhancedData create(Attribute.Compound attr){
        var dist = AnnotationUtils.getAnnotationValueAsEnumList(attr,"value",SubscribeEventEnhanced.Dist.class);
        var priority = AnnotationUtils.getAnnotationValueAsEnum(attr,"priority",SubscribeEventEnhanced.EventPriority.class);
        Boolean receiveCanceled = (Boolean) AnnotationUtils.getAnnotationValue(attr,"receiveCanceled");
        List<String> requireModLoaded = AnnotationUtils.getAnnotationValueAsStringList(attr,"requireModLoaded");
        
        if(dist == null) dist = List.of(SubscribeEventEnhanced.Dist.CLIENT, SubscribeEventEnhanced.Dist.DEDICATED_SERVER);
        if(priority == null) priority = SubscribeEventEnhanced.EventPriority.NORMAL;
        if(receiveCanceled == null) receiveCanceled = Boolean.FALSE;
        if(requireModLoaded == null) requireModLoaded = List.of();
        return new SubscribeEventEnhancedData(dist, priority, receiveCanceled, requireModLoaded);
    }
    
    public int getDistFlag(){
        if(dist.size() == 2) return 2;
        if(dist.contains(SubscribeEventEnhanced.Dist.DEDICATED_SERVER)) return 1;
        return 0;
    }
    
    public JCTree.JCExpression getEventPriority(){
        return treeMaker.Select(JCTreeUtils.makeIdent("net.neoforged.bus.api.EventPriority"),JCTreeUtils.name(priority.name()));
    }
    
    public JCTree.JCExpression getReceiveCanceled(){
        return treeMaker.Literal(TypeTag.BOOLEAN,receiveCanceled ? 1 : 0);
    }
    
    public JCTree.JCExpression getRequireModLoaded(){
        return treeMaker.Apply(com.sun.tools.javac.util.List.nil(),treeMaker.Select(makeIdent("java.util.List"),name("of")),
                com.sun.tools.javac.util.List.from(requireModLoaded.stream().map(treeMaker::Literal).toList()));
    }
}
