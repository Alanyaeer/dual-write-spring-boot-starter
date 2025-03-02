package com.wjh.middleware.exception;

public class DBException extends RuntimeException{
    public DBException(Exception e){
        super(e.getMessage());
    }
}
