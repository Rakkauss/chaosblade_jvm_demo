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
import com.alibaba.jvm.sandbox.api.ProcessControlException;

/**
 * ThrowsEnhancer - 异常增强器
 * 
 * [改造说明]
 * 简化版本的异常注入，在方法执行时抛出指定异常
 * 
 * 参数：
 * - exception: 异常类名（全限定名），如 java.lang.RuntimeException
 * - message: 异常消息（可选）
 * 
 * 示例：
 * exception=java.lang.RuntimeException, message=Chaos injected exception
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class ThrowsEnhancer extends Enhancer {
    
    /** 异常类名 */
    private String exceptionClassName;
    
    /** 异常消息 */
    private String exceptionMessage;
    
    @Override
    public String getName() {
        return "throws";
    }
    
    @Override
    public void enhance(EnhancerModel model) throws Exception {
        // 1. 解析异常参数
        parseParams();
        
        // 2. 创建并抛出异常
        logger.info("[ThrowsEnhancer] Throwing exception: {} with message: {} for {}#{}",
                exceptionClassName, exceptionMessage,
                model.getClassName(), model.getMethodName());
        
        // 3. 增加执行计数
        increaseCount();
        
        // 4. 抛出异常（使用 ProcessControlException）
        // 注意：ProcessControlException.throwThrowsImmediately() 会抛出异常，不会返回
        throwException();
    }
    
    @Override
    public boolean filter(EnhancerModel model) {
        // 1. 检查是否达到限制次数
        if (isLimit()) {
            logger.debug("[ThrowsEnhancer] Limit reached, skip enhancement");
            return false;
        }
        
        // 2. 检查是否匹配类和方法
        if (pointCut != null) {
            boolean classMatch = pointCut.matchClass(model.getClassName());
            boolean methodMatch = pointCut.matchMethod(model.getMethodName());
            
            if (!classMatch || !methodMatch) {
                logger.debug("[ThrowsEnhancer] Not matched, skip enhancement");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 解析异常参数
     */
    private void parseParams() {
        if (params == null) {
            logger.warn("[ThrowsEnhancer] Params is null, using default exception");
            exceptionClassName = "java.lang.RuntimeException";
            exceptionMessage = "Chaos injected exception";
            return;
        }
        
        // 解析异常类名
        exceptionClassName = params.get("exception");
        if (exceptionClassName == null || exceptionClassName.isEmpty()) {
            exceptionClassName = "java.lang.RuntimeException";
        }
        
        // 解析异常消息
        exceptionMessage = params.get("message");
        if (exceptionMessage == null || exceptionMessage.isEmpty()) {
            exceptionMessage = "Chaos injected exception by ChaosBlade";
        }
    }
    
    /**
     * 创建并抛出异常
     * 使用 ProcessControlException.throwThrowsImmediately() 来让 Sandbox 处理异常
     * 
     * @throws Exception ProcessControlException（必须抛出给 Sandbox）
     */
    private void throwException() throws Exception {
        try {
            // 1. 加载异常类
            Class<?> exceptionClass = Class.forName(exceptionClassName);
            
            // 2. 检查是否是 Throwable 的子类
            if (!Throwable.class.isAssignableFrom(exceptionClass)) {
                logger.error("[ThrowsEnhancer] Class {} is not a Throwable", exceptionClassName);
                throw new RuntimeException("Invalid exception class: " + exceptionClassName);
            }
            
            // 3. 创建异常实例
            Throwable throwable;
            try {
                // 尝试使用带 String 参数的构造函数
                throwable = (Throwable) exceptionClass
                        .getConstructor(String.class)
                        .newInstance(exceptionMessage);
            } catch (NoSuchMethodException e) {
                // 如果没有带 String 参数的构造函数，使用无参构造函数
                throwable = (Throwable) exceptionClass.newInstance();
            }
            
            // 4. 使用 ProcessControlException 抛出异常（让 Sandbox 处理）
            // 注意：ProcessControlException 必须向上传播，不能被捕获
            logger.info("[ThrowsEnhancer] 即将通过 ProcessControlException 抛出异常: {}", throwable.getClass().getName());
            ProcessControlException.throwThrowsImmediately(throwable);
            
        } catch (ProcessControlException e) {
            // ProcessControlException 必须向上传播，不能被捕获
            throw e;
        } catch (ClassNotFoundException e) {
            logger.error("[ThrowsEnhancer] Exception class not found: {}", exceptionClassName);
            throw new RuntimeException("Exception class not found: " + exceptionClassName, e);
        } catch (Exception e) {
            logger.error("[ThrowsEnhancer] Failed to create exception instance", e);
            throw new RuntimeException("Failed to create exception: " + exceptionClassName, e);
        }
    }
}



