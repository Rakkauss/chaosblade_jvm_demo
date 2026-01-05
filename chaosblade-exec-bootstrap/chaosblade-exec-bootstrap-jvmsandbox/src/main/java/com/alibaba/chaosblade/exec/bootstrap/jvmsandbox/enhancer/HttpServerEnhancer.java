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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP 服务端增强器
 * 
 * 拦截 Servlet 的 service() 方法
 * 支持对进入应用的 HTTP 请求进行故障注入
 * 
 * 适用场景：
 * - Spring MVC Controller
 * - Servlet API
 * - Spring Boot 应用
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class HttpServerEnhancer extends Enhancer {

    protected static final Logger logger = LoggerFactory.getLogger(HttpServerEnhancer.class);
    
    // HTTP 相关常量
    protected static final String URI_KEY = "uri";
    protected static final String METHOD_KEY = "httpMethod";
    
    // Servlet 切点定义
    private static final String POINT_CUT_CLASS = "javax.servlet.http.HttpServlet";
    private static final String POINT_CUT_METHOD = "service";

    @Override
    public String getName() {
        return "httpserver";
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
    public void enhance(EnhancerModel enhancerModel) throws Exception {
        try {
            // HttpServlet.service() 方法签名：
            // protected void service(HttpServletRequest req, HttpServletResponse resp)
            
            Object[] methodArguments = enhancerModel.getArguments();
            if (methodArguments == null || methodArguments.length < 2) {
                logger.warn("HttpServlet service method arguments invalid");
                return;
            }
            
            // 第一个参数是 HttpServletRequest
            Object requestObj = methodArguments[0];
            if (!(requestObj instanceof HttpServletRequest)) {
                logger.warn("First argument is not HttpServletRequest");
                return;
            }
            
            HttpServletRequest request = (HttpServletRequest) requestObj;
            
            // 第二个参数是 HttpServletResponse
            Object responseObj = methodArguments[1];
            if (!(responseObj instanceof HttpServletResponse)) {
                logger.warn("Second argument is not HttpServletResponse");
                return;
            }
            
            HttpServletResponse response = (HttpServletResponse) responseObj;
            
            // 提取请求信息
            String requestUri = extractRequestUri(request);
            String httpMethod = extractHttpMethod(request);
            
            logger.debug("HTTP Server Request: {} {}", httpMethod, requestUri);
            
            // 执行具体的增强逻辑（延迟、异常、Mock 等）
            doEnhance(enhancerModel, request, response, requestUri, httpMethod);
            
            // 增加执行计数
            increaseCount();
            
        } catch (Exception e) {
            logger.error("HTTP server enhancement failed", e);
            throw e;
        }
    }

    /**
     * 提取请求 URI
     * 
     * @param request HttpServletRequest
     * @return 请求 URI
     */
    protected String extractRequestUri(HttpServletRequest request) {
        try {
            String requestUri = request.getRequestURI();
            logger.debug("Request URI: {}", requestUri);
            return requestUri;
        } catch (Exception e) {
            logger.warn("Extract request URI failed", e);
            return null;
        }
    }

    /**
     * 提取 HTTP 方法
     * 
     * @param request HttpServletRequest
     * @return HTTP 方法（GET、POST 等）
     */
    protected String extractHttpMethod(HttpServletRequest request) {
        try {
            String method = request.getMethod();
            logger.debug("HTTP Method: {}", method);
            return method;
        } catch (Exception e) {
            logger.warn("Extract HTTP method failed", e);
            return null;
        }
    }

    /**
     * 执行具体的增强逻辑
     * 子类可以覆盖，实现自定义的增强逻辑
     * 默认实现：不执行任何增强
     * 
     * @param enhancerModel 增强模型
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param requestUri 请求 URI
     * @param httpMethod HTTP 方法
     * @throws Exception 增强失败
     */
    protected void doEnhance(EnhancerModel enhancerModel, 
                            HttpServletRequest request,
                            HttpServletResponse response,
                            String requestUri, 
                            String httpMethod) throws Exception {
        // 默认实现：不执行任何增强
        // 子类可以覆盖此方法，实现具体的增强逻辑
        // 例如：延迟、异常、Mock 返回值等
    }

    /**
     * 检查请求 URI 是否匹配
     * 
     * @param requestUri 请求 URI
     * @param pattern 匹配模式（支持通配符）
     * @return 是否匹配
     */
    protected boolean matchUri(String requestUri, String pattern) {
        if (requestUri == null || pattern == null) {
            return false;
        }
        
        // 简单的通配符匹配
        // 例如：/api/* 匹配 /api/users、/api/orders 等
        if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return requestUri.startsWith(prefix);
        }
        
        // 精确匹配
        return requestUri.equals(pattern);
    }

    /**
     * 检查 HTTP 方法是否匹配
     * 
     * @param httpMethod HTTP 方法
     * @param expectedMethod 期望的方法
     * @return 是否匹配
     */
    protected boolean matchMethod(String httpMethod, String expectedMethod) {
        if (httpMethod == null || expectedMethod == null) {
            return false;
        }
        
        // 不区分大小写
        return httpMethod.equalsIgnoreCase(expectedMethod);
    }

}

