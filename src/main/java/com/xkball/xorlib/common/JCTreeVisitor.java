package com.xkball.xorlib.common;

import com.sun.tools.javac.tree.JCTree;

public class JCTreeVisitor {
    
    public void visitJCTree(JCTree tree) {
        switch (tree) {
            case JCTree.JCMethodDecl methodDecl -> visitMethodDecl(methodDecl);
            case JCTree.JCClassDecl classDecl -> visitClassDecl(classDecl);
            case JCTree.JCVariableDecl variableDecl -> visitVariableDecl(variableDecl);
            case JCTree.JCFieldAccess jcFieldAccess -> visitJCFieldAccess(jcFieldAccess);
            case JCTree.JCMethodInvocation jcMethodInvocation -> visitJCMethodInvocation(jcMethodInvocation);
            case JCTree.JCBlock jcBlock -> visitJCBlock(jcBlock);
            case JCTree.JCLabeledStatement labeledStatement -> visitJCLabeledStatement(labeledStatement);
            case JCTree.JCSynchronized sync -> visitJCSync(sync);
            case JCTree.JCYield yield -> visitJCYield(yield);
            case JCTree.JCTry try_ -> visitJCTry(try_);
            case JCTree.JCWhileLoop jcWhileLoop -> visitJCWhileLoop(jcWhileLoop);
            case JCTree.JCIf jcIf -> visitJCIf(jcIf);
            case JCTree.JCForLoop jcForLoop -> visitJCForLoop(jcForLoop);
            case JCTree.JCSwitch jcSwitch -> visitJCSwitch(jcSwitch);
            case JCTree.JCAssert jcAssert -> visitJCAssert(jcAssert);
            case JCTree.JCExpressionStatement jcExpr -> visitJCExpressionStatement(jcExpr);
            case JCTree.JCCase jcCase -> visitJCCase(jcCase);
            case JCTree.JCReturn jcReturn -> visitJCReturn(jcReturn);
            case JCTree.JCEnhancedForLoop jcForEach -> visitJCForEach(jcForEach);
            case JCTree.JCDoWhileLoop jcDoWhileLoop -> visitJCDoWhile(jcDoWhileLoop);
            case JCTree.JCCatch jcCatch -> visitJCCatch(jcCatch);
            case JCTree.JCConditional jcConditional -> visitJCConditional(jcConditional);
            case JCTree.JCNewClass jcNewClass -> visitJCNewClass(jcNewClass);
            case JCTree.JCMemberReference jcMemberReference -> visitJCMemberReference(jcMemberReference);
            case JCTree.JCLambda jcLambda -> visitJCLambda(jcLambda);
            case JCTree.JCAssign jcAssign -> visitJCAssign(jcAssign);
            case JCTree.JCParens jcParens -> visitJCParens(jcParens);
            case JCTree.JCTypeCast jcTypeCast -> visitJCTypeCast(jcTypeCast);
            case JCTree.JCUnary jcUnary -> visitJCUnary(jcUnary);
            case JCTree.JCBinary jcBinary -> visitJCBinary(jcBinary);
            case JCTree.JCAssignOp jcAssignOp -> visitJCAssignOp(jcAssignOp);
            case JCTree.JCNewArray jcNewArray -> visitJCNewArray(jcNewArray);
            case JCTree.JCArrayAccess jcArrayAccess -> visitJCArrayAccess(jcArrayAccess);
            case JCTree.JCInstanceOf jcInstanceOf -> visitJCInstanceOf(jcInstanceOf);
            case null -> {}
            default -> visitDefault(tree);
        }
    }
    
    public void visitDefault(JCTree tree) {
    
    }
    
    public void visitClassDecl(JCTree.JCClassDecl classDecl) {
        for(var def : classDecl.defs) {
            visitJCTree(def);
        }
    }
    
    public void visitMethodDecl(JCTree.JCMethodDecl methodDecl) {
        visitJCTree(methodDecl.defaultValue);
        visitJCTree(methodDecl.body);
    }
    
    public void visitVariableDecl(JCTree.JCVariableDecl variableDecl) {
        visitJCTree(variableDecl.init);
    }
    
    public void visitJCBlock(JCTree.JCBlock jcBlock) {
        for(var statement : jcBlock.stats) {
            visitJCTree(statement);
        }
    }
    
    public void visitJCLabeledStatement(JCTree.JCLabeledStatement labeledStatement) {
        visitJCTree(labeledStatement.body);
    }
    
    public void visitJCSync(JCTree.JCSynchronized sync) {
        visitJCTree(sync.lock);
        visitJCTree(sync.body);
    }
    
    public void visitJCYield(JCTree.JCYield yield) {
        visitJCTree(yield.value);
    }
    
    public void visitJCTry(JCTree.JCTry jctry) {
        visitJCTree(jctry.body);
        visitJCTree(jctry.finalizer);
        for(var jcCatch : jctry.catchers) {
            visitJCTree(jcCatch);
        }
    }
    
