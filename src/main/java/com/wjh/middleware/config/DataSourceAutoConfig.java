package com.wjh.middleware.config;


import com.wjh.middleware.DBRouterConfig;
import com.wjh.middleware.dynamic.DynamicDataSource;
import com.wjh.middleware.strategy.IDBRouterStrategy;
import com.wjh.middleware.strategy.impl.DBRouterStrategyDualWrite;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@EnableConfigurationProperties(DataSourceProperties.class)
@ConditionalOnProperty(prefix = "spring.demo", name = "name")
@Configuration
public class DataSourceAutoConfig {
    @Resource
    private DataSourceProperties dataSourceProperties;
    /**
     * 数据源配置组
     */
    private final Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();

    /**
     * 默认数据源配置
     */
    private Map<String, Object> defaultDataSourceConfig;

    @Bean
    public IDBRouterStrategy dbRouterStrategy() {
        return new DBRouterStrategyDualWrite();
    }
    @Bean
    public DBRouterConfig dbRouterConfig() {
        return new DBRouterConfig(dataSourceProperties.getPersistenceDataSource());
    }
    @Bean
    public DataSource dataSource() {
        //创建数据源
        Map<Object, Object> targetDataSources = new HashMap<>();
        for (String dbInfo : dataSourceMap.keySet()) {
            Map<String, Object> objectMap = dataSourceMap.get(dbInfo);
            targetDataSources.put(dbInfo, new DriverManagerDataSource(objectMap.get("url").toString(),
                    objectMap.get("username").toString(), objectMap.get("password").toString()));
        }

        //设置数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(new DriverManagerDataSource(
                defaultDataSourceConfig.get("url").toString(),
                defaultDataSourceConfig.get("username").toString(),
                defaultDataSourceConfig.get("password").toString()
        ));

        return dynamicDataSource;
    }
}
