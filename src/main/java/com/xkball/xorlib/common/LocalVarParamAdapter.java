package com.xkball.xorlib.common;

import com.sun.tools.javac.tree.JCTree;
import com.xkball.xorlib.api.internal.IJCParamAdapter;
import com.xkball.xorlib.util.StringUtils;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

public record LocalVarParamAdapter(String typeFullName, String typeArg, String varName) implements IJCParamAdapter {
    
    @Override
    public boolean match(JCTree.JCVariableDecl param) {
        var type = parseVarType(param);
        return typeFullName.equals(type.fst) && typeArg.equals(type.snd);
    }
    
    @Override
    public JCTree.JCExpression makeParam() {
        return JCTreeUtils.ident(varName);
    }
}
