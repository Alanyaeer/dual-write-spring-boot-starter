package com.wjh.middleware.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.datasource.ignite")
public class IgniteDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;

}