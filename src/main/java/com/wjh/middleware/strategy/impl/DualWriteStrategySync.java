package com.wjh.middleware.strategy.impl;

import com.wjh.middleware.DBContextHolder;
import com.wjh.middleware.exception.DBException;
import com.wjh.middleware.strategy.IDualWriteStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class DualWriteStrategySync implements IDualWriteStrategy {

    @Autowired
    private ConfigurableApplicationContext context;

    private ExecutorService multiThreadExecutor = Executors.newFixedThreadPool(10);

    class DualWriteTask implements Runnable {
        private final Invocation invocation;
        public DualWriteTask(Invocation invocation){
            this.invocation = invocation;
        }
        @Override
        public void run() {
            try {
                DBContextHolder.setIgniteDataSource();

                MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];

                String mapperMethodName = mappedStatement.getId();
                String mapperClassName = mapperMethodName.substring(0, mapperMethodName.lastIndexOf("."));
                String targetMethodName = mapperMethodName.substring(mapperMethodName.lastIndexOf(".") + 1);

                Class<?> mapperClass = Class.forName(mapperClassName);

                Object mapperInstance = context.getBean(mapperClass);

                Method method = Arrays.stream(mapperClass.getMethods())
                        .filter(item -> item.getName().equals(targetMethodName))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchMethodException("Method not found: " + targetMethodName));

                Object[] methodArgs;
                if (invocation.getArgs().length > 1 && invocation.getArgs()[1] instanceof Map) {
                    // 如果参数是 Map 类型（多参数情况）
                    Map<?, ?> paramMap = (Map<?, ?>) invocation.getArgs()[1];
                    methodArgs = extractMethodArgs(method, paramMap);
                } else {
                    methodArgs = Arrays.copyOfRange(invocation.getArgs(), 1, invocation.getArgs().length);
                }
                method.invoke(mapperInstance, methodArgs);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }finally {
                DBContextHolder.clearDataSourceKey();
            }
        }
    }
    /**
     * 同步双写
     * @param invocation {@link Invocation}
     */
    @Override
    public Object doDualWrite(Invocation invocation) {
        try {
            Object proceed = invocation.proceed();
            multiThreadExecutor.submit(new DualWriteTask(invocation));
            return proceed;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new DBException(e);
        }
    }
    /**
     * 从 Map 中提取方法参数值，并按方法定义的参数顺序返回
     */
    private Object[] extractMethodArgs(Method method, Map<?, ?> paramMap) {
        Class<?>[] parameterTypes = method.getParameterTypes(); // 方法参数类型
        Object[] args = new Object[parameterTypes.length]; // 参数值数组

        for (int i = 0; i < parameterTypes.length; i++) {
            // 参数名可能是 param1, param2, ... 或者是实际参数名
            String paramName = "param" + (i + 1);
            if (paramMap.containsKey(paramName)) {
                args[i] = paramMap.get(paramName);
            } else {
                // 如果 param1, param2 不存在，尝试使用实际参数名
                String actualParamName = method.getParameters()[i].getName(); // 需要 -parameters 编译选项支持
                args[i] = paramMap.get(actualParamName);
            }
        }

        return args;
    }
}
