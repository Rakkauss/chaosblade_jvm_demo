package com.example.helper.chaos.model;

/**
 * OkHttp3 客户端注入请求模型
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class OkHttp3Request extends BaseChaosRequest {
    
    private String className = "okhttp3.RealCall";
    private String methodName = "execute";
    
    /**
     * 创建延迟注入请求
     */
    public static OkHttp3Request delay(int time) {
        OkHttp3Request request = new OkHttp3Request();
        request.setTarget("okhttp3");
        BaseChaosRequest.delay(request, time);
        return request;
    }
    
    /**
     * 创建异常注入请求
     */
    public static OkHttp3Request throwsException(String exception, String message) {
        OkHttp3Request request = new OkHttp3Request();
        request.setTarget("okhttp3");
        BaseChaosRequest.throwsException(request, exception, message);
        return request;
    }
    
    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
}

