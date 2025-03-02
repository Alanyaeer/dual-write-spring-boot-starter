package com.wjh.middleware.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.dw.ignite")
public class IgniteDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;

}