package com.xkball.xorlib.util.jctree;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.xkball.xorlib.api.annotation.NetworkPacket;
import com.xkball.xorlib.util.StringUtils;
import com.xkball.xorlib.util.Types;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.*;


public class NetworkJCTreeUtils {
    
    
    public static void addNetworkPacketType(JCTree.JCClassDecl target,String modid){
        var flag = JCTreeUtils.Modifiers.publicStaticFinal();
        var init = treeMaker.NewClass(null, List.nil(),
                treeMaker.TypeApply(makeIdent(Types.CUSTOM_PACKET_PAYLOAD_TYPE), List.nil()),
                List.of(treeMaker.Apply(List.nil(),makeIdent("net.minecraft.resources.ResourceLocation.fromNamespaceAndPath"),List.of(treeMaker.Literal(modid),treeMaker.Literal(StringUtils.toSnakeCase(target.name.toString()))))), null);
        Adder.addField2Class(target,Types.CUSTOM_PACKET_PAYLOAD_TYPE,"TYPE",flag,init);
    }
    
    public static void addNetworkPacketTypeGetter(JCTree.JCClassDecl target){
        var flag = JCTreeUtils.Modifiers.public_();
        var block = treeMaker.Block(0,List.of(treeMaker.Return(ident("TYPE"))));
        Adder.addMethod2Class(target,"type",makeIdent(Types.CUSTOM_PACKET_PAYLOAD_TYPE),flag,List.nil(),block);
    }
    
    public static void addNetworkRegListener(JCTree.JCClassDecl target, String modid, NetworkPacket.Type type, String codecName, String handleName, String handleOwnerName){
        var flag = JCTreeUtils.Modifiers.publicStatic();
        var block = treeMaker.Block(0,List.of(
                treeMaker.VarDef(noMods(),name("register"),makeIdent(Types.PAYLOAD_REGISTRAR),treeMaker.Apply(List.nil(),treeMaker.Select(ident("event_"),name("registrar")),List.of(treeMaker.Literal(modid)))),
                treeMaker.Exec(treeMaker.Apply(List.nil(),treeMaker.Select(ident("register"),name(type.getMethodName())),List.of(makeIdent(target.sym.fullname.toString()+".TYPE"),makeIdent(codecName),treeMaker.Reference(MemberReferenceTree.ReferenceMode.INVOKE,name(handleName),makeIdent(handleOwnerName),null))))
        ));
        Adder.addMethod2Class(target,"register",treeMaker.TypeIdent(TypeTag.VOID),flag,
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER),name("event_"),makeIdent(Types.REG_PAYLOAD_EVENT),null)),block);
        
    }
    
}
