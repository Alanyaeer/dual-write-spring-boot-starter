package com.wjh.middleware;

import com.wjh.middleware.annotation.DBRouter;
import com.wjh.middleware.strategy.IDBRouterStrategy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import static com.wjh.middleware.constants.DataSourceConstants.IGNITE_DATA_SOURCE;
import static com.wjh.middleware.constants.DataSourceConstants.MYSQL_DATA_SOURCE;

@Aspect
public class DBRouterAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBRouterAspect.class);
    private final DBRouterConfig dbRouterConfig;
    private final IDBRouterStrategy dbRouterStrategy;
    public DBRouterAspect(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategy = dbRouterStrategy;
    }
    @Pointcut("@annotation(com.wjh.middleware.annotation.DBRouter)")
    public void aopPoint(){}

    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint joinPoint, DBRouter dbRouter) throws Throwable {
        try {
            DBContextHolder.setMysqlDataSource();
            return joinPoint.proceed();
        }finally {
            DBContextHolder.clearDataSourceKey();
        }
    }
}
