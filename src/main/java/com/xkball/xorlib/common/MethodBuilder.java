package com.xkball.xorlib.common;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.xkball.xorlib.util.jctree.JCTreeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.*;

public class MethodBuilder extends BlockBuilder<MethodBuilder> {
    
    protected final List<Supplier<JCTree.JCVariableDecl>> params = new ArrayList<>();
    protected final List<JCTree.JCTypeParameter> typeArgs = new ArrayList<>();
    protected String name;
    protected JCTree.JCExpression returnType;
    protected JCTree.JCModifiers flag;
    
    public static MethodBuilder builder(String name){
        return new MethodBuilder().name(name);
    }
    
    public MethodBuilder name(String name){
        this.name = name;
        return this;
    }
    
    public MethodBuilder returnType(String returnType){
        this.returnType = makeIdent(returnType);
        return this;
    }
    
    public MethodBuilder returnType(TypeTag returnType){
        this.returnType = maker.TypeIdent(returnType);
        return this;
    }
    
    public MethodBuilder flag(JCTree.JCModifiers flag){
        this.flag = flag;
        return this;
    }
    
    public MethodBuilder typeArg(JCTree.JCTypeParameter typeParameter){
        this.typeArgs.add(typeParameter);
        return this;
    }
    
    public MethodBuilder addParam(TypeTag typeTag, String varName){
        this.params.add(() -> maker.VarDef(JCTreeUtils.Modifiers.param(), JCTreeUtils.name(varName), maker.TypeIdent(typeTag), null));
        return this;
    }
    
    public MethodBuilder addParam(Class<?> clazz, String varName){
        this.params.add(() -> new Parameter(clazz.getName(),"",varName).makeParam());
        return this;
    }
    
    public MethodBuilder addParam(Class<?> clazz, String typeArg, String varName){
        this.params.add(() -> new Parameter(clazz.getName(),typeArg,varName).makeParam());
        return this;
    }
    
    public MethodBuilder addParam(Class<?> clazz, Class<?> typeArg, String varName){
        this.params.add(() -> new Parameter(clazz.getName(),typeArg.getName(),varName).makeParam());
        return this;
    }
    
    public MethodBuilder addParam(String typeFullName, String varName) {
        this.params.add(() -> new Parameter(typeFullName,"", varName).makeParam());
        return this;
    }
    
    public MethodBuilder addParam(String typeFullName, String typeArg, String varName) {
        this.params.add(() -> new Parameter(typeFullName, typeArg, varName).makeParam());
        return this;
    }
    
    public JCTree.JCMethodDecl build() {
        com.sun.tools.javac.util.List<JCTree.JCVariableDecl> pList = com.sun.tools.javac.util.List.nil();
        for (var param : params) {
            pList = pList.append(param.get());
            posStack.iinc();
        }
        return maker.MethodDef(flag, JCTreeUtils.name(name),returnType, com.sun.tools.javac.util.List.from(typeArgs), pList, com.sun.tools.javac.util.List.nil(),getAsBlock(),null);
    }
    
    public record Parameter(String typeFullName, String typeArg, String varName){
        public JCTree.JCVariableDecl makeParam(){
            if(typeArg.isEmpty()){
                return treeMaker.VarDef(JCTreeUtils.Modifiers.param(), JCTreeUtils.name(varName), makeIdent(typeFullName), null);
            }
            else{
                return treeMaker.VarDef(JCTreeUtils.Modifiers.param(), JCTreeUtils.name(varName), treeMaker.TypeApply(makeIdent(typeFullName), com.sun.tools.javac.util.List.of(makeVarType())),null);
            }
        }
        
        public JCTree.JCExpression makeVarType(){
            if(typeArg.startsWith("?")){
                BoundKind boundKind;
                if(typeArg.startsWith("? extends ")) boundKind = BoundKind.EXTENDS;
                else if(typeArg.startsWith("? super ")) boundKind = BoundKind.SUPER;
                else boundKind = BoundKind.UNBOUND;
                var typeArg_ = typeArg.substring(boundKind.toString().length());
                return treeMaker.Wildcard(treeMaker.TypeBoundKind(boundKind),typeArg_.isEmpty() ? null : makeIdent(typeArg_));
            }
            else{
                return makeIdent(typeArg);
            }
        }
    }
}
