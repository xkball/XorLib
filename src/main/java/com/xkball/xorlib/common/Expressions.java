package com.xkball.xorlib.common;

import com.sun.tools.javac.tree.JCTree;
import com.xkball.xorlib.api.internal.expr.IExprFunction;
import com.xkball.xorlib.api.internal.expr.IExprGetter;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

import java.util.List;
import java.util.function.Supplier;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.*;

public class Expressions {
    
    public static IExprGetter getter(String var,String method) {
        return new IExprGetter() {
            @Override
            public List<String> localVariables() {
                return List.of(var);
            }
            
            @Override
            public JCTree.JCExpression applyGet() {
                var maker = JCTreeUtils.treeMaker;
                return maker.Apply(com.sun.tools.javac.util.List.nil(),maker.Select(makeIdent(var),name(method)), com.sun.tools.javac.util.List.nil());
            }
        };
    }
    
    public static IExprGetter select(String clazz, String field) {
        return new IExprGetter() {
            @Override
            public List<String> localVariables() {
                return List.of();
            }
            
            @Override
            public JCTree.JCExpression applyGet() {
                var maker = JCTreeUtils.treeMaker;
                return maker.Select(makeIdent(clazz),name(field));
            }
        };
    }
    
    public static IExprGetter literal(Supplier<String> str){
        return new IExprGetter() {
            @Override
            public List<String> localVariables() {
                return List.of();
            }
            
            @Override
            public JCTree.JCExpression applyGet() {
                var maker = JCTreeUtils.treeMaker;
                return maker.Literal(str.get());
            }
        };
    }
    
    public static IExprFunction apply(String var, String method) {
        return new IExprFunction() {
            @Override
            public List<String> localVariables() {
                return List.of(var);
            }
            
            @Override
            public JCTree.JCExpression applyFunction(JCTree.JCExpression arg) {
                var maker = JCTreeUtils.treeMaker;
                return maker.Apply(com.sun.tools.javac.util.List.nil(),maker.Select(ident(var),name(method)), com.sun.tools.javac.util.List.of(arg));
            }
        };
    }
}
