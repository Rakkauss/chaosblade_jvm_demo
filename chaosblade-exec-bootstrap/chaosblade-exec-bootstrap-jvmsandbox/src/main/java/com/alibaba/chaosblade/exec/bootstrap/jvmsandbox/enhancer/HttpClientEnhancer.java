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
import com.alibaba.chaosblade.exec.common.exception.InterruptProcessException;
import com.alibaba.jvm.sandbox.api.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * HTTP 客户端增强器抽象基类
 * 
 * 参考开源版本 chaosblade-exec-jvm 的 HttpEnhancer 设计
 * 支持多种 HTTP 客户端框架（OkHttp、HttpClient、RestTemplate 等）
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public abstract class HttpClientEnhancer extends Enhancer {

    protected static final Logger logger = LoggerFactory.getLogger(HttpClientEnhancer.class);
    
    // HTTP 相关常量
    protected static final String URI_KEY = "uri";
    protected static final String METHOD_KEY = "httpMethod";
    protected static final int DEFAULT_TIMEOUT = 3000;

    @Override
    public boolean filter(EnhancerModel enhancerModel) {
        // 默认不过滤，子类可以覆盖
        return true;
    }

    @Override
    public void enhance(EnhancerModel enhancerModel) throws Exception {
        try {
            // 1. 提取 URL
            String url = extractUrl(enhancerModel);
            if (url == null || url.isEmpty()) {
                logger.warn("Cannot extract URL from HTTP client call");
                return;
            }
            
            logger.debug("HTTP Client URL: {}", url);
            
            // 2. 提取 HTTP 方法（GET、POST 等）
            String httpMethod = extractHttpMethod(enhancerModel);
            logger.debug("HTTP Method: {}", httpMethod);
            
            // 3. 获取超时时间
            int timeout = extractTimeout(enhancerModel);
            logger.debug("HTTP Timeout: {}ms", timeout);
            
            // 4. 执行具体的增强逻辑（延迟、异常、Mock 等）
            doEnhance(enhancerModel, url, httpMethod, timeout);
            
            // 5. 增加执行计数
            increaseCount();
            
        } catch (InterruptProcessException e) {
            // 重新抛出中断异常（用于立即返回）
            throw e;
        } catch (Exception e) {
            logger.error("HTTP client enhancement failed", e);
            throw e;
        }
    }

    /**
     * 提取 HTTP 请求 URL
     * 子类必须实现，根据不同的 HTTP 客户端框架提取 URL
     * 
     * @param enhancerModel 增强模型
     * @return HTTP 请求 URL
     * @throws Exception 提取失败
     */
    protected abstract String extractUrl(EnhancerModel enhancerModel) throws Exception;

    /**
     * 提取 HTTP 请求方法（GET、POST、PUT、DELETE 等）
     * 子类可以覆盖，默认返回 null
     * 
     * @param enhancerModel 增强模型
     * @return HTTP 请求方法
     * @throws Exception 提取失败
     */
    protected String extractHttpMethod(EnhancerModel enhancerModel) throws Exception {
        // 默认实现：返回 null
        // 子类可以根据具体框架提取 HTTP 方法
        return null;
    }

    /**
     * 提取 HTTP 请求超时时间
     * 子类可以覆盖，默认返回 DEFAULT_TIMEOUT
     * 
     * @param enhancerModel 增强模型
     * @return 超时时间（毫秒）
     * @throws Exception 提取失败
     */
    protected int extractTimeout(EnhancerModel enhancerModel) throws Exception {
        // 默认实现：返回默认超时时间
        // 子类可以根据具体框架提取超时配置
        return DEFAULT_TIMEOUT;
    }

    /**
     * 执行具体的增强逻辑
     * 子类可以覆盖，实现自定义的增强逻辑
     * 默认实现：不执行任何增强
     * 
     * @param enhancerModel 增强模型
     * @param url HTTP 请求 URL
     * @param httpMethod HTTP 请求方法
     * @param timeout 超时时间
     * @throws Exception 增强失败
     */
    protected void doEnhance(EnhancerModel enhancerModel, String url, 
                            String httpMethod, int timeout) throws Exception {
        // 默认实现：不执行任何增强
        // 子类可以覆盖此方法，实现具体的增强逻辑
        // 例如：延迟、异常、Mock 返回值等
    }

    /**
     * 通过反射调用方法
     * 工具方法，简化反射调用
     * 
     * @param object 目标对象
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @param args 参数值
     * @return 方法返回值
     * @throws Exception 调用失败
     */
    protected Object invokeMethod(Object object, String methodName, 
                                  Class<?>[] paramTypes, Object[] args) throws Exception {
        if (object == null) {
            return null;
        }
        Method method = object.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(object, args);
    }

    /**
     * 通过反射调用无参方法
     * 
     * @param object 目标对象
     * @param methodName 方法名
     * @return 方法返回值
     * @throws Exception 调用失败
     */
    protected Object invokeMethod(Object object, String methodName) throws Exception {
        return invokeMethod(object, methodName, new Class<?>[0], new Object[0]);
    }

    /**
     * 通过反射获取字段值
     * 
     * @param object 目标对象
     * @param fieldName 字段名
     * @return 字段值
     * @throws Exception 获取失败
     */
    protected Object getFieldValue(Object object, String fieldName) throws Exception {
        if (object == null) {
            return null;
        }
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    /**
     * 从 URL 中移除查询参数
     * 
     * @param url 完整 URL
     * @return 不含查询参数的 URL
     */
    protected String removeQueryParameters(String url) {
        if (url == null) {
            return null;
        }
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            return url.substring(0, queryIndex);
        }
        return url;
    }
}

