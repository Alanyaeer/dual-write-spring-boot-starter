package com.wjh.middleware.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {
    /**
     * 按照逗号隔开
     */
    private String dataSourceList;
    /**
     * 持久层数据源
     */
    private String persistenceDataSource;


}
