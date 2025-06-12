package com.xkball.xorlib.api.internal.expr;

import com.sun.tools.javac.tree.JCTree;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

import java.util.List;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.name;

public interface IExprGetter extends IJCExpr{
    
    JCTree.JCExpression applyGet();
    
    default IExprGetter thenGet(String method) {
        return new IExprGetter() {
            @Override
            public List<String> localVariables() {
                return IExprGetter.this.localVariables();
            }
            
            @Override
            public JCTree.JCExpression applyGet() {
                var maker = JCTreeUtils.treeMaker;
                return maker.Apply(com.sun.tools.javac.util.List.nil(),maker.Select(IExprGetter.this.applyGet(),name(method)), com.sun.tools.javac.util.List.nil());
            }
        };
    }
    
    default IExprFunction thenApply(String method) {
        return new IExprFunction() {
            
            @Override
            public List<String> localVariables() {
                return IExprGetter.this.localVariables();
            }
            
            @Override
            public JCTree.JCExpression applyFunction(JCTree.JCExpression arg) {
                var maker = JCTreeUtils.treeMaker;
                return maker.Apply(com.sun.tools.javac.util.List.nil(),maker.Select(IExprGetter.this.applyGet(),name(method)), com.sun.tools.javac.util.List.of(arg));
            }
        };
    }
}
