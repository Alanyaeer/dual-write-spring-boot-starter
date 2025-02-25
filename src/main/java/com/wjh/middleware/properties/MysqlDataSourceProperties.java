package com.wjh.middleware.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.datasource.mysql")
public class MysqlDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;

}