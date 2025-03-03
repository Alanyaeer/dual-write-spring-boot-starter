package com.wjh.middleware.dynamic;


import com.wjh.middleware.DBContextHolder;
import com.wjh.middleware.annotation.DBRouter;
import com.wjh.middleware.constants.DataSourceConstants;
import com.wjh.middleware.strategy.IDualWriteStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Intercepts(value = {
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class DynamicMybatisPlugin implements Interceptor {

    @Resource
    private IDualWriteStrategy dualWriteStrategy;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            String methodName = invocation.getMethod().getName();
            MappedStatement mappedStatement = (MappedStatement)invocation.getArgs()[0];
            String mapperMethodName = mappedStatement.getId();
            Class<?> mapperClass = Class.forName(mapperMethodName.substring(0, mapperMethodName.lastIndexOf(".")));
            String methodMapperName = mapperMethodName.substring(mapperMethodName.lastIndexOf(".") + 1);
            // 官方不建议重载（甚至是不支持，所以我们直接找到对应的方法名就OK）
            Method method = Arrays.stream(mapperClass.getMethods()).filter(item -> item.getName().equals(methodMapperName)).findFirst().orElseThrow(() -> new NoSuchMethodException("Method not found: " + methodMapperName));
            log.info(methodMapperName);
            log.info("当前数据源是: {}", DBContextHolder.getDataSourceKey() == null ? "mysql" : DBContextHolder.getDataSourceKey());
            if(methodName.equals("query")){
                log.info("本次是否需要双写 {}" , method.isAnnotationPresent(DBRouter.class));
                if(method.isAnnotationPresent(DBRouter.class)){
                    DBRouter dbRouter = method.getAnnotation(DBRouter.class);
                    // mysql 和 ignite 双写
                    if(dbRouter.queryMemDB()){
                        DBContextHolder.setIgniteDataSource();
                    }
                    else{
                        DBContextHolder.setMysqlDataSource();
                    }
                }
                return invocation.proceed();
            }
            else if(methodName.equals("update")){
                // 此处需要规避双写造成的重复问题 。。。
                if(method.isAnnotationPresent(DBRouter.class) && (DBContextHolder.getDataSourceKey() == null || !DBContextHolder.getDataSourceKey().equals(DataSourceConstants.IGNITE_DATA_SOURCE))){
                    DBRouter dbRouter = method.getAnnotation(DBRouter.class);
                    log.info("本次是否需要双写 {}", dbRouter.dualWrite());
                    if(dbRouter.dualWrite()){
                        return dualWriteStrategy.doDualWrite(invocation);
                    }
                }
                return invocation.proceed();
            }
            else throw new NoSuchMethodException("未预判到的方法" +  methodName);
        } finally {
            DBContextHolder.clearDataSourceKey();
        }
    }
}
