package com.xkball.xorlib.common;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.xkball.xorlib.api.internal.IBlockSupplier;
import com.xkball.xorlib.api.internal.expr.IExprGetter;

import java.util.ArrayList;
import java.util.List;

import static com.xkball.xorlib.util.jctree.JCTreeUtils.*;

@SuppressWarnings("unchecked")
public class BlockBuilder<T extends BlockBuilder<T>> implements IBlockSupplier {
    
    public final List<JCTree.JCStatement> statements = new ArrayList<>();
    protected final TreeMaker maker;
    
    protected BlockBuilder(){
        this.maker = treeMaker;
    }
    
    public static BlockBuilderImpl of(){
        return new BlockBuilderImpl();
    }
    
    public T varDef(String varName, String typeArg, IExprGetter varDef) {
        statements.add(maker.VarDef(noMods(),name(varName),makeIdent(typeArg),varDef.applyGet()));
        return (T) this;
    }
    
    public T varDef(String varName, JCTree.JCExpression varDef) {
        statements.add(maker.VarDef(noMods(),name(varName),null,varDef));
        return (T) this;
    }
    
    public T varDef(String varName, IExprGetter varDef) {
        statements.add(maker.VarDef(noMods(),name(varName),null,varDef.applyGet()));
        return (T) this;
    }
    
    public T assign(String varName, JCTree.JCExpression varDef) {
        statements.add(maker.Exec(maker.Assign(makeIdent(varName),varDef)));
        return (T) this;
    }
    
    public T assign(String varName, IExprGetter varDef) {
        statements.add(maker.Exec(maker.Assign(makeIdent(varName),varDef.applyGet())));
        return (T) this;
    }
    
    public T exec(IExprGetter statement){
        statements.add(maker.Exec(statement.applyGet()));
        return (T) this;
    }
    
    public T exec(JCTree.JCExpression statement){
        statements.add(maker.Exec(statement));
        return (T) this;
    }
    
    public T if_(JCTree.JCExpression condition, JCTree.JCBlock then){
        statements.add(maker.If(condition,then,null));
        return (T) this;
    }
    
    public T if_(JCTree.JCExpression condition, JCTree.JCBlock then, JCTree.JCBlock else_){
        statements.add(maker.If(condition,then,else_));
        return (T) this;
    }
    
    public T if_(JCTree.JCExpression condition, IBlockSupplier then){
        statements.add(maker.If(condition,then.getAsBlock(),null));
        return (T) this;
    }
    
    public T if_(JCTree.JCExpression condition, IBlockSupplier then, IBlockSupplier else_){
        statements.add(maker.If(condition,then.getAsBlock(),else_.getAsBlock()));
        return (T) this;
    }
    
    public T forEach(String varName, JCTree.JCExpression iterable, JCTree.JCBlock block){
        statements.add(maker.ForeachLoop(maker.VarDef(noMods(),name(varName),null,null),iterable,block));
        return (T) this;
    }
    
    public T forEach(String varName, JCTree.JCExpression iterable, IBlockSupplier block){
        statements.add(maker.ForeachLoop(maker.VarDef(noMods(),name(varName),null,null),iterable,block.getAsBlock()));
        return (T) this;
    }
    
    public T return_(){
        statements.add(maker.Return(null));
        return (T) this;
    }
    
    public T return_(String varName){
        statements.add(maker.Return(makeIdent(varName)));
        return (T) this;
    }
    
    public T return_(JCTree.JCExpression return_){
        statements.add(maker.Return(return_));
        return (T) this;
    }
    
    public T try_(JCTree.JCBlock block, List<JCTree.JCCatch> catchers){
        statements.add(maker.Try(block, com.sun.tools.javac.util.List.from(catchers),null));
        return (T) this;
    }
    
    @Override
    public JCTree.JCBlock getAsBlock() {
        return maker.Block(0, com.sun.tools.javac.util.List.from(statements));
    }
    
    public static class BlockBuilderImpl extends BlockBuilder<BlockBuilderImpl>{}
}
