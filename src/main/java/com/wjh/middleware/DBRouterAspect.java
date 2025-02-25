package com.wjh.middleware;

import com.wjh.middleware.strategy.IDBRouterStrategy;
import com.wjh.middleware.strategy.impl.DBRouterStrategyDualWrite;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

@Aspect
public class DBRouterAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBRouterAspect.class);
    private final DBRouterConfig dbRouterConfig;
    private final IDBRouterStrategy dbRouterStrategy;
    public DBRouterAspect(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategy = dbRouterStrategy;
    }

}
