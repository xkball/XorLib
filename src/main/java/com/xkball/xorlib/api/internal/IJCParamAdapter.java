package com.xkball.xorlib.api.internal;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Pair;
import com.xkball.xorlib.util.StringUtils;

public interface IJCParamAdapter {

    boolean match(JCTree.JCVariableDecl param);
    
    JCTree.JCExpression makeParam();
    
    default Pair<String,String> parseVarType(JCTree.JCVariableDecl param) {
        return StringUtils.splitTypeNameWithArg(param.vartype.type == null ? param.vartype.toString() : param.vartype.type.toString());
    }
}
