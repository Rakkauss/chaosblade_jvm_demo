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
 * Dubbo 消费者端增强器
 * 
 * 拦截 Dubbo 消费者端的 RPC 调用
 * 支持对远程服务调用进行故障注入
 * 
 * 拦截点：
 * - Dubbo 2.x: com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker.invoke()
 * - Dubbo 3.x: org.apache.dubbo.rpc.proxy.AbstractProxyInvoker.invoke()
 * 
 * 适用场景：
 * - 模拟远程服务调用延迟
 * - 模拟远程服务调用异常
 * - 测试服务降级逻辑
 * - 测试熔断器功能
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class DubboConsumerEnhancer extends DubboEnhancer {

    // Dubbo 2.x 切点
    private static final String DUBBO2_POINT_CUT_CLASS = "com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker";
    // Dubbo 3.x 切点
    private static final String DUBBO3_POINT_CUT_CLASS = "org.apache.dubbo.rpc.proxy.AbstractProxyInvoker";
    private static final String POINT_CUT_METHOD = "invoke";
    
    // Dubbo Invocation 相关方法
    private static final String GET_METHOD_NAME = "getMethodName";
    private static final String GET_INTERFACE_NAME = "getInterfaceName";
    private static final String GET_ATTACHMENT = "getAttachment";

    @Override
    public String getName() {
        return "dubbo-consumer";
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
    protected String extractServiceName(EnhancerModel enhancerModel) throws Exception {
        // AbstractProxyInvoker.invoke() 方法签名：
        // public Result invoke(Invocation invocation)
        
        Object[] methodArguments = enhancerModel.getArguments();
        if (methodArguments == null || methodArguments.length == 0) {
            logger.warn("Dubbo 消费者端方法参数为空");
            return null;
        }
        
        // 第一个参数是 Invocation 对象
        Object invocation = methodArguments[0];
        if (invocation == null) {
            logger.warn("Dubbo Invocation 为空");
            return null;
        }
        
        // 调用 Invocation.getInterfaceName() 获取服务接口名
        Object interfaceName = invokeMethod(invocation, GET_INTERFACE_NAME);
        if (interfaceName == null) {
            logger.warn("无法从 Invocation 获取接口名");
            return null;
        }
        
        String serviceName = interfaceName.toString();
        logger.debug("Dubbo 消费者端服务名: {}", serviceName);
        return serviceName;
    }

    @Override
    protected String extractMethodName(EnhancerModel enhancerModel) throws Exception {
        Object[] methodArguments = enhancerModel.getArguments();
        if (methodArguments == null || methodArguments.length == 0) {
            return null;
        }
        
        // 第一个参数是 Invocation 对象
        Object invocation = methodArguments[0];
        if (invocation == null) {
            return null;
        }
        
        // 调用 Invocation.getMethodName() 获取方法名
        Object methodName = invokeMethod(invocation, GET_METHOD_NAME);
        if (methodName == null) {
            logger.warn("无法从 Invocation 获取方法名");
            return null;
        }
        
        String method = methodName.toString();
        logger.debug("Dubbo 消费者端方法名: {}", method);
        return method;
    }

    @Override
    protected int extractTimeout(EnhancerModel enhancerModel) throws Exception {
        Object[] methodArguments = enhancerModel.getArguments();
        if (methodArguments == null || methodArguments.length == 0) {
            return DEFAULT_TIMEOUT;
        }
        
        // 第一个参数是 Invocation 对象
        Object invocation = methodArguments[0];
        if (invocation == null) {
            return DEFAULT_TIMEOUT;
        }
        
        try {
            // 尝试从 Invocation 的 attachment 中获取 timeout
            Object timeoutObj = invokeMethod(invocation, GET_ATTACHMENT, 
                                            new Class<?>[]{String.class}, TIMEOUT_KEY);
            if (timeoutObj != null) {
                int timeout = Integer.parseInt(timeoutObj.toString());
                logger.debug("Dubbo 消费者端超时: {}ms", timeout);
                return timeout;
            }
        } catch (Exception e) {
            logger.debug("无法从 Invocation attachment 获取超时: {}", e.getMessage());
        }
        
        logger.debug("使用默认超时: {}ms", DEFAULT_TIMEOUT);
        return DEFAULT_TIMEOUT;
    }

    @Override
    protected void doEnhance(EnhancerModel enhancerModel, 
                            String serviceName, 
                            String methodName, 
                            int timeout) throws Exception {
        if (params == null) {
            logger.warn("Dubbo 消费者端参数为空，跳过增强");
            return;
        }
        
        String action = params.get("action");
        if (action == null || action.isEmpty()) {
            logger.warn("Dubbo 消费者端 action 为空，跳过增强");
            return;
        }
        
        logger.info("Dubbo 消费者端增强: 服务={}, 方法={}, 动作={}, 超时={}ms", 
                    serviceName, methodName, action, timeout);
        
        // 根据 action 执行不同的增强逻辑
        switch (action) {
            case "delay":
                doDelay(enhancerModel, serviceName, methodName);
                break;
            case "throws":
                doThrows(enhancerModel, serviceName, methodName);
                break;
            default:
                logger.warn("未知动作: {}，跳过增强", action);
        }
    }
    
    /**
     * 执行延迟注入
     */
    private void doDelay(EnhancerModel enhancerModel, String serviceName, String methodName) throws Exception {
        String timeStr = params.get("time");
        if (timeStr == null || timeStr.isEmpty()) {
            logger.warn("未指定延迟时间，跳过延迟");
            return;
        }
        
        long delayTime;
        try {
            delayTime = Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            logger.warn("无效的延迟时间: {}，跳过延迟", timeStr);
            return;
        }
        
        logger.info("Dubbo 消费者端延迟: {}ms 用于 服务={}, 方法={}", 
                    delayTime, serviceName, methodName);
        
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            logger.warn("Dubbo 消费者端延迟被中断", e);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 执行异常注入
     */
    private void doThrows(EnhancerModel enhancerModel, String serviceName, String methodName) throws Exception {
        String exceptionClass = params.get("exception");
        String exceptionMessage = params.get("exceptionMessage");
        
        if (exceptionClass == null || exceptionClass.isEmpty()) {
            logger.warn("未指定异常类，跳过异常注入");
            return;
        }
        
        logger.info("Dubbo 消费者端抛出异常: {} 用于 服务={}, 方法={}", 
                    exceptionClass, serviceName, methodName);
        
        // 创建异常对象
        Exception exception;
        try {
            Class<?> exceptionType = Class.forName(exceptionClass);
            if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                exception = (Exception) exceptionType.getConstructor(String.class)
                    .newInstance(exceptionMessage);
            } else {
                exception = (Exception) exceptionType.getConstructor().newInstance();
            }
        } catch (Exception e) {
            logger.error("创建异常失败: {}", exceptionClass, e);
            exception = new RuntimeException("ChaosBlade: " + exceptionClass + 
                (exceptionMessage != null ? ": " + exceptionMessage : ""));
        }
        
        throw exception;
    }
}

