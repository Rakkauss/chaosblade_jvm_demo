package com.example.helper.chaos.model;

/**
 * Mock 注入请求
 * 
 * @author rakkaus
 */
public class MockRequest extends BaseChaosRequest {
    
    private String className;
    private String methodName;
    
    /**
     * 创建 Mock 注入请求
     */
    public static MockRequest of(String className, String methodName, String value) {
        MockRequest request = new MockRequest();
        request.setTarget("jvm");
        request.setAction("mock");
        request.className = className;
        request.methodName = methodName;
        request.setValue(value);
        return request;
    }
    
    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    
    // Setters
    public void setClassName(String className) { this.className = className; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
}

