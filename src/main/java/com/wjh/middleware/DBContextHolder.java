package com.wjh.middleware;

import com.wjh.middleware.constants.DataSourceConstants;

public class DBContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    public static void setMysqlDataSource(){
        contextHolder.set(DataSourceConstants.MYSQL_DATA_SOURCE);
    }
    public static void setIgniteDataSource(){
        contextHolder.set(DataSourceConstants.IGNITE_DATA_SOURCE);
    }
    public static String getDataSourceKey(){
        return contextHolder.get();
    }

    public static void clearDataSourceKey() {

        contextHolder.remove();
    }
}
