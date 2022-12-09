package com.hubz.common.util.list2Tree;

@FunctionalInterface
public interface FloorConsumer<T, K extends Integer> {
    void accept(T t, Integer tList);
}