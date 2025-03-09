package com.xkball.xorlib.batch_ap;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.List;
import com.xkball.xorlib.XorLib;
import com.xkball.xorlib.api.annotation.NetworkPacket;
import com.xkball.xorlib.api.annotation.internal.SupportMCVersion;
import com.xkball.xorlib.util.AnnotationUtils;
import com.xkball.xorlib.util.Types;
import com.xkball.xorlib.util.jctree.JCTreeUtils;
import com.xkball.xorlib.util.jctree.NetworkJCTreeUtils;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.isPublicStatic;


@SupportMCVersion(loader = XorLib.NEO_FORGE, version = {"1.21.1","1.21.4"})
public class NetworkPackProcessor extends BaseAnnoProcessor {
    
    public static final String NETWORK_PACK = "com.xkball.xorlib.api.annotation.NetworkPacket";
    public static final String CODEC = "com.xkball.xorlib.api.annotation.NetworkPacket.Codec";
    public static final String HANDLER = "com.xkball.xorlib.api.annotation.NetworkPacket.Handler";
    public static final String LOG_HEAD = "[XorLib.NetworkPackProcessor] ";
    
    @Override
    public boolean process() {
        JCTreeUtils.setup(processingEnv);
        var usingClasses = roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(NETWORK_PACK));
        var usingHandlers = roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(HANDLER));
        var usingCodec = roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(CODEC));
        assert usingClasses.stream().allMatch(element -> element.getKind().isClass());
        assert usingHandlers.stream().allMatch(element -> element.getKind().isExecutable());
        assert usingCodec.stream().allMatch(element -> element.getKind().isField());
        for (var currentClass : usingClasses) {
            var classElement = (Symbol.ClassSymbol)currentClass;
            var fullClassName = classElement.fullname.toString();
            
            System.out.println(LOG_HEAD + "Processing " + fullClassName);
            

            var codec = (Symbol.VarSymbol)usingCodec.stream().filter(symbol -> ((Symbol.VarSymbol)symbol).owner.equals(currentClass)).findFirst().orElseThrow();
            var handler = (Symbol.MethodSymbol)usingHandlers.stream().filter(symbol -> ((Symbol.MethodSymbol)symbol).owner.equals(currentClass)).findFirst().orElseThrow();
            assert isPublicStatic(codec.flags());
            
            var netPackAnnotation = AnnotationUtils.findAnnotation(classElement,NETWORK_PACK);
            var modid = (String) AnnotationUtils.getAnnotationValue(netPackAnnotation,"modid");
            var type = (NetworkPacket.Type) AnnotationUtils.getEnumAnnotationValue(netPackAnnotation,"type",NetworkPacket.Type.class);
            
            var classTree = trees.getTree(classElement);
            JCTreeUtils.setPos(classTree);
            JCTreeUtils.addModBusSubscriber(classTree,modid);
            NetworkJCTreeUtils.addNetworkPacketType(classTree,modid);
            NetworkJCTreeUtils.addNetworkPacketTypeGetter(classTree);
            JCTreeUtils.addImplement2Class(classTree, Types.CUSTOM_PACKET_PAYLOAD);
            NetworkJCTreeUtils.addNetworkRegListener(classTree,modid,type,codec.owner.flatName()+"."+codec.flatName(),handler.name.toString(),handler.owner.flatName().toString());
            JCTreeUtils.addAnnotation2Method(JCTreeUtils.findMethod(classTree,"register"),Types.SUBSCRIBE_EVENT, List.nil());
            
        }
        return false;
    }
    


}
