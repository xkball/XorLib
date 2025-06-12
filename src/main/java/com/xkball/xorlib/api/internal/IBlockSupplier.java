package com.xkball.xorlib.api.internal;

import com.sun.tools.javac.tree.JCTree;

import java.util.List;

public interface IBlockSupplier {
    //todo[xkball] 实现局部变量检查
    default void validLocals(List<String> locals) {}
    JCTree.JCBlock getAsBlock();
}
