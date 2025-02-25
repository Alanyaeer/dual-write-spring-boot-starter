package com.wjh.middleware;

public class DBContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    public static void setDataSourceKey(String dbKey){
        contextHolder.set(dbKey);
    }
    public static String getDataSourceKey(){
        return contextHolder.get();
    }
}
