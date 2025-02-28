package com.wjh.middleware.dynamic;

import com.wjh.middleware.DBContextHolder;
import com.wjh.middleware.annotation.DBRouter;
import com.wjh.middleware.strategy.IDualWriteStrategy;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Component
@Intercepts(value = {@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class})})
public class DynamicMybatisPlugin implements Interceptor {

    @Autowired
    private IDualWriteStrategy dualWriteStrategy;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            String methodName = invocation.getMethod().getName();
            MappedStatement mappedStatement = (MappedStatement)invocation.getArgs()[0];
            String mapperMethodName = mappedStatement.getId();
            Class<?> mapperClass = Class.forName(mapperMethodName.substring(0, mapperMethodName.lastIndexOf(".")));
            String methodMapperName = mapperMethodName.substring(mapperMethodName.lastIndexOf(".") + 1);
            Method method = mapperClass.getMethod(methodMapperName, mappedStatement.getParameterMap().getType());
            if(methodName.equals("query")){
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
                if(method.isAnnotationPresent(DBRouter.class)){
                    DBRouter dbRouter = method.getAnnotation(DBRouter.class);
                    if(dbRouter.dualWrite()){
                        dualWriteStrategy.doDualWrite(invocation);
                    }
                }
            }
            return invocation.proceed();
        } finally {
            DBContextHolder.clearDataSourceKey();
        }
    }
}
