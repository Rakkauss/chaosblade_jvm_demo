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

/**
 * OkHttp3 客户端增强器
 * 
 * 拦截 OkHttp3 的 RealCall.execute() 方法
 * 支持同步和异步请求
 * 
 * 参考原版 chaosblade-exec-jvm 的 Okhttp3Enhancer 实现
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class Okhttp3Enhancer extends HttpClientEnhancer {

    // OkHttp3 切点定义
    private static final String POINT_CUT_CLASS = "okhttp3.RealCall";
    private static final String POINT_CUT_METHOD = "execute";
    
    // OkHttp3 反射方法名
    private static final String GET_REQUEST = "request";
    private static final String GET_URL = "url";
    private static final String GET_METHOD = "method";
    private static final String GET_CONNECTION_TIMEOUT = "connectTimeoutMillis";
    private static final String GET_READ_TIMEOUT = "readTimeoutMillis";

    @Override
    public String getName() {
        return "okhttp3";
    }

    @Override
    public boolean filter(EnhancerModel enhancerModel) {
        // 检查是否匹配类和方法
        if (pointCut != null) {
            boolean classMatch = pointCut.matchClass(enhancerModel.getClassName());
            boolean methodMatch = pointCut.matchMethod(enhancerModel.getMethodName());
            return classMatch && methodMatch;
        }
        return true;
    }

    @Override
    protected String extractUrl(EnhancerModel enhancerModel) throws Exception {
        // 从 RealCall 对象提取 URL
        Object realCall = enhancerModel.getTarget();
        if (realCall == null) {
            logger.warn("OkHttp3 RealCall object is null");
            return null;
        }
        
        // 获取 Request 对象
        Object request = invokeMethod(realCall, GET_REQUEST);
        if (request == null) {
            logger.warn("OkHttp3 Request is null, cannot get URL");
            return null;
        }
        
        // 获取 URL 对象
        Object requestUrl = invokeMethod(request, GET_URL);
        if (requestUrl == null) {
            logger.warn("OkHttp3 URL is null, cannot get URL");
            return null;
        }
        
        // 转换为字符串并移除查询参数
        String fullUrl = requestUrl.toString();
        String path = removeQueryParameters(fullUrl);
        
        logger.debug("OkHttp3 extracted URL: {}", path);
        return path;
    }

    @Override
    protected String extractHttpMethod(EnhancerModel enhancerModel) throws Exception {
        // 从 RealCall 对象提取 HTTP 方法
        Object realCall = enhancerModel.getTarget();
        if (realCall == null) {
            return null;
        }
        
        // 获取 Request 对象
        Object request = invokeMethod(realCall, GET_REQUEST);
        if (request == null) {
            return null;
        }
        
        // 获取 HTTP 方法
        Object method = invokeMethod(request, GET_METHOD);
        return method != null ? method.toString() : null;
    }

    @Override
    protected int extractTimeout(EnhancerModel enhancerModel) throws Exception {
        // 从 RealCall 对象提取超时配置
        Object realCall = enhancerModel.getTarget();
        if (realCall == null) {
            logger.warn("OkHttp3 RealCall is null, return default timeout {}", DEFAULT_TIMEOUT);
            return DEFAULT_TIMEOUT;
        }
        
        try {
            // 获取 OkHttpClient 对象
            Object client = getFieldValue(realCall, "client");
            if (client == null) {
                logger.warn("OkHttpClient from RealCall not found, return default timeout {}", DEFAULT_TIMEOUT);
                return DEFAULT_TIMEOUT;
            }
            
            // 获取连接超时和读取超时
            Object connectionTimeoutObj = invokeMethod(client, GET_CONNECTION_TIMEOUT);
            Object readTimeoutObj = invokeMethod(client, GET_READ_TIMEOUT);
            
            int connectionTimeout = connectionTimeoutObj != null ? (Integer) connectionTimeoutObj : 0;
            int readTimeout = readTimeoutObj != null ? (Integer) readTimeoutObj : 0;
            
            int totalTimeout = connectionTimeout + readTimeout;
            logger.debug("OkHttp3 timeout: connection={}ms, read={}ms, total={}ms", 
                        connectionTimeout, readTimeout, totalTimeout);
            
            return totalTimeout > 0 ? totalTimeout : DEFAULT_TIMEOUT;
            
        } catch (Exception e) {
            logger.warn("Getting timeout from OkHttp3 client failed, return default timeout {}", 
                       DEFAULT_TIMEOUT, e);
            return DEFAULT_TIMEOUT;
        }
    }

}

