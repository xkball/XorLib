package com.xkball.xorlib.util;

public class LogHelper {
    
    public static final LogHelper INSTANCE = new LogHelper();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final boolean debug = System.getenv("XORLIB.DEBUG") != null;
    
    private LogHelper() {}
    
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
        if (debug) {
            log(msg, clazz);
        }
    }
}
