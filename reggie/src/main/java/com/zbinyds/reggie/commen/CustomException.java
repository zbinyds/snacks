package com.zbinyds.reggie.commen;

/**
 * @author zbinyds
 * @time 2022/08/17 16:38
 *
 * 自定义异常（相关业务异常）
 */
public class CustomException extends RuntimeException{
    public CustomException(String message) {
        super(message);
    }
}
