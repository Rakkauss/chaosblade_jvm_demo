package com.example.helper.chaos.model;

/**
 * RestTemplate 客户端注入请求模型
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class RestTemplateRequest extends BaseChaosRequest {
    
    private String className = "org.springframework.web.client.RestTemplate";
    private String methodName = "doExecute";
    
    /**
     * 创建延迟注入请求
     */
    public static RestTemplateRequest delay(int time) {
        RestTemplateRequest request = new RestTemplateRequest();
        request.setTarget("resttemplate");
        BaseChaosRequest.delay(request, time);
        return request;
    }
    
    /**
     * 创建异常注入请求
     */
    public static RestTemplateRequest throwsException(String exception, String message) {
        RestTemplateRequest request = new RestTemplateRequest();
        request.setTarget("resttemplate");
        BaseChaosRequest.throwsException(request, exception, message);
        return request;
    }
    
    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
}

