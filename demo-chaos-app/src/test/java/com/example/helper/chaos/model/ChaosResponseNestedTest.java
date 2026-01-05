package com.example.helper.chaos.model;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * ChaosResponse 嵌套响应结构测试
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public class ChaosResponseNestedTest {
    
    @Test
    public void testNestedResponse() {
        // 测试嵌套响应结构
        String nestedJson = "{\"code\":200,\"success\":true,\"result\":{\"code\":200,\"success\":true,\"result\":{\"experimentId\":\"6982ab78\",\"target\":\"jvm\",\"action\":\"throws\",\"status\":\"created\"}}}";
        
        ChaosResponse<Map<String, Object>> response = ChaosResponse.fromJson(nestedJson);
        
        // 验证响应解析成功
        assertTrue("响应应该成功", response.isSuccess());
        assertEquals("响应码应该是 200", 200, response.getCode());
        
        // 验证 result 不是嵌套结构，而是直接的数据
        Map<String, Object> result = response.getResult();
        assertNotNull("result 不应该为 null", result);
        
        // 验证可以直接获取 experimentId（不需要再嵌套查找）
        String experimentId = response.getExperimentId();
        assertNotNull("experimentId 不应该为 null", experimentId);
        assertEquals("experimentId 应该是 6982ab78", "6982ab78", experimentId);
        
        // 验证 result 中直接包含 experimentId（不是嵌套的）
        assertTrue("result 应该包含 experimentId", result.containsKey("experimentId"));
        assertEquals("experimentId 值应该正确", "6982ab78", result.get("experimentId"));
    }
    
    @Test
    public void testNormalResponse() {
        // 测试正常响应结构（不嵌套）
        String normalJson = "{\"code\":200,\"success\":true,\"result\":{\"experimentId\":\"12345\",\"target\":\"jvm\"}}";
        
        ChaosResponse<Map<String, Object>> response = ChaosResponse.fromJson(normalJson);
        
        assertTrue("响应应该成功", response.isSuccess());
        String experimentId = response.getExperimentId();
        assertNotNull("experimentId 不应该为 null", experimentId);
        assertEquals("experimentId 应该是 12345", "12345", experimentId);
    }
    
    @Test
    public void testDoubleNestedResponse() {
        // 测试双重嵌套响应结构
        String doubleNestedJson = "{\"code\":200,\"success\":true,\"result\":{\"code\":200,\"success\":true,\"result\":{\"code\":200,\"success\":true,\"result\":{\"experimentId\":\"abc123\"}}}}";
        
        ChaosResponse<Map<String, Object>> response = ChaosResponse.fromJson(doubleNestedJson);
        
        assertTrue("响应应该成功", response.isSuccess());
        String experimentId = response.getExperimentId();
        assertNotNull("experimentId 不应该为 null", experimentId);
        assertEquals("experimentId 应该是 abc123", "abc123", experimentId);
    }
}