    public void visitJCWhileLoop(JCTree.JCWhileLoop jcwhileloop) {
        visitJCTree(jcwhileloop.cond);
        visitJCTree(jcwhileloop.body);
    }
    
    public void visitJCIf(JCTree.JCIf jcif) {
        visitJCTree(jcif.cond);
        visitJCTree(jcif.thenpart);
        visitJCTree(jcif.elsepart);
    }
    
    public void visitJCForLoop(JCTree.JCForLoop jcforloop) {
        for(var stat : jcforloop.init){
            visitJCTree(stat);
        }
        visitJCTree(jcforloop.cond);
        for(var stat : jcforloop.step){
            visitJCTree(stat);
        }
        visitJCTree(jcforloop.body);
    }
    
    public void visitJCSwitch(JCTree.JCSwitch jcswitch) {
        visitJCTree(jcswitch.selector);
        for(var jcCase : jcswitch.cases) {
            visitJCTree(jcCase);
        }
    }
    
    public void visitJCAssert(JCTree.JCAssert jcAssert) {
        visitJCTree(jcAssert.cond);
        visitJCTree(jcAssert.detail);
    }
    
    public void visitJCExpressionStatement(JCTree.JCExpressionStatement jcExpressionStatement) {
        visitJCTree(jcExpressionStatement.expr);
    }
    
    public void visitJCReturn(JCTree.JCReturn jcReturn) {
        visitJCTree(jcReturn.expr);
    }
    
    public void visitJCCase(JCTree.JCCase jcCase) {
        visitJCTree(jcCase.guard);
        visitJCTree(jcCase.body);
        for(var stat : jcCase.stats) {
            visitJCTree(stat);
        }
    }
    
    public void visitJCForEach(JCTree.JCEnhancedForLoop jcForEach) {
        visitJCTree(jcForEach.var);
        visitJCTree(jcForEach.expr);
        visitJCTree(jcForEach.body);
    }
    
    public void visitJCDoWhile(JCTree.JCDoWhileLoop jcDoWhileLoop) {
        visitJCTree(jcDoWhileLoop.body);
        visitJCTree(jcDoWhileLoop.cond);
    }
    
    public void visitJCCatch(JCTree.JCCatch jccatch) {
        visitJCTree(jccatch.body);
    }
    
    public void visitJCFieldAccess(JCTree.JCFieldAccess jcFieldAccess) {
        visitJCTree(jcFieldAccess.selected);
    }
    
    public void visitJCMethodInvocation(JCTree.JCMethodInvocation jcMethodInvocation) {
        visitJCTree(jcMethodInvocation.meth);
        for(var arg : jcMethodInvocation.args) {
            visitJCTree(arg);
        }
    }
    
    public void visitJCConditional(JCTree.JCConditional jcConditional) {
        visitJCTree(jcConditional.cond);
        visitJCTree(jcConditional.truepart);
        visitJCTree(jcConditional.falsepart);
    }
    
    public void visitJCNewClass(JCTree.JCNewClass jcNewClass) {
        for(var arg : jcNewClass.args) {
            visitJCTree(arg);
        }
    }
    
    public void visitJCMemberReference(JCTree.JCMemberReference jcMemberReference) {
        visitJCTree(jcMemberReference.expr);
    }
    
    public void visitJCLambda(JCTree.JCLambda jcLambda) {
        visitJCTree(jcLambda.body);
    }
    
    public void visitJCAssign(JCTree.JCAssign jcAssign) {
        visitJCTree(jcAssign.lhs);
        visitJCTree(jcAssign.rhs);
    }
    
    public void visitJCParens(JCTree.JCParens jcParens) {
        visitJCTree(jcParens.expr);
    }
    
    public void visitJCTypeCast(JCTree.JCTypeCast jcTypeCast) {
        visitJCTree(jcTypeCast.expr);
    }
    
    public void visitJCUnary(JCTree.JCUnary jcUnary) {
        visitJCTree(jcUnary.arg);
    }
    
    public void visitJCBinary(JCTree.JCBinary jcBinary) {
        visitJCTree(jcBinary.lhs);
        visitJCTree(jcBinary.rhs);
    }
    
    public void visitJCAssignOp(JCTree.JCAssignOp jcAssignOp) {
        visitJCTree(jcAssignOp.lhs);
        visitJCTree(jcAssignOp.rhs);
    }
    
    public void visitJCNewArray(JCTree.JCNewArray jcNewArray) {
        for(var elem : jcNewArray.elems) {
            visitJCTree(elem);
        }
    }
    
    public void visitJCArrayAccess(JCTree.JCArrayAccess jcArrayAccess) {
        visitJCTree(jcArrayAccess.index);
        visitJCTree(jcArrayAccess.indexed);
    }
    
    public void visitJCInstanceOf(JCTree.JCInstanceOf jcInstanceOf) {
        visitJCTree(jcInstanceOf.expr);
    }
}
