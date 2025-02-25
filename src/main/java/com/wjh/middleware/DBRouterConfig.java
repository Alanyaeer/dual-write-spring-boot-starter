package com.wjh.middleware;

import lombok.Data;

@Data
public class DBRouterConfig {
    private String persistenceDataSource;

    public DBRouterConfig(String persistenceDataSource) {
        this.persistenceDataSource = persistenceDataSource;
    }
}
