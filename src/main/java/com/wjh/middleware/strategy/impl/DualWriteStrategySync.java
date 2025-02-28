package com.wjh.middleware.strategy.impl;

import com.wjh.middleware.DBContextHolder;
import com.wjh.middleware.exception.DBException;
import com.wjh.middleware.strategy.IDualWriteStrategy;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.InvocationTargetException;

public class DualWriteStrategySync implements IDualWriteStrategy {
    /**
     * 同步双写
     * @param invocation {@link Invocation}
     */
    @Override
    public void doDualWrite(Invocation invocation) {
        try {
            invocation.proceed();
            DBContextHolder.setIgniteDataSource();
            invocation.proceed();
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new DBException(e);
        }
    }
}
