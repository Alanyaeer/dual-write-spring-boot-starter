package com.wjh.middleware.config;


import com.wjh.middleware.constants.DataSourceConstants;
import com.wjh.middleware.dynamic.DynamicDataSource;
import com.wjh.middleware.dynamic.DynamicMybatisPlugin;
import com.wjh.middleware.properties.IgniteDataSourceProperties;
import com.wjh.middleware.properties.MysqlDataSourceProperties;
import com.wjh.middleware.strategy.IDualWriteStrategy;
import com.wjh.middleware.strategy.impl.DualWriteStrategySync;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
@EnableConfigurationProperties({MysqlDataSourceProperties.class, IgniteDataSourceProperties.class})
@ConditionalOnProperty(prefix = "spring.dw", name = "enabled", havingValue = "true")
@Configuration
public class DataSourceAutoConfig {

    @Resource
    private MysqlDataSourceProperties mysqlProperties;
    @Resource
    private IgniteDataSourceProperties igniteProperties;
    //会依赖注入Spring容器中所有的mybatis的Interceptor拦截器

    /**
     * 默认数据源配置
     */
    private static final Map<Object, Object> defaultDataSourceConfig = new HashMap<>();

    @Bean
    public Interceptor interceptor(){
        return new DynamicMybatisPlugin();
    }
    @Bean(name = "dynamicDataSource")
    public DataSource dataSource() {
        DriverManagerDataSource igniteDataSource = new DriverManagerDataSource();
        igniteDataSource.setUrl(igniteProperties.getUrl());
        igniteDataSource.setUsername(igniteProperties.getUsername());
        igniteDataSource.setPassword(igniteProperties.getPassword());
        igniteDataSource.setDriverClassName(igniteProperties.getDriverClassName());

        DriverManagerDataSource mysqlDataSource = new DriverManagerDataSource();
        mysqlDataSource.setUrl(mysqlProperties.getUrl());
        mysqlDataSource.setUsername(mysqlProperties.getUsername());
        mysqlDataSource.setPassword(mysqlProperties.getPassword());
        mysqlDataSource.setDriverClassName(mysqlProperties.getDriverClassName());

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setDefaultTargetDataSource(mysqlDataSource);
        defaultDataSourceConfig.put(DataSourceConstants.MYSQL_DATA_SOURCE, mysqlDataSource);
        defaultDataSourceConfig.put(DataSourceConstants.IGNITE_DATA_SOURCE, igniteDataSource);
        dynamicDataSource.setTargetDataSources(defaultDataSourceConfig);
        return dynamicDataSource;
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource());

        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(dataSourceTransactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
    }
    @Bean
    @ConditionalOnMissingBean
    public IDualWriteStrategy dualWriteStrategy(){
        return new DualWriteStrategySync();
    }
}
