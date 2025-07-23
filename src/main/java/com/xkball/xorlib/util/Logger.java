package com.xkball.xorlib.util;

import com.xkball.xorlib.XorLib;

public class Logger {
    
    public static final Logger INSTANCE = new Logger();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    
    private Logger() {}
    
    public void log(Object msg) {
        var clazz = STACK_WALKER.getCallerClass();
        log(msg.toString(), clazz);
    }
    
    public void log(String msg) {
        var clazz = STACK_WALKER.getCallerClass();
        log(msg,clazz);
    }
    
    public void log(String msg, Class<?> clazz) {
        System.out.println("["+clazz.getName()+"] " + msg);
    }
    
    public void debug(String msg) {
        var clazz = STACK_WALKER.getCallerClass();
        if (XorLib.DEBUG) {
            log(msg, clazz);
        }
    }
}
