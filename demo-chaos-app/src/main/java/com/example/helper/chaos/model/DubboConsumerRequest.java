package com.example.helper.chaos.model;

/**
 * Dubbo 消费者注入请求模型
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class DubboConsumerRequest extends BaseChaosRequest {
    
    // 支持 Dubbo 3.x (org.apache.dubbo) 和 Dubbo 2.x (com.alibaba.dubbo)
    // 默认使用 Dubbo 3.x
    private String className = "org.apache.dubbo.rpc.proxy.AbstractProxyInvoker";
    private String methodName = "invoke";
    
    /**
     * 创建延迟注入请求
     */
    public static DubboConsumerRequest delay(int time) {
        DubboConsumerRequest request = new DubboConsumerRequest();
        request.setTarget("dubbo-consumer");
        BaseChaosRequest.delay(request, time);
        return request;
    }
    
    /**
     * 创建异常注入请求
     */
    public static DubboConsumerRequest throwsException(String exception, String message) {
        DubboConsumerRequest request = new DubboConsumerRequest();
        request.setTarget("dubbo-consumer");
        BaseChaosRequest.throwsException(request, exception, message);
        return request;
    }
    
    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
}

