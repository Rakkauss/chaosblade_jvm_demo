package com.example.helper.chaos.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON 工具类（使用 Jackson 实现）
 * 
 * @author rakkaus
 */
public final class JsonUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    
    /** 私有构造函数 */
    private JsonUtil() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }
    
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    
    /** 字段名映射（用于将 Java 字段名映射为 JSON 键名） */
    private static final Map<String, String> FIELD_NAME_MAPPING = new HashMap<>();
    
    static {
        FIELD_NAME_MAPPING.put("className", "classname");
        FIELD_NAME_MAPPING.put("methodName", "methodname");
    }
    
    /**
     * 将对象转换为查询参数字符串
     * 
     * @param obj 待转换的对象
     * @return 查询参数字符串
     */
    public static String toQueryString(Object obj) {
        if (obj == null) {
            return "";
        }
        
        StringBuilder params = new StringBuilder();
        boolean first = true;
        
        // 获取所有字段（包括父类字段）
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            
            for (Field field : fields) {
                // 跳过静态字段
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (value == null) {
                        continue;
                    }
                    
                    if (!first) {
                        params.append("&");
                    }
                    
                    // 使用字段名映射
                    String fieldName = field.getName();
                    String paramName = FIELD_NAME_MAPPING.getOrDefault(fieldName, fieldName);
                    
                    params.append(paramName).append("=").append(value.toString());
                    first = false;
                    
                } catch (IllegalAccessException e) {
                    // 忽略无法访问的字段
                }
            }
            
            // 继续获取父类字段
            clazz = clazz.getSuperclass();
        }
        
        return params.toString();
    }
    
    /**
     * 将对象序列化为 JSON 字符串
     * 
     * @param obj 待序列化的对象，不能为 null
     * @return JSON 字符串
     * @throws IllegalArgumentException 如果对象为 null
     * @throws RuntimeException 如果序列化失败
     */
    public static String toJson(Object obj) {
        ChaosException.checkNotNull(obj, "对象");
        try {
            // 如果已经是 Map 类型，直接序列化
            if (obj instanceof Map) {
                return MAPPER.writeValueAsString(obj);
            }
            
            // 使用 Jackson 直接序列化（会自动处理所有注解：@JsonProperty、@JsonIgnore 等）
            String json = MAPPER.writeValueAsString(obj);
            
            // 应用字段名映射（className -> classname, methodName -> methodname）
            // 使用正则表达式精确匹配 JSON 键名，避免误替换值中的内容
            json = applyFieldNameMapping(json);
            
            return json;
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失败", e);
            throw ChaosException.wrap("JSON 序列化失败", e);
        }
    }
    
    /**
     * 应用字段名映射到 JSON 字符串
     * 使用正则表达式精确匹配 JSON 键名（"key":），避免误替换值中的内容
     */
    private static String applyFieldNameMapping(String json) {
        String result = json;
        for (Map.Entry<String, String> entry : FIELD_NAME_MAPPING.entrySet()) {
            String oldKey = entry.getKey();
            String newKey = entry.getValue();
            // 匹配 JSON 键名格式："oldKey": 或 "oldKey":
            // 使用 \b 确保完整单词匹配，避免部分匹配
            String pattern = "\"" + oldKey + "\"\\s*:";
            String replacement = "\"" + newKey + "\":";
            result = result.replaceAll(pattern, replacement);
        }
        return result;
    }
    
    /**
     * 将 JSON 字符串反序列化为对象
     * 
     * @param json JSON 字符串，不能为 null 或空
     * @param clazz 目标类型，不能为 null
     * @param <T> 目标类型参数
     * @return 反序列化后的对象
     * @throws IllegalArgumentException 如果参数为 null 或空
     * @throws RuntimeException 如果反序列化失败
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        ChaosException.checkNotBlank(json, "JSON 字符串");
        ChaosException.checkNotNull(clazz, "目标类型");
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("JSON 反序列化失败", e);
            throw ChaosException.wrap("JSON 反序列化失败", e);
        }
    }
    
    /**
     * 将对象转换为指定类型
     * 
     * @param obj 源对象
     * @param clazz 目标类型
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    public static <T> T convertValue(Object obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.convertValue(obj, clazz);
        } catch (Exception e) {
            log.warn("对象转换失败: {} -> {}", obj.getClass().getName(), clazz.getName());
            return null;
        }
    }
}
