package com.xkball.xorlib.util.jctree;

import com.sun.tools.javac.code.Symbol;
import org.jetbrains.annotations.Nullable;

public class SymbolUtils {
    
    @Nullable
    public static Symbol.PackageSymbol findPackage(@Nullable Symbol symbol) {
        if (symbol == null) return null;
        if (symbol instanceof Symbol.PackageSymbol packageSymbol) return packageSymbol;
        return findPackage(symbol.owner);
    }
}
