package com.xkball.xorlib.common;

import com.sun.tools.javac.tree.JCTree;
import com.xkball.xorlib.api.internal.IJCParamAdapter;
import com.xkball.xorlib.api.internal.expr.IExprGetter;
import com.xkball.xorlib.util.StringUtils;

import java.util.Locale;

public class VarGetterParamAdapter implements IJCParamAdapter {
    
    public final String typeFullName;
    public final String typeArg;
    public final IExprGetter exprGetter;
    
    public static VarGetterParamAdapter ofEvent(String typeFullName, String methodName) {
        return new VarGetterParamAdapter(typeFullName,"","event_", methodName);
    }
    
    public VarGetterParamAdapter(String typeFullName, IExprGetter exprGetter) {
        this(typeFullName,"", exprGetter);
    }
    
    public VarGetterParamAdapter(String typeFullName, String typeArg,String varName, String methodName) {
        this(typeFullName,typeArg,Expressions.getter(varName,methodName));
    }
    
    public VarGetterParamAdapter(String typeFullName, String typeArg, IExprGetter exprGetter) {
        this.typeFullName = typeFullName;
        this.typeArg = typeArg;
        this.exprGetter = exprGetter;
    }
    
    @Override
    public boolean match(JCTree.JCVariableDecl param) {
        var type = parseVarType(param);
        return typeFullName.equals(type.fst) && typeArg.equals(type.snd);
    }
    
    @Override
    public JCTree.JCExpression makeParam() {
        return exprGetter.applyGet();
    }
    
    public static class MatchParamNameIgnoreCase extends VarGetterParamAdapter {
        public final String paramName;
        
        public MatchParamNameIgnoreCase(String typeFullName, String typeArg, String paramName, IExprGetter exprGetter) {
            super(typeFullName, typeArg, exprGetter);
            this.paramName = paramName.toLowerCase(Locale.ROOT);
        }
        
        @Override
        public boolean match(JCTree.JCVariableDecl param) {
            return super.match(param) && paramName.equals(param.name.toString().toLowerCase(Locale.ROOT));
        }
    }
}
