package com.hubz.common.util;

import cn.hutool.core.lang.Validator;
import lombok.Data;

import java.util.function.Supplier;

/**
 * @author hubz
 * @date 2022/5/7 14:32
 **/
@Data
public class MethodChain<R> {
    /**
     * 将要执行的方法
     */
    private Supplier<? extends R> func;

    /**
     * 链的下一步
     */
    private MethodChain<R> next;

    /**
     * 创建新节点
     * @author hubz
     * @date 2022/5/7 16:04
     *
     * @return com.hubz.minimdmanage.common.utils.MethodChain<R>
     **/
    public MethodChain<R> createMethodChain() {
        return new MethodChain<>();
    }

    /**
     * 加入执行方法并声称新的节点返回
     * @author hubz
     * @date 2022/5/7 16:07
     *
     * @param func 节点的执行方法
     * @return com.hubz.minimdmanage.common.utils.MethodChain<R>
     **/
    public MethodChain<R> appendHandler(Supplier<? extends R> func) {
        this.func = func;
        next = createMethodChain();
        return next;
    }

    /**
     * 执行链中的方法
     * @author hubz
     * @date 2022/5/7 16:08
     *
     *
     * @return R
     **/
    public R run() {
        R result = this.func.get();
        if (Validator.isNull(result)) {
            return Validator.isNull(this.next) ? null : this.next.run();
        }
        return result;
    }
}