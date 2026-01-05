package com.example.helper.chaos.model;

/**
 * 延迟注入请求模型
 * 
 * @author rakkaus
 */
public class DelayRequest extends BaseChaosRequest {
    
    /** 目标类名（全限定名，如：com.example.service.UserService） */
    private String className;
    
    /** 目标方法名（如：getUserById） */
    private String methodName;
    
    /**
     * 创建延迟注入请求
     * 
     * @param className 目标类名
     * @param methodName 目标方法名
     * @param time 延迟时间（毫秒）
     * @return 延迟注入请求对象
     */
    public static DelayRequest of(String className, String methodName, int time) {
        DelayRequest request = new DelayRequest();
        request.setTarget("jvm");
        request.setAction("delay");
        request.className = className;
        request.methodName = methodName;
        request.setTime(time);
        return request;
    }
    
    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    
    // Setters
    public void setClassName(String className) { this.className = className; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
}

