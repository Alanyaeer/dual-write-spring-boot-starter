package com.wjh.middleware;

public class DBRouterConfig {
    private String persistenceDataSource;

    public DBRouterConfig(String persistenceDataSource) {
        this.persistenceDataSource = persistenceDataSource;
    }
}
