package com.example.helper.chaos.model;

/**
 * Dubbo 提供者注入请求模型
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class DubboProviderRequest extends BaseChaosRequest {
    
    // 支持 Dubbo 3.x (org.apache.dubbo) 和 Dubbo 2.x (com.alibaba.dubbo)
    // 默认使用 Dubbo 3.x
    private String className = "org.apache.dubbo.rpc.proxy.AbstractProxyInvoker";
    private String methodName = "doInvoke";
    
    /**
     * 创建延迟注入请求
     */
    public static DubboProviderRequest delay(int time) {
        DubboProviderRequest request = new DubboProviderRequest();
        request.setTarget("dubbo-provider");
        BaseChaosRequest.delay(request, time);
        return request;
    }
    
    /**
     * 创建异常注入请求
     */
    public static DubboProviderRequest throwsException(String exception, String message) {
        DubboProviderRequest request = new DubboProviderRequest();
        request.setTarget("dubbo-provider");
        BaseChaosRequest.throwsException(request, exception, message);
        return request;
    }
    
    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
}

