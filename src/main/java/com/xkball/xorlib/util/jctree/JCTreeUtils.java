package com.xkball.xorlib.util.jctree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.xkball.xorlib.util.Types;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class JCTreeUtils {
    
    public static JavacTrees trees;
    public static Elements elementUtils;
    public static TreeMaker treeMaker;
    public static Names names;
    public static PosStack posStack;
    
    public static final Map<JCTree.JCClassDecl,String> createdClassNames = new HashMap<>();
    
    public static void setup(ProcessingEnvironment processingEnv) {
        JCTreeUtils.trees = JavacTrees.instance(processingEnv);
        JCTreeUtils.elementUtils = processingEnv.getElementUtils();
        var context = getContext(processingEnv);
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
    }
    
    public static Context getContext(ProcessingEnvironment processingEnv) {
        try {
            var f = processingEnv.getClass().getDeclaredField("context"); // 得到 context
            f.setAccessible(true);
            return  (Context) f.get(processingEnv);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static JCTree.JCIdent ident(String name){
        return treeMaker.Ident(names.fromString(name));
    }
    
    public static Name name(String name){
        return names.fromString(name);
    }
    
    public static JCTree.JCModifiers noMods(){
        return treeMaker.Modifiers(0);
    }
    
    public static JCTree.JCModifiers mods(long... flag){
        long flags = 0;
        for(long l : flag) flags |= l;
        return treeMaker.Modifiers(flags);
    }
    
    public static JCTree.JCFieldAccess select(String selected,String selector){
        return treeMaker.Select(makeIdent(selected), name(selector));
    }
    
    public static JCTree.JCExpression makeIdent(String name){
        //name = name.replace('$','.');
        var ele = name.split("\\.");
        JCTree.JCExpression e = treeMaker.Ident(names.fromString(ele[0]));
        for (int i = 1; i < ele.length; i++) {
            e = treeMaker.Select(e, names.fromString(ele[i]));
        }
        return e;
    }
    
    public static JCTree.JCLambda makeLambda(List<String> params,JCTree body){
        var p = List.<JCTree.JCVariableDecl>nil();
        for (var str : params){
            p = p.append(treeMaker.VarDef(mods(Flags.PARAMETER),name(str),null,null));
        }
        return treeMaker.Lambda(p,body);
    }
    
    public static JCTree.JCMethodInvocation makeApply(JCTree.JCExpression fn, List<JCTree.JCExpression> args){
        return treeMaker.Apply(List.nil(),fn,args);
    }
    
//    private static void setPos(JCTree target){
//        treeMaker.at(target.pos);
//    }
    
//    public static void increaseTreeMakerPos(){
//        treeMaker.at(treeMaker.pos+1);
//    }
    
    public static boolean isPublicStatic(JCTree.JCVariableDecl del){
        return isPublicStatic(del.mods.flags);
    }
    
    public static boolean isPublicStatic(long flag){
        return Modifier.isPublic((int) flag) && Modifier.isStatic((int) flag);
    }
    
    public static <T> List<T> emptyList(){
        return List.nil();
    }
    
    public static String getClassFullName(JCTree.JCClassDecl tree) {
        if(tree.sym != null){
            return tree.sym.toString();
        }
        else if(createdClassNames.containsKey(tree)){
            return createdClassNames.get(tree);
        }
        return tree.name.toString();
    }
    
    public static JCTree.JCParens cast(String type, JCTree.JCExpression expr){
        return treeMaker.Parens(treeMaker.TypeCast(makeIdent(type),expr));
    }
    
    public static class Adder{
        
        public static void addImplement2Class(JCTree.JCClassDecl target, String interfaceName) {
            target.implementing = target.implementing.append(makeIdent(interfaceName));
        }
        
        public static void addField2Class(JCTree.JCClassDecl target, JCTree.JCExpression fieldType, String fieldName, JCTree.JCModifiers modifiers, JCTree.JCExpression initValue) {
            target.defs = target.defs.append(treeMaker.VarDef(modifiers,name(fieldName),fieldType,initValue));
        }
        
        public static void addField2Class(JCTree.JCClassDecl target, String fieldType, String fieldName, JCTree.JCModifiers modifiers, JCTree.JCExpression initValue) {
            target.defs = target.defs.append(treeMaker.VarDef(modifiers,name(fieldName),makeIdent(fieldType),initValue));
        }
        
        public static void addAnnotation2Class(JCTree.JCClassDecl target, String annotationName, List<JCTree.JCExpression> args) {
            target.mods.annotations = target.mods.annotations.append(treeMaker.Annotation(makeIdent(annotationName), args));
        }
        
        public static void addAnnotation2Method(JCTree.JCMethodDecl target, String annotationName, List<JCTree.JCExpression> args) {
            target.mods.annotations = target.mods.annotations.append(treeMaker.Annotation(makeIdent(annotationName), args));
        }
        
        public static void addAnnotation2Methods(String annotationName, List<JCTree.JCExpression> args, JCTree.JCMethodDecl... target) {
            Arrays.stream(target).forEach(m -> addAnnotation2Method(m,annotationName,args));
        }
        
        public static void addMethod2Class(JCTree.JCClassDecl target, JCTree.JCMethodDecl method){
            target.defs = target.defs.append(method);
        }
        
        public static void addClass2Class(JCTree.JCClassDecl target, JCTree.JCClassDecl classDecl){
            createdClassNames.put(classDecl,getClassFullName(target) + "." + classDecl.name.toString());
            target.defs = target.defs.append(classDecl);
        }
        
        public static void addMethod2Class(JCTree.JCClassDecl target, String methodName, JCTree.JCExpression returnType, JCTree.JCModifiers modifiers, List<JCTree.JCVariableDecl> args, JCTree.JCBlock block) {
            target.defs = target.defs.append(treeMaker.MethodDef(modifiers,name(methodName),returnType,List.nil(),args,List.nil(),block,null));
        }
        
        public static void addModBusSubscriber(JCTree.JCClassDecl target, String modid){
            if(Finder.findAnnotation(target,Types.EVENT_BUS_SUBSCRIBER) != null){
                System.out.println("Class already has an event bus subscriber."+target.sym.fullname.toString());
                return;
            }
            var modid_ = treeMaker.Assign(ident("modid"), treeMaker.Literal(modid));
            var bus_ = treeMaker.Assign(ident("bus"), treeMaker.Select(makeIdent(Types.EVENT_BUS_SUBSCRIBER+".Bus"),name("MOD")));
            addAnnotation2Class(target,Types.EVENT_BUS_SUBSCRIBER,List.of(modid_,bus_));
        }
        
        public static void addEventListener2Class(JCTree.JCClassDecl target, String event, JCTree.JCBlock body){
            var flag = treeMaker.Modifiers(Modifier.PUBLIC | Modifier.STATIC);
            var eventName = event.substring(event.lastIndexOf('.')+1);
            var methodName = "xorLibGenerate$on"+eventName;
            Adder.addMethod2Class(target,methodName,treeMaker.TypeIdent(TypeTag.VOID),flag,
                    List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER),name("event_"),makeIdent(event),null)),
                    body);
            Adder.addAnnotation2Method(JCTreeUtils.Finder.findSingleMethod(target,methodName),Types.SUBSCRIBE_EVENT, List.nil());
        }
        
        public static void appendStatement2Method(JCTree.JCMethodDecl methodDecl, List<JCTree.JCStatement> statements){
            methodDecl.body.stats = methodDecl.body.stats.appendList(statements);
        }
    }
    
    public static class Finder{
        
        @Nullable
        public static Symbol.ClassSymbol findClassSymbolRecursive(@Nullable Symbol symbol){
            if(symbol == null) return null;
            if(symbol instanceof Symbol.ClassSymbol classSymbol) return classSymbol;
            return findClassSymbolRecursive(symbol.owner);
        }
        
        @Nullable
        public static JCTree.JCAnnotation findAnnotation(JCTree.JCVariableDecl tree,String annoName){
            return tree.mods.annotations.stream().filter(anno -> annoName.equals(anno.type.toString())).findFirst().orElse(null);
        }
        
        @Nullable
        public static JCTree.JCAnnotation findAnnotation(JCTree.JCClassDecl tree,String annoName){
            return tree.mods.annotations.stream().filter(anno -> annoName.equals(anno.type.toString())).findFirst().orElse(null);
        }
        
        public static java.util.List<JCTree.JCMethodDecl> findMethods(JCTree.JCClassDecl target, String methodName){
            return target.defs.stream().filter(t -> t instanceof JCTree.JCMethodDecl m && m.name.toString().equals(methodName)).map(t -> (JCTree.JCMethodDecl) t).toList();
        }
        
        public static JCTree.JCMethodDecl findSingleMethod(JCTree.JCClassDecl target, String methodName){
            return findMethods(target,methodName).stream().findFirst().orElseThrow();
        }
        
        public static java.util.List<JCTree.JCMethodDecl> findConstructors(JCTree.JCClassDecl target){
            return findMethods(target,"<init>");
        }
        
        public static java.util.List<JCTree.JCVariableDecl> findFieldWithAnno(JCTree.JCClassDecl target, String annoName){
            return target.defs.stream()
                    .filter( def -> def instanceof JCTree.JCVariableDecl vd &&
                            vd.mods.annotations.stream().anyMatch(anno -> annoName.equals(anno.type.toString())))
                    .map( def -> (JCTree.JCVariableDecl)def )
                    .toList();
        }
    }
    
    public static class Modifiers{
        
        public static JCTree.JCModifiers public_(){
            return treeMaker.Modifiers(Modifier.PUBLIC);
        }
        
        public static JCTree.JCModifiers publicStatic(){
            return treeMaker.Modifiers(Modifier.PUBLIC | Modifier.STATIC);
        }
        
        public static JCTree.JCModifiers publicStaticFinal(){
            return treeMaker.Modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
        }
        
        public static JCTree.JCModifiers privateStaticFinal(){
            return treeMaker.Modifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
        }
        
        public static JCTree.JCModifiers param(){
            return treeMaker.Modifiers(Flags.PARAMETER);
        }
    }
    
    public static class PosStack{
        private final LinkedList<Integer> stack =  new LinkedList<>();
        
        public void pushPos(JCTree tree){
            stack.push(tree.pos);
            treeMaker.pos = stack.getLast();
        }
        
        public void iinc(){
            if(stack.isEmpty()){
                throw new IllegalStateException("Pos stack is empty.");
            }
            else stack.addLast(stack.removeLast()+1);
            treeMaker.pos = stack.getLast();
        }
        
        public void popPos(){
            stack.pop();
            treeMaker.pos = stack.getLast();
        }
    }
}
