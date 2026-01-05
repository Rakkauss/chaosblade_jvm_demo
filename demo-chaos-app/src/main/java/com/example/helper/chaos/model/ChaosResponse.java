package com.example.helper.chaos.model;

import com.example.helper.chaos.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * ChaosBlade 响应对象
 * 
 * 统一的响应封装类，提供类型安全和便捷的数据提取方法
 * 
 * @param <T> result 字段的类型
 * @author rakkaus
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChaosResponse<T> {
    
    @JsonProperty("code")
    private int code;
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("result")
    private T result;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("requestId")
    private String requestId;
    
    /**
     * 默认构造函数
     */
    public ChaosResponse() {
    }
    
    /**
     * 构造函数
     * 
     * @param code 响应码
     * @param success 是否成功
     * @param result 结果对象
     * @param error 错误信息
     */
    public ChaosResponse(int code, boolean success, T result, String error) {
        this.code = code;
        this.success = success;
        this.result = result;
        this.error = error;
    }
    
    /**
     * 创建成功响应
     * 
     * @param result 结果对象
     * @param <T> 结果类型
     * @return 响应对象
     */
    public static <T> ChaosResponse<T> success(T result) {
        return new ChaosResponse<>(200, true, result, null);
    }
    
    /**
     * 创建失败响应
     * 
     * @param code 错误码
     * @param error 错误信息
     * @param <T> 结果类型
     * @return 响应对象
     */
    public static <T> ChaosResponse<T> failure(int code, String error) {
        return new ChaosResponse<>(code, false, null, error);
    }
    
    /**
     * 从 JSON 字符串解析响应（result 为 Map）
     * 
     * @param json JSON 字符串
     * @return 响应对象
     */
    @SuppressWarnings("unchecked")
    public static ChaosResponse<Map<String, Object>> fromJson(String json) {
        try {
            // 先解析为 Map，然后手动构建 ChaosResponse
            Map<String, Object> map = JsonUtil.fromJson(json, Map.class);
            ChaosResponse<Map<String, Object>> response = new ChaosResponse<>();
            response.setCode((Integer) map.getOrDefault("code", 200));
            response.setSuccess(Boolean.TRUE.equals(map.get("success")));
            response.setError((String) map.get("error"));
            response.setRequestId((String) map.get("requestId"));
            
            Object result = map.get("result");
            if (result instanceof Map) {
                Map<String, Object> resultMap = (Map<String, Object>) result;
                // 递归展开嵌套的响应结构
                Map<String, Object> unwrappedResult = unwrapNestedResponse(resultMap);
                response.setResult(unwrappedResult);
            } else if (result instanceof String) {
                // 如果 result 是字符串，尝试解析为 Map
                try {
                    Map<String, Object> parsedResult = JsonUtil.fromJson((String) result, Map.class);
                    // 递归展开嵌套的响应结构
                    Map<String, Object> unwrappedResult = unwrapNestedResponse(parsedResult);
                    response.setResult(unwrappedResult);
                } catch (Exception e) {
                    // 解析失败，设置为 null
                    response.setResult(null);
                }
            }
            
            return response;
        } catch (Exception e) {
            throw new RuntimeException("解析响应失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 递归展开嵌套的响应结构
     * 
     * @param map 可能包含嵌套响应的 Map
     * @return 展开后的 Map（不包含嵌套的响应结构）
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> unwrapNestedResponse(Map<String, Object> map) {
        // 检查是否是嵌套的响应结构（包含 code, success, result 字段）
        if (isNestedResponse(map)) {
            Object nestedResult = map.get("result");
            if (nestedResult instanceof Map) {
                // 递归展开嵌套的 result
                return unwrapNestedResponse((Map<String, Object>) nestedResult);
            } else {
                // 嵌套的 result 不是 Map，返回原 Map
                return map;
            }
        } else {
            // 不是嵌套响应，直接返回
            return map;
        }
    }
    
    /**
     * 检查 Map 是否是一个嵌套的响应结构
     * 
     * @param map 待检查的 Map
     * @return 如果是嵌套响应返回 true
     */
    private static boolean isNestedResponse(Map<String, Object> map) {
        // 如果 Map 同时包含 code, success, result 字段，说明是嵌套的响应结构
        return map.containsKey("code") && map.containsKey("success") && map.containsKey("result");
    }
    
    /**
     * 获取 experimentId（如果 result 是包含 experimentId 的对象）
     * 
     * @return experimentId，如果不存在返回 null
     */
    public String getExperimentId() {
        if (result == null) {
            return null;
        }
        
        // 如果 result 是 Map，直接获取
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = (Map<String, Object>) result;
            
            // 直接获取 experimentId（因为 fromJson 已经处理了嵌套结构）
            Object experimentId = resultMap.get("experimentId");
            if (experimentId != null) {
                return experimentId.toString();
            }
        }
        
        // 如果 result 是 String，尝试解析为 JSON
        if (result instanceof String) {
            try {
                ChaosResponse<Map<String, Object>> nestedResponse = ChaosResponse.fromJson((String) result);
                return nestedResponse.getExperimentId();
            } catch (Exception e) {
                // 解析失败，返回 null
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * 获取 result 作为指定类型
     * 
     * @param clazz 目标类型
     * @param <R> 目标类型
     * @return 转换后的对象，如果转换失败返回 null
     */
    @SuppressWarnings("unchecked")
    public <R> R getResultAs(Class<R> clazz) {
        if (result == null) {
            return null;
        }
        
        if (clazz.isInstance(result)) {
            return (R) result;
        }
        
        // 如果 result 是 String，尝试解析为 JSON
        if (result instanceof String) {
            return JsonUtil.fromJson((String) result, clazz);
        }
        
        // 如果 result 是 Map，尝试转换为目标类型
        if (result instanceof Map) {
            return JsonUtil.convertValue(result, clazz);
        }
        
        return null;
    }
    
    /**
     * 检查是否成功
     * 
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 检查是否失败
     * 
     * @return 是否失败
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * 如果失败，抛出异常
     * 
     * @throws RuntimeException 如果响应失败
     */
    public void throwIfFailure() {
        if (isFailure()) {
            throw new RuntimeException("操作失败: " + error);
        }
    }
    
    // Getters and Setters
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public T getResult() {
        return result;
    }
    
    public void setResult(T result) {
        this.result = result;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    @Override
    public String toString() {
        return "ChaosResponse{" +
                "code=" + code +
                ", success=" + success +
                ", result=" + result +
                ", error='" + error + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}

