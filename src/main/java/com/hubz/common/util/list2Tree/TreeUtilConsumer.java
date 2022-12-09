package com.hubz.common.util.list2Tree;

import java.util.List;

@FunctionalInterface
public interface TreeUtilConsumer<T> {

    void accept(T t, List<T> tList);

}
