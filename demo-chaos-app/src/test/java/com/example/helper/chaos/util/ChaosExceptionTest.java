package com.example.helper.chaos.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * ChaosException 单元测试
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public class ChaosExceptionTest {
    
    @Test
    public void testCheckNotNull() {
        // 正常情况不应该抛出异常
        ChaosException.checkNotNull("test", "paramName");
        ChaosException.checkNotNull(new Object(), "obj");
        
        // null 应该抛出异常
        try {
            ChaosException.checkNotNull(null, "nullParam");
            fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("nullParam"));
            assertTrue(e.getMessage().contains("不能为 null"));
        }
    }
    
    @Test
    public void testCheckNotBlank() {
        // 正常情况不应该抛出异常
        ChaosException.checkNotBlank("test", "paramName");
        ChaosException.checkNotBlank("  test  ", "paramName");
        
        // null 应该抛出异常
        try {
            ChaosException.checkNotBlank(null, "nullParam");
            fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("nullParam"));
            assertTrue(e.getMessage().contains("不能为 null 或空"));
        }
        
        // 空字符串应该抛出异常
        try {
            ChaosException.checkNotBlank("", "emptyParam");
            fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("emptyParam"));
        }
        
        // 空白字符串应该抛出异常
        try {
            ChaosException.checkNotBlank("   ", "blankParam");
            fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("blankParam"));
        }
    }
    
    @Test
    public void testCheckPort() {
        // 正常端口不应该抛出异常
        ChaosException.checkPort(1);
        ChaosException.checkPort(8080);
        ChaosException.checkPort(65535);
        
        // 无效端口应该抛出异常
        try {
            ChaosException.checkPort(0);
            fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("端口必须在 1-65535 之间"));
        }
        
        try {
            ChaosException.checkPort(-1);
            fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("端口必须在 1-65535 之间"));
        }
        
        try {
            ChaosException.checkPort(65536);
            fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("端口必须在 1-65535 之间"));
        }
    }
    
    @Test
    public void testWrap() {
        Exception cause = new IOException("原始异常");
        RuntimeException wrapped = ChaosException.wrap("测试错误", cause);
        
        assertNotNull(wrapped);
        assertEquals(cause, wrapped.getCause());
        assertTrue(wrapped.getMessage().contains("测试错误"));
        assertTrue(wrapped.getMessage().contains("原始异常"));
    }
    
    @Test
    public void testRequireNotNull() {
        IllegalArgumentException exception = ChaosException.requireNotNull("paramName");
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("paramName"));
        assertTrue(exception.getMessage().contains("不能为 null"));
    }
}

