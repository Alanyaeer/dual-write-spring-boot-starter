package com.wjh.middleware.strategy;

import org.apache.ibatis.plugin.Invocation;

public interface IDualWriteStrategy {
    Object doDualWrite(Invocation invocation);
}
