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

import java.net.URI;

/**
 * Spring RestTemplate 客户端增强器
 * 
 * 拦截 RestTemplate 的 doExecute() 方法
 * 支持所有 REST 操作（GET、POST、PUT、DELETE 等）
 *
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class RestTemplateEnhancer extends HttpClientEnhancer {

    // RestTemplate 切点定义
    private static final String POINT_CUT_CLASS = "org.springframework.web.client.RestTemplate";
    private static final String POINT_CUT_METHOD = "doExecute";
    
    // RestTemplate 反射方法名
    private static final String GET_REQUEST_FACTORY = "getRequestFactory";
    private static final String GET_CONNECT_TIMEOUT = "getConnectTimeout";
    private static final String GET_READ_TIMEOUT = "getReadTimeout";

    @Override
    public String getName() {
        return "resttemplate";
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
        // RestTemplate.doExecute() 方法签名：
        // protected <T> T doExecute(URI url, HttpMethod method, 
        //                          RequestCallback requestCallback, 
        //                          ResponseExtractor<T> responseExtractor)
        
        Object[] methodArguments = enhancerModel.getArguments();
        if (methodArguments == null || methodArguments.length == 0) {
            logger.warn("RestTemplate method arguments is null or empty");
            return null;
        }
        
        // 第一个参数是 URI 对象
        Object uriObject = methodArguments[0];
        if (uriObject == null) {
            logger.warn("RestTemplate URI is null, cannot get URL");
            return null;
        }
        
        // 转换为 URI 并提取路径
        URI uri = (URI) uriObject;
        String fullUrl = uri.toString();
        String path = removeQueryParameters(fullUrl);
        
        logger.debug("RestTemplate extracted URL: {}", path);
        return path;
    }

    @Override
    protected String extractHttpMethod(EnhancerModel enhancerModel) throws Exception {
        // RestTemplate.doExecute() 方法签名中第二个参数是 HttpMethod
        Object[] methodArguments = enhancerModel.getArguments();
        if (methodArguments == null || methodArguments.length < 2) {
            return null;
        }
        
        // 第二个参数是 HttpMethod 对象
        Object httpMethodObject = methodArguments[1];
        if (httpMethodObject == null) {
            return null;
        }
        
        // HttpMethod 是一个枚举，直接 toString() 即可
        // 例如：GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE
        String method = httpMethodObject.toString();
        logger.debug("RestTemplate HTTP method: {}", method);
        return method;
    }

    @Override
    protected int extractTimeout(EnhancerModel enhancerModel) throws Exception {
        // 从 RestTemplate 对象提取超时配置
        Object restTemplate = enhancerModel.getTarget();
        if (restTemplate == null) {
            logger.warn("RestTemplate object is null, return default timeout {}", DEFAULT_TIMEOUT);
            return DEFAULT_TIMEOUT;
        }
        
        try {
            // 获取 ClientHttpRequestFactory
            Object requestFactory = invokeMethod(restTemplate, GET_REQUEST_FACTORY);
            if (requestFactory == null) {
                logger.warn("ClientHttpRequestFactory from RestTemplate not found, return default timeout {}", 
                           DEFAULT_TIMEOUT);
                return DEFAULT_TIMEOUT;
            }
            
            // 尝试获取超时配置
            // 不同的 RequestFactory 实现类可能有不同的方法
            // 常见的有：SimpleClientHttpRequestFactory, HttpComponentsClientHttpRequestFactory
            int connectTimeout = 0;
            int readTimeout = 0;
            
            try {
                // 尝试获取连接超时
                Object connectTimeoutObj = invokeMethod(requestFactory, GET_CONNECT_TIMEOUT);
                if (connectTimeoutObj != null) {
                    connectTimeout = (Integer) connectTimeoutObj;
                }
            } catch (Exception e) {
                logger.debug("Cannot get connect timeout from RequestFactory: {}", e.getMessage());
            }
            
            try {
                // 尝试获取读取超时
                Object readTimeoutObj = invokeMethod(requestFactory, GET_READ_TIMEOUT);
                if (readTimeoutObj != null) {
                    readTimeout = (Integer) readTimeoutObj;
                }
            } catch (Exception e) {
                logger.debug("Cannot get read timeout from RequestFactory: {}", e.getMessage());
            }
            
            int totalTimeout = connectTimeout + readTimeout;
            logger.debug("RestTemplate timeout: connection={}ms, read={}ms, total={}ms", 
                        connectTimeout, readTimeout, totalTimeout);
            
            return totalTimeout > 0 ? totalTimeout : DEFAULT_TIMEOUT;
            
        } catch (Exception e) {
            logger.warn("Getting timeout from RestTemplate failed, return default timeout {}", 
                       DEFAULT_TIMEOUT, e);
            return DEFAULT_TIMEOUT;
        }
    }

}

