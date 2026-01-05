/*
 * Copyright 2025 The ChaosBlade Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer;

import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.EnhancerModel;

/**
 * MockEnhancer - 返回值Mock增强器
 * 
 * [改造]
 * 简化版本的返回值Mock，直接修改方法返回值
 * 
 * 参数：
 * - value: Mock返回值（字符串形式）
 * - type: 返回值类型（可选，默认String）
 *   支持：String, int, long, boolean, double, float
 * 
 * 示例：
 * value=100, type=int -> 返回整数 100
 * value=true, type=boolean -> 返回 true
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class MockEnhancer extends Enhancer {
    
    /** Mock 返回值（字符串形式） */
    private String mockValue;
    
    /** 返回值类型 */
    private String valueType;
    
    @Override
    public String getName() {
        return "mock";
    }
    
    @Override
    public void enhance(EnhancerModel model) throws Exception {
        // 1. 解析Mock参数
        parseParams();
        
        // 2. 转换为目标类型
        Object returnValue = convertValue();
        
        // 3. 设置返回值
        model.setReturnValue(returnValue);
        
        logger.info("[Mock增强器] Mock 返回值: {} (类型: {}) 用于 {}#{}",
                returnValue, valueType,
                model.getClassName(), model.getMethodName());
        
        // 4. 增加执行计数
        increaseCount();
    }
    
    @Override
    public boolean filter(EnhancerModel model) {
        // 1. 检查是否达到限制次数
        if (isLimit()) {
            logger.debug("[Mock增强器] 已达到限制次数，跳过增强");
            return false;
        }
        
        // 2. 检查是否匹配类和方法
        if (pointCut != null) {
            boolean classMatch = pointCut.matchClass(model.getClassName());
            boolean methodMatch = pointCut.matchMethod(model.getMethodName());
            
            if (!classMatch || !methodMatch) {
                logger.debug("[Mock增强器] 不匹配，跳过增强");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 解析Mock参数
     */
    private void parseParams() {
        if (params == null) {
            logger.warn("[Mock增强器] 参数为空，使用默认值: null");
            mockValue = null;
            valueType = "String";
            return;
        }
        
        // 解析返回值
        mockValue = params.get("value");
        
        // 解析类型
        valueType = params.get("type");
        if (valueType == null || valueType.isEmpty()) {
            valueType = "String";
        }
    }
    
    /**
     * 转换Mock值为目标类型
     * 
     * @return 转换后的值
     */
    private Object convertValue() {
        if (mockValue == null) {
            return null;
        }
        
        try {
            switch (valueType.toLowerCase()) {
                case "string":
                    return mockValue;
                    
                case "int":
                case "integer":
                    return Integer.parseInt(mockValue);
                    
                case "long":
                    return Long.parseLong(mockValue);
                    
                case "boolean":
                    return Boolean.parseBoolean(mockValue);
                    
                case "double":
                    return Double.parseDouble(mockValue);
                    
                case "float":
                    return Float.parseFloat(mockValue);
                    
                case "null":
                    return null;
                    
                default:
                    logger.warn("[Mock增强器] 未知类型: {}，使用 String", valueType);
                    return mockValue;
            }
        } catch (Exception e) {
            logger.error("[Mock增强器] 转换值失败: {} 到类型: {}", 
                    mockValue, valueType, e);
            return mockValue;
        }
    }
}



