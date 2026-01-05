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

import java.lang.reflect.Method;

/**
 * Dubbo RPC 增强器基类
 * 
 * 提供 Dubbo RPC 调用的通用增强框架
 * 支持 Dubbo 2.x 和 3.x 版本
 * 
 * Dubbo 调用链路：
 * Consumer -> Proxy -> Filter -> Invoker -> Protocol -> Provider
 * 
 * 拦截点：
 * - Consumer 端：拦截 Proxy 调用，模拟调用失败、延迟等
 * - Provider 端：拦截 Service 实现，模拟服务异常、延迟等
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public abstract class DubboEnhancer extends Enhancer {

    protected static final Logger logger = LoggerFactory.getLogger(DubboEnhancer.class);
    
    // Dubbo 相关常量
    protected static final String SERVICE_KEY = "service";
    protected static final String METHOD_KEY = "method";
    protected static final String TIMEOUT_KEY = "timeout";
    protected static final String VERSION_KEY = "version";
    protected static final String GROUP_KEY = "group";
    
    // 默认超时时间（毫秒）
    protected static final int DEFAULT_TIMEOUT = 3000;

    @Override
    public boolean filter(EnhancerModel enhancerModel) {
        // 默认不过滤，子类可以覆盖
        return true;
    }

    /**
     * 提取 Dubbo 服务接口名
     * 
     * @param enhancerModel 增强模型
     * @return 服务接口名
     * @throws Exception 提取失败
     */
    protected abstract String extractServiceName(EnhancerModel enhancerModel) throws Exception;

    /**
     * 提取 Dubbo 方法名
     * 
     * @param enhancerModel 增强模型
     * @return 方法名
     * @throws Exception 提取失败
     */
    protected abstract String extractMethodName(EnhancerModel enhancerModel) throws Exception;

    /**
     * 提取 Dubbo 超时配置
     * 
     * @param enhancerModel 增强模型
     * @return 超时时间（毫秒）
     * @throws Exception 提取失败
     */
    protected abstract int extractTimeout(EnhancerModel enhancerModel) throws Exception;

    /**
     * 执行具体的增强逻辑
     * 
     * @param enhancerModel 增强模型
     * @param serviceName 服务接口名
     * @param methodName 方法名
     * @param timeout 超时时间
     * @throws Exception 增强失败
     */
    protected abstract void doEnhance(EnhancerModel enhancerModel, 
                                     String serviceName,
                                     String methodName, 
                                     int timeout) throws Exception;

    @Override
    public void enhance(EnhancerModel enhancerModel) throws Exception {
        try {
            // 提取 Dubbo 调用信息
            String serviceName = extractServiceName(enhancerModel);
            String methodName = extractMethodName(enhancerModel);
            int timeout = extractTimeout(enhancerModel);
            
            logger.debug("Dubbo RPC Call: service={}, method={}, timeout={}ms", 
                        serviceName, methodName, timeout);
            
            // 执行具体的增强逻辑
            doEnhance(enhancerModel, serviceName, methodName, timeout);
            
            // 增加执行计数
            increaseCount();
            
        } catch (Exception e) {
            logger.error("Dubbo RPC enhancement failed", e);
            throw e;
        }
    }

    /**
     * 通过反射调用对象的方法
     * 
     * @param target 目标对象
     * @param methodName 方法名
     * @param parameterTypes 参数类型
     * @param args 参数值
     * @return 方法返回值
     * @throws Exception 反射调用失败
     */
    protected Object invokeMethod(Object target, String methodName, 
                                  Class<?>[] parameterTypes, Object... args) throws Exception {
        if (target == null) {
            logger.warn("Target object is null, cannot invoke method: {}", methodName);
            return null;
        }
        
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (NoSuchMethodException e) {
            logger.debug("Method not found: {}.{}()", target.getClass().getName(), methodName);
            return null;
        } catch (Exception e) {
            logger.warn("Invoke method failed: {}.{}()", target.getClass().getName(), methodName, e);
            throw e;
        }
    }

    /**
     * 通过反射调用对象的无参方法
     * 
     * @param target 目标对象
     * @param methodName 方法名
     * @return 方法返回值
     * @throws Exception 反射调用失败
     */
    protected Object invokeMethod(Object target, String methodName) throws Exception {
        return invokeMethod(target, methodName, new Class<?>[0]);
    }

    /**
     * 通过反射获取对象的字段值
     * 
     * @param target 目标对象
     * @param fieldName 字段名
     * @return 字段值
     * @throws Exception 反射获取失败
     */
    protected Object getField(Object target, String fieldName) throws Exception {
        if (target == null) {
            logger.warn("Target object is null, cannot get field: {}", fieldName);
            return null;
        }
        
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException e) {
            logger.debug("Field not found: {}.{}", target.getClass().getName(), fieldName);
            return null;
        } catch (Exception e) {
            logger.warn("Get field failed: {}.{}", target.getClass().getName(), fieldName, e);
            throw e;
        }
    }

}

