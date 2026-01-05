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
import com.alibaba.jvm.sandbox.api.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 新增动态分发增强器
 * 
 * 根据运行时条件动态选择不同的增强器执行
 *
 * 功能：
 * - 支持根据参数条件动态选择增强器
 * - 支持多种条件匹配（等于、不等于、包含、不包含）
 * - 支持基本类型和字符串匹配
 * 
 * 参数：
 * - paramIndex: 参数索引（从 0 开始）
 * - paramValue: 期望的参数值
 * - conditionType: 条件类型（1=等于, 2=不等于, 3=包含, 4=不包含）
 * - actionType: 动作类型（1=Mock, 2=异常, 3=延迟）
 * - returnValue: Mock 返回值（actionType=1 时使用）
 * - exception: 异常类名（actionType=2 时使用）
 * - time: 延迟时间（actionType=3 时使用）
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class DynamicDispatcherEnhancer extends Enhancer {

    protected static final Logger logger = LoggerFactory.getLogger(DynamicDispatcherEnhancer.class);
    
    /**
     * 动作类型枚举
     */
    private static final int ACTION_TYPE_MOCK = 1;
    private static final int ACTION_TYPE_THROWS = 2;
    private static final int ACTION_TYPE_DELAY = 3;
    
    /**
     * 条件类型枚举
     */
    private static final int CONDITION_TYPE_EQUAL = 1;
    private static final int CONDITION_TYPE_NOT_EQUAL = 2;
    private static final int CONDITION_TYPE_CONTAIN = 3;
    private static final int CONDITION_TYPE_NOT_CONTAIN = 4;
    
    /**
     * 动态增强器映射表
     */
    private static final Map<Integer, Class<? extends Enhancer>> dynamicEnhancerMap = new HashMap<>();
    
    static {
        dynamicEnhancerMap.put(ACTION_TYPE_MOCK, MockEnhancer.class);
        dynamicEnhancerMap.put(ACTION_TYPE_THROWS, ThrowsEnhancer.class);
        dynamicEnhancerMap.put(ACTION_TYPE_DELAY, DelayEnhancer.class);
    }

    @Override
    public String getName() {
        return "dynamic-dispatcher";
    }

    @Override
    public boolean filter(EnhancerModel enhancerModel) {
        // 动态分发增强器的过滤逻辑在 enhance 方法中处理
        return true;
    }

    @Override
    public void enhance(EnhancerModel enhancerModel) throws Exception {
        try {
            // 1. 获取参数
            Map<String, String> params = getParams();
            if (params == null || params.isEmpty()) {
                logger.warn("DynamicDispatcher params is empty");
                return;
            }
            
            // 2. 获取方法参数
            Object[] methodArguments = enhancerModel.getArguments();
            if (methodArguments == null || methodArguments.length == 0) {
                logger.warn("Method arguments is empty");
                return;
            }
            
            // 3. 获取条件参数
            String paramIndexStr = params.get("paramIndex");
            String paramValue = params.get("paramValue");
            String conditionTypeStr = params.get("conditionType");
            String actionTypeStr = params.get("actionType");
            
            if (paramIndexStr == null || paramValue == null || 
                conditionTypeStr == null || actionTypeStr == null) {
                logger.warn("Missing required parameters for DynamicDispatcher");
                return;
            }
            
            int paramIndex = Integer.parseInt(paramIndexStr);
            int conditionType = Integer.parseInt(conditionTypeStr);
            int actionType = Integer.parseInt(actionTypeStr);
            
            // 4. 检查参数索引是否有效
            if (paramIndex < 0 || paramIndex >= methodArguments.length) {
                logger.warn("Invalid paramIndex: {}, method has {} arguments", 
                           paramIndex, methodArguments.length);
                return;
            }
            
            // 5. 获取目标参数值
            Object targetParam = methodArguments[paramIndex];
            if (targetParam == null) {
                logger.warn("Target parameter at index {} is null", paramIndex);
                return;
            }
            
            String targetValue = String.valueOf(targetParam);
            logger.debug("DynamicDispatcher checking: targetValue={}, expectedValue={}, conditionType={}", 
                        targetValue, paramValue, conditionType);
            
            // 6. 条件匹配
            if (!isMatch(targetValue, paramValue, conditionType)) {
                logger.debug("Condition not matched, skipping enhancement");
                return;
            }
            
            logger.info("Condition matched! Dispatching to actionType={}", actionType);
            
            // 7. 根据 actionType 分发到对应的增强器
            dispatchToEnhancer(enhancerModel, actionType, params);
            
            // 增加执行计数
            increaseCount();
            
        } catch (Exception e) {
            logger.error("DynamicDispatcher enhancement failed", e);
            throw e;
        }
    }
    
    /**
     * 条件匹配
     * 
     * @param targetValue 目标值
     * @param expectedValue 期望值
     * @param conditionType 条件类型
     * @return 是否匹配
     */
    private boolean isMatch(String targetValue, String expectedValue, int conditionType) {
        switch (conditionType) {
            case CONDITION_TYPE_EQUAL:
                return targetValue.equals(expectedValue);
            case CONDITION_TYPE_NOT_EQUAL:
                return !targetValue.equals(expectedValue);
            case CONDITION_TYPE_CONTAIN:
                return targetValue.contains(expectedValue);
            case CONDITION_TYPE_NOT_CONTAIN:
                return !targetValue.contains(expectedValue);
            default:
                logger.warn("Unknown condition type: {}", conditionType);
                return false;
        }
    }
    
    /**
     * 分发到对应的增强器
     * 
     * @param enhancerModel 增强模型
     * @param actionType 动作类型
     * @param params 参数
     * @throws Exception 增强失败
     */
    private void dispatchToEnhancer(EnhancerModel enhancerModel, 
                                    int actionType, 
                                    Map<String, String> params) throws Exception {
        // 获取对应的增强器类
        Class<? extends Enhancer> enhancerClass = dynamicEnhancerMap.get(actionType);
        if (enhancerClass == null) {
            logger.warn("Unknown action type: {}", actionType);
            return;
        }
        
        // 创建增强器实例
        Enhancer enhancer = enhancerClass.newInstance();
        
        // 设置增强器参数
        enhancer.setParams(params);
        enhancer.setUid(getUid());
        enhancer.setPointCut(getPointCut());
        
        // 执行增强
        logger.info("Dispatching to enhancer: {}", enhancerClass.getSimpleName());
        enhancer.enhance(enhancerModel);
    }
    
    /**
     * 获取动作名称
     * 
     * @param actionType 动作类型
     * @return 动作名称
     */
    private String getActionName(int actionType) {
        switch (actionType) {
            case ACTION_TYPE_MOCK:
                return "mock";
            case ACTION_TYPE_THROWS:
                return "throws";
            case ACTION_TYPE_DELAY:
                return "delay";
            default:
                return "unknown";
        }
    }

}

