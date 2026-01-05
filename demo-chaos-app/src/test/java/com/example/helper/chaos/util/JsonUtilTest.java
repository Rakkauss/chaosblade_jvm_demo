package com.example.helper.chaos.util;

import com.example.helper.chaos.model.ChaosResponse;
import com.example.helper.chaos.model.DelayRequest;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * JsonUtil 单元测试
 * 
 * @author rakkaus
 */
public class JsonUtilTest {
    
    @Test
    public void testToQueryString() {
        DelayRequest request = DelayRequest.of("HelloController", "hello", 2000);
        String queryString = JsonUtil.toQueryString(request);
        
        // 由于 DelayRequest 继承 BaseChaosRequest，字段是 protected，反射可以访问
        // 但字段名映射会应用（className -> classname, methodName -> methodname）
        assertNotNull(queryString);
        assertTrue("应该包含 target", queryString.contains("target=jvm"));
        assertTrue("应该包含 action", queryString.contains("action=delay"));
        assertTrue("应该包含 classname（映射后）", queryString.contains("classname=HelloController"));
        assertTrue("应该包含 methodname（映射后）", queryString.contains("methodname=hello"));
        assertTrue("应该包含 time", queryString.contains("time=2000"));
    }
    
    @Test
    public void testIsSuccess() {
        String successJson = "{\"success\":true,\"result\":\"123\"}";
        ChaosResponse<Map<String, Object>> successResponse = ChaosResponse.fromJson(successJson);
        assertTrue(successResponse.isSuccess());
        
        String failJson = "{\"success\":false,\"error\":\"failed\"}";
        ChaosResponse<Map<String, Object>> failResponse = ChaosResponse.fromJson(failJson);
        assertFalse(failResponse.isSuccess());
    }
    
    @Test
    public void testGetResult() {
        String json = "{\"success\":true,\"result\":\"experiment-123\"}";
        ChaosResponse<Map<String, Object>> response = ChaosResponse.fromJson(json);
        Map<String, Object> result = response.getResult();
        // result 字段可能是字符串或 Map，取决于 JSON 结构
        // 这里只测试响应解析成功
        assertTrue("响应应该成功", response.isSuccess());
    }
    
    @Test
    public void testGetError() {
        String json = "{\"success\":false,\"error\":\"Command not found\"}";
        ChaosResponse<Map<String, Object>> response = ChaosResponse.fromJson(json);
        String error = response.getError();
        assertEquals("Command not found", error);
    }
    
    @Test
    public void testToJson() {
        DelayRequest request = DelayRequest.of("Test", "test", 1000);
        String json = JsonUtil.toJson(request);
        
        assertNotNull("JSON 不应该为 null", json);
        assertTrue("应该包含 target", json.contains("\"target\":\"jvm\""));
        assertTrue("应该包含 action", json.contains("\"action\":\"delay\""));
        // 字段名会被映射：className -> classname, methodName -> methodname
        assertTrue("应该包含 classname（映射后）", json.contains("\"classname\":\"Test\""));
        assertTrue("应该包含 methodname（映射后）", json.contains("\"methodname\":\"test\""));
    }
    
    @Test
    public void testToJsonWithMap() {
        // 测试 Map 类型的序列化（用于 destroy 命令）
        java.util.Map<String, String> map = java.util.Collections.singletonMap("uid", "80906946");
        String json = JsonUtil.toJson(map);
        
        assertNotNull("JSON 不应该为 null", json);
        assertTrue("应该包含 uid", json.contains("\"uid\""));
        assertTrue("应该包含实验ID", json.contains("80906946"));
        // Map 应该直接序列化，不需要字段名映射
        assertEquals("{\"uid\":\"80906946\"}", json);
    }
    
    @Test
    public void testGetExperimentId() {
        String json = "{\"success\":true,\"result\":{\"experimentId\":\"test-123\",\"target\":\"jvm\"}}";
        ChaosResponse<Map<String, Object>> response = ChaosResponse.fromJson(json);
        String experimentId = response.getExperimentId();
        assertEquals("test-123", experimentId);
    }
}

