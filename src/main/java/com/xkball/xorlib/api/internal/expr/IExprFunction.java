package com.xkball.xorlib.api.internal.expr;

import com.sun.tools.javac.tree.JCTree;
import com.xkball.xorlib.util.Utils;

import java.util.List;

public interface IExprFunction extends IJCExpr{
    
    JCTree.JCExpression applyFunction(JCTree.JCExpression arg);
    
    default IExprGetter accept(IExprGetter arg){
        return new IExprGetter() {
            
            @Override
            public List<String> localVariables() {
                return Utils.union(IExprFunction.this.localVariables(), arg.localVariables());
            }
            
            @Override
            public JCTree.JCExpression applyGet() {
                return IExprFunction.this.applyFunction(arg.applyGet());
            }
        };
    }
}
