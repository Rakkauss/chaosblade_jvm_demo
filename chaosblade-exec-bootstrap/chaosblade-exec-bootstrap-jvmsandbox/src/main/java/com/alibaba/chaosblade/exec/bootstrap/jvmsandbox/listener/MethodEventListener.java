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

package com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.listener;

import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.EnhancerModel;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.Enhancer;
import com.alibaba.chaosblade.exec.common.util.ReflectUtil;
import com.alibaba.jvm.sandbox.api.ProcessControlException;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * MethodEventListener - 方法事件监听器
 * 
 * [改造说明]
 * 简化版本的事件监听器，主要功能：
 * 1. 监听方法调用前事件（BeforeEvent）
 * 2. 构建 EnhancerModel（执行上下文）
 * 3. 调用 Enhancer 的 filter() 和 enhance() 方法
 * 4. 处理返回值 Mock（通过 ProcessControlException）
 * 
 * 简化点：
 * - 只处理 BeforeEvent
 * - 移除 LineEvent 和 ReturnEvent 支持
 * - 移除复杂的 ThreadLocal 缓存
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class MethodEventListener implements EventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodEventListener.class);
    
    /** 关联的 Enhancer */
    private final Enhancer enhancer;
    
    public MethodEventListener(Enhancer enhancer) {
        this.enhancer = enhancer;
    }
    
    @Override
    public void onEvent(Event event) throws Throwable {
        // 简化版本：只处理 BeforeEvent
        if (!(event instanceof BeforeEvent)) {
            return;
        }
        
        BeforeEvent beforeEvent = (BeforeEvent) event;
        
        try {
            // 1. 检查是否达到限制次数
            if (enhancer.isLimit()) {
                logger.debug("[方法事件监听器] 增强器已达到限制次数: {}", enhancer.getUid());
                return;
            }
            
            // 2. 构建 EnhancerModel
            EnhancerModel model = buildEnhancerModel(beforeEvent);
            if (model == null) {
                return;
            }
            
            // 3. 过滤：是否应该执行增强
            if (!enhancer.filter(model)) {
                logger.debug("[方法事件监听器] 增强器被过滤: {}", enhancer.getUid());
                return;
            }
            
            // 4. 执行增强（内部会调用 increaseCount()）
            enhancer.enhance(model);
            
            // 5. 处理返回值 Mock（如果设置了返回值）
            handleReturnValue(model);
            
        } catch (ProcessControlException e) {
            // ProcessControlException 是 Sandbox 的控制异常，必须抛出
            // 用于控制方法执行流程（如立即返回、抛出异常等）
            throw e;
        } catch (Throwable e) {
            logger.error("[方法事件监听器] 增强过程中发生错误", e);
            // 其他异常不抛出，避免影响目标方法执行
        }
    }
    
    /**
     * 构建 EnhancerModel
     * 
     * @param beforeEvent BeforeEvent 事件
     * @return EnhancerModel 实例
     */
    private EnhancerModel buildEnhancerModel(BeforeEvent beforeEvent) {
        try {
            // 1. 获取类
            Class<?> clazz;
            if (beforeEvent.target == null) {
                clazz = beforeEvent.javaClassLoader.loadClass(beforeEvent.javaClassName);
            } else {
                clazz = beforeEvent.target.getClass();
            }
            
            // 2. 获取方法
            Method method;
            try {
                method = ReflectUtil.getMethod(
                    clazz, 
                    beforeEvent.javaMethodDesc, 
                    beforeEvent.javaMethodName
                );
            } catch (NoSuchMethodException e) {
                logger.warn("[方法事件监听器] 方法未找到: {}.{}", 
                    beforeEvent.javaClassName, beforeEvent.javaMethodName);
                return null;
            }
            
            // 3. 构建 EnhancerModel
            EnhancerModel model = new EnhancerModel();
            model.setTarget(beforeEvent.target);
            model.setMethod(method);
            model.setArguments(beforeEvent.argumentArray);
            model.setClassLoader(beforeEvent.javaClassLoader);
            model.setClassName(beforeEvent.javaClassName);
            model.setMethodName(beforeEvent.javaMethodName);
            
            return model;
            
        } catch (Exception e) {
            logger.error("[方法事件监听器] 构建增强模型失败", e);
            return null;
        }
    }
    
    /**
     * 处理返回值 Mock
     * 如果 Enhancer 设置了返回值，通过 ProcessControlException 返回
     * 
     * @param model EnhancerModel
     * @throws Throwable ProcessControlException
     */
    private void handleReturnValue(EnhancerModel model) throws Throwable {
        // 使用 hasReturnValue() 而不是检查 returnValue != null
        // 这样可以正确处理 null 返回值的情况
        logger.info("[方法事件监听器] 调用处理返回值方法, hasReturnValue={}", model.hasReturnValue());
        
        if (model.hasReturnValue()) {
            Object returnValue = model.getReturnValue();
            logger.info("[方法事件监听器] Mock 返回值: {} 用于 {}.{}",
                returnValue, model.getClassName(), model.getMethodName());
            logger.info("[方法事件监听器] 即将抛出 ProcessControlException.throwReturnImmediately");
            
            // 通过 ProcessControlException 立即返回
            ProcessControlException.throwReturnImmediately(returnValue);
            
            logger.info("[方法事件监听器] 这行代码不应该被执行到！");
        } else {
            logger.info("[方法事件监听器] 未设置返回值，跳过 Mock");
        }
    }
}

