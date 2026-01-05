package com.example.helper.chaos.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 异常注入请求
 * 
 * @author rakkaus
 */
public class ThrowsRequest extends BaseChaosRequest {
    
    private String className;
    private String methodName;
    
    /** 异常消息（JVM throws 使用 message，覆盖父类的 exceptionMessage） */
    @JsonProperty("message")
    private String message;
    
    /**
     * 创建异常注入请求
     */
    public static ThrowsRequest of(String className, String methodName, 
                                   String exception, String exceptionMessage) {
        ThrowsRequest request = new ThrowsRequest();
        request.setTarget("jvm");
        request.setAction("throws");
        request.className = className;
        request.methodName = methodName;
        request.setException(exception);
        request.message = exceptionMessage;  // 使用 message 字段，序列化为 "message"
        return request;
    }
    
    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    
    // Setters
    public void setClassName(String className) { this.className = className; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
}

