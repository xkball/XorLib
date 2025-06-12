package com.xkball.xorlib.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

public class Utils {
    
    public static <T> List<T> iterableToList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }
    
    public static <T> T assertSingleAndGet(Iterable<T> iterable, String errMessage) {
        var list = iterableToList(iterable);
        if(list.size() != 1) {
            throw new RuntimeException(errMessage);
        }
        return list.getFirst();
    }
    
    public static <T> List<T> union(List<T> list1, List<T> list2) {
        var list = new ArrayList<>(list1);
        list.addAll(list2);
        return Collections.unmodifiableList(list);
    }
}
