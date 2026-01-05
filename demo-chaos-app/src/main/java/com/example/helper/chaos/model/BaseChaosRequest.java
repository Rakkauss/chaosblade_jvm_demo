package com.example.helper.chaos.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Chaos 请求基础类
 * 
 * 统一管理所有请求类的共同字段和方法，减少代码重复
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public abstract class BaseChaosRequest {
    
    /** 目标类型（如：jvm, okhttp3, dubbo-consumer 等） */
    protected String target;
    
    /** 动作类型（delay, throws, mock） */
    protected String action;
    
    /** 延迟时间（毫秒，action=delay 时使用） */
    protected Integer time;
    
    /** 异常类型（action=throws 时使用） */
    protected String exception;
    
    /** 异常消息（action=throws 时使用） */
    protected String exceptionMessage;
    
    /** Mock 返回值（action=mock 时使用） */
    protected String value;
    
    /**
     * 创建延迟注入请求
     * 
     * @param request 请求对象
     * @param time 延迟时间（毫秒）
     * @param <T> 请求类型
     * @return 请求对象
     */
    public static <T extends BaseChaosRequest> T delay(T request, int time) {
        request.action = "delay";
        request.time = time;
        return request;
    }
    
    /**
     * 创建异常注入请求
     * 
     * @param request 请求对象
     * @param exception 异常类型
     * @param message 异常消息
     * @param <T> 请求类型
     * @return 请求对象
     */
    public static <T extends BaseChaosRequest> T throwsException(T request, String exception, String message) {
        request.action = "throws";
        request.exception = exception;
        request.exceptionMessage = message;
        return request;
    }
    
    /**
     * 创建 Mock 注入请求
     * 
     * @param request 请求对象
     * @param value Mock 返回值
     * @param <T> 请求类型
     * @return 请求对象
     */
    public static <T extends BaseChaosRequest> T mock(T request, String value) {
        request.action = "mock";
        request.value = value;
        return request;
    }
    
    // Getters
    
    public String getTarget() {
        return target;
    }
    
    public String getAction() {
        return action;
    }
    
    public Integer getTime() {
        return time;
    }
    
    public String getException() {
        return exception;
    }
    
    public String getExceptionMessage() {
        return exceptionMessage;
    }
    
    public String getValue() {
        return value;
    }
    
    // Setters
    
    protected void setTarget(String target) {
        this.target = target;
    }
    
    protected void setAction(String action) {
        this.action = action;
    }
    
    protected void setTime(Integer time) {
        this.time = time;
    }
    
    protected void setException(String exception) {
        this.exception = exception;
    }
    
    protected void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
    
    protected void setValue(String value) {
        this.value = value;
    }
}

