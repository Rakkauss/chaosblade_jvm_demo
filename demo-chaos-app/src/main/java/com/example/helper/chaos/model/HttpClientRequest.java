package com.example.helper.chaos.model;

/**
 * HTTP 客户端注入请求模型
 * 
 * @author rakkaus
 */
public class HttpClientRequest {
    
    /** 目标类型(okhttp3, resttemplate) */
    public String target;
    
    /** 动作类型(delay, throws, mock) */
    public String action;
    
    /** URL 匹配模式(支持通配符) */
    public String url;
    
    /** HTTP 方法(GET, POST, PUT, DELETE等) */
    public String httpMethod;
    
    /** 延迟时间(毫秒, action=delay时使用) */
    public Integer time;
    
    /** 异常类型(action=throws时使用) */
    public String exception;
    
    /** 异常消息(action=throws时使用) */
    public String message;
    
    /** Mock 返回值(action=mock时使用) */
    public String value;
    
    /**
     * 创建 OkHttp3 延迟注入请求
     * 
     * @param url URL 匹配模式
     * @param httpMethod HTTP 方法
     * @param delayMs 延迟时间(毫秒)
     * @return 请求对象
     */
    public static HttpClientRequest okhttp3Delay(String url, String httpMethod, int delayMs) {
        HttpClientRequest request = new HttpClientRequest();
        request.target = "okhttp3";
        request.action = "delay";
        request.url = url;
        request.httpMethod = httpMethod;
        request.time = delayMs;
        return request;
    }
    
    /**
     * 创建 OkHttp3 异常注入请求
     * 
     * @param url URL 匹配模式
     * @param httpMethod HTTP 方法
     * @param exception 异常类型
     * @param message 异常消息
     * @return 请求对象
     */
    public static HttpClientRequest okhttp3Throws(String url, String httpMethod, String exception, String message) {
        HttpClientRequest request = new HttpClientRequest();
        request.target = "okhttp3";
        request.action = "throws";
        request.url = url;
        request.httpMethod = httpMethod;
        request.exception = exception;
        request.message = message;
        return request;
    }
    
    /**
     * 创建 RestTemplate 延迟注入请求
     * 
     * @param url URL 匹配模式
     * @param httpMethod HTTP 方法
     * @param delayMs 延迟时间(毫秒)
     * @return 请求对象
     */
    public static HttpClientRequest restTemplateDelay(String url, String httpMethod, int delayMs) {
        HttpClientRequest request = new HttpClientRequest();
        request.target = "resttemplate";
        request.action = "delay";
        request.url = url;
        request.httpMethod = httpMethod;
        request.time = delayMs;
        return request;
    }
    
    /**
     * 创建 RestTemplate 异常注入请求
     * 
     * @param url URL 匹配模式
     * @param httpMethod HTTP 方法
     * @param exception 异常类型
     * @param message 异常消息
     * @return 请求对象
     */
    public static HttpClientRequest restTemplateThrows(String url, String httpMethod, String exception, String message) {
        HttpClientRequest request = new HttpClientRequest();
        request.target = "resttemplate";
        request.action = "throws";
        request.url = url;
        request.httpMethod = httpMethod;
        request.exception = exception;
        request.message = message;
        return request;
    }
}

