package com.example.helper.chaos.model;

/**
 * HTTP 服务端注入请求模型
 * 
 * @author rakkaus
 */
public class HttpServerRequest extends BaseChaosRequest {
    
    private String className = "javax.servlet.http.HttpServlet";
    private String methodName = "service";
    private String type;  // Mock 返回类型
    
    /**
     * 创建延迟注入请求
     */
    public static HttpServerRequest delay(int time) {
        HttpServerRequest request = new HttpServerRequest();
        request.setTarget("httpserver");
        BaseChaosRequest.delay(request, time);
        return request;
    }
    
    /**
     * 创建异常注入请求
     */
    public static HttpServerRequest throwsException(String exception, String message) {
        HttpServerRequest request = new HttpServerRequest();
        request.setTarget("httpserver");
        BaseChaosRequest.throwsException(request, exception, message);
        return request;
    }
    
    /**
     * 创建 Mock 注入请求
     */
    public static HttpServerRequest mock(String value, String type) {
        HttpServerRequest request = new HttpServerRequest();
        request.setTarget("httpserver");
        BaseChaosRequest.mock(request, value);
        request.type = type;
        return request;
    }
    
    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    public String getType() { return type; }
}
