package com.xkball.xorlib.util.jctree;

import com.sun.source.tree.ImportTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.xkball.xorlib.api.internal.IExtendedProcessingEnv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImportHelper {
    
    private final List<JCTree.JCImport> imports;
    private final List<JCTree.JCImport> staticImports;
    
    public ImportHelper(IExtendedProcessingEnv env, Symbol.ClassSymbol classSymbol){
        this(env.getJavacTrees().getPath(classSymbol).getCompilationUnit().getImports());
    }
    
    public ImportHelper(List< ? extends ImportTree> importTrees) {
        var imports = new ArrayList<JCTree.JCImport>();
        importTrees.forEach(tree -> imports.add((JCTree.JCImport) tree));
        this.imports = imports.stream().filter(i -> !i.staticImport).toList();
        this.staticImports = imports.stream().filter(i -> i.staticImport).toList();
    }
    
    public boolean haveImport(String className){
        return imports.stream()
                .anyMatch(i -> i.qualid.toString().equals(className));
    }
    
    public boolean haveStaticImport(String className,String member){
        return staticImports.stream()
                .anyMatch(i -> i.qualid.toString().equals(className + "." + member));
    }
    
    public boolean matchMethodInvoke(JCTree.JCMethodInvocation methodInvocation, String methodClass, String methodName){
        var meth = methodInvocation.meth;
        var methName = meth.toString();
        var classShortName = Arrays.asList(methodClass.split("\\.")).getLast();
        if(methName.equals(methodClass + "." + methodName)) return true;
        if(haveImport(methodClass) && methName.equals(classShortName + "." + methodName)) return true;
        return haveStaticImport(methodClass, methodName) && methName.equals(methodName);
    }
}
