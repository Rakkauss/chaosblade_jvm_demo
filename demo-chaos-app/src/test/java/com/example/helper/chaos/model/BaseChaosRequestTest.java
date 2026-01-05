package com.example.helper.chaos.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * BaseChaosRequest 单元测试
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public class BaseChaosRequestTest {
    
    @Test
    public void testDelay() {
        DelayRequest request = new DelayRequest();
        BaseChaosRequest.delay(request, 2000);
        
        assertEquals("delay", request.getAction());
        assertEquals(Integer.valueOf(2000), request.getTime());
    }
    
    @Test
    public void testThrowsException() {
        DelayRequest request = new DelayRequest();
        BaseChaosRequest.throwsException(request, "java.lang.RuntimeException", "测试异常");
        
        assertEquals("throws", request.getAction());
        assertEquals("java.lang.RuntimeException", request.getException());
        assertEquals("测试异常", request.getExceptionMessage());
    }
    
    @Test
    public void testMock() {
        MockRequest request = new MockRequest();
        BaseChaosRequest.mock(request, "Mocked Value");
        
        assertEquals("mock", request.getAction());
        assertEquals("Mocked Value", request.getValue());
    }
    
    @Test
    public void testGettersAndSetters() {
        DelayRequest request = new DelayRequest();
        
        // 测试 getters（通过 setter 设置值）
        request.setTarget("jvm");
        request.setAction("delay");
        request.setTime(1000);
        request.setException("java.lang.RuntimeException");
        request.setExceptionMessage("test");
        request.setValue("value");
        
        assertEquals("jvm", request.getTarget());
        assertEquals("delay", request.getAction());
        assertEquals(Integer.valueOf(1000), request.getTime());
        assertEquals("java.lang.RuntimeException", request.getException());
        assertEquals("test", request.getExceptionMessage());
        assertEquals("value", request.getValue());
    }
    
    @Test
    public void testInheritance() {
        // 测试继承关系
        DelayRequest delayRequest = new DelayRequest();
        assertTrue(delayRequest instanceof BaseChaosRequest);
        
        MockRequest mockRequest = new MockRequest();
        assertTrue(mockRequest instanceof BaseChaosRequest);
        
        ThrowsRequest throwsRequest = new ThrowsRequest();
        assertTrue(throwsRequest instanceof BaseChaosRequest);
    }
}

