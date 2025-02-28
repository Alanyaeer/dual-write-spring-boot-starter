package com.wjh.middleware.config;


import com.wjh.middleware.DBRouterConfig;
import com.wjh.middleware.constants.DataSourceConstants;
import com.wjh.middleware.dynamic.DynamicDataSource;
import com.wjh.middleware.properties.DataSourceProperties;
import com.wjh.middleware.properties.IgniteDataSourceProperties;
import com.wjh.middleware.properties.MysqlDataSourceProperties;
import com.wjh.middleware.strategy.IDualWriteStrategy;
import com.wjh.middleware.strategy.impl.DualWriteStrategySync;
import org.springframework.beans.factory.annotation.Qualifier;
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

@EnableConfigurationProperties(DataSourceProperties.class)
@ConditionalOnProperty(prefix = "spring.demo", name = "name")
@Configuration
public class DataSourceAutoConfig {
    @Resource
    private DataSourceProperties dataSourceProperties;
    @Resource
    private MysqlDataSourceProperties mysqlProperties;
    @Resource
    private IgniteDataSourceProperties igniteProperties;


    /**
     * 默认数据源配置
     */
    private static final Map<Object, Object> defaultDataSourceConfig = new HashMap<>();

    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(mysqlProperties.getUrl());
        dataSource.setUsername(mysqlProperties.getUsername());
        dataSource.setPassword(mysqlProperties.getPassword());
        dataSource.setDriverClassName(mysqlProperties.getDriverClassName());
        return dataSource;
    }

    @Bean(name = "igniteDataSource")
    public DataSource igniteDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(igniteProperties.getUrl());
        dataSource.setUsername(igniteProperties.getUsername());
        dataSource.setPassword(igniteProperties.getPassword());
        dataSource.setDriverClassName(igniteProperties.getDriverClassName());
        return dataSource;
    }


/*    @Bean
    public IDBRouterStrategy dbRouterStrategy() {
        return new DBRouterStrategyDualWrite();
    }*/
    @Bean
    public DBRouterConfig dbRouterConfig() {
        return new DBRouterConfig(dataSourceProperties.getPersistenceDataSource());
    }

    @Bean
    public DataSource dataSource(@Qualifier("mysqlDataSource") DataSource mysqlDataSource,
                                 @Qualifier("igniteDataSource") DataSource igniteDataSource) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setDefaultTargetDataSource(mysqlDataSource);
        defaultDataSourceConfig.put(DataSourceConstants.MYSQL_DATA_SOURCE, mysqlDataSource);
        defaultDataSourceConfig.put(DataSourceConstants.IGNITE_DATA_SOURCE, igniteDataSource);
        dynamicDataSource.setTargetDataSources(defaultDataSourceConfig);
        return dynamicDataSource;
    }

    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);

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
