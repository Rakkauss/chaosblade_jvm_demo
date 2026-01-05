package com.example.helper.chaos.model;

/**
 * Dubbo RPC 注入请求
 * 
 * 支持 Dubbo 消费者和提供者的故障注入
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class DubboRequest {
    
    /** 目标类型（dubbo-consumer、dubbo-provider） */
    public String target;
    
    /** 动作类型（delay、throws、mock） */
    public String action;
    
    /** 服务接口名 */
    public String serviceName;
    
    /** 方法名 */
    public String methodName;
    
    /** 延迟时间（毫秒，action=delay时使用） */
    public Integer time;
    
    /** 异常类型（action=throws时使用） */
    public String exception;
    
    /** 异常消息（action=throws时使用） */
    public String message;
    
    /** Mock 返回值（action=mock时使用） */
    public String value;
    
    /**
     * 创建消费者延迟注入请求
     * 
     * @param serviceName 服务接口名
     * @param methodName 方法名
     * @param delayMs 延迟时间（毫秒）
     * @return 请求对象
     */
    public static DubboRequest consumerDelay(String serviceName, String methodName, int delayMs) {
        DubboRequest request = new DubboRequest();
        request.target = "dubbo-consumer";
        request.action = "delay";
        request.serviceName = serviceName;
        request.methodName = methodName;
        request.time = delayMs;
        return request;
    }
    
    /**
     * 创建消费者异常注入请求
     * 
     * @param serviceName 服务接口名
     * @param methodName 方法名
     * @param exception 异常类型
     * @param message 异常消息
     * @return 请求对象
     */
    public static DubboRequest consumerThrows(String serviceName, String methodName, String exception, String message) {
        DubboRequest request = new DubboRequest();
        request.target = "dubbo-consumer";
        request.action = "throws";
        request.serviceName = serviceName;
        request.methodName = methodName;
        request.exception = exception;
        request.message = message;
        return request;
    }
    
    /**
     * 创建提供者延迟注入请求
     * 
     * @param serviceName 服务接口名
     * @param methodName 方法名
     * @param delayMs 延迟时间（毫秒）
     * @return 请求对象
     */
    public static DubboRequest providerDelay(String serviceName, String methodName, int delayMs) {
        DubboRequest request = new DubboRequest();
        request.target = "dubbo-provider";
        request.action = "delay";
        request.serviceName = serviceName;
        request.methodName = methodName;
        request.time = delayMs;
        return request;
    }
    
    /**
     * 创建提供者异常注入请求
     * 
     * @param serviceName 服务接口名
     * @param methodName 方法名
     * @param exception 异常类型
     * @param message 异常消息
     * @return 请求对象
     */
    public static DubboRequest providerThrows(String serviceName, String methodName, String exception, String message) {
        DubboRequest request = new DubboRequest();
        request.target = "dubbo-provider";
        request.action = "throws";
        request.serviceName = serviceName;
        request.methodName = methodName;
        request.exception = exception;
        request.message = message;
        return request;
    }
}

