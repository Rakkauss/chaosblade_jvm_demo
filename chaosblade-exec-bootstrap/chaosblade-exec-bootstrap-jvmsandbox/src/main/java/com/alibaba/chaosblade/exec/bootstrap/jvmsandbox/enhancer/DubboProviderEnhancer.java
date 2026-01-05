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
 * Dubbo 提供者端增强器
 * 
 * 拦截 Dubbo 提供者端的服务实现
 * 支持对服务方法进行故障注入
 * 
 * 拦截点：
 * - Dubbo 2.x: com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker.doInvoke()
 * - Dubbo 3.x: org.apache.dubbo.rpc.proxy.AbstractProxyInvoker.doInvoke()
 * 
 * 适用场景：
 * - 模拟服务处理延迟
 * - 模拟服务异常
 * - Mock 服务返回值
 * - 测试消费者容错能力
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class DubboProviderEnhancer extends DubboEnhancer {

    // Dubbo 2.x 切点
    private static final String DUBBO2_POINT_CUT_CLASS = "com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker";
    // Dubbo 3.x 切点
    private static final String DUBBO3_POINT_CUT_CLASS = "org.apache.dubbo.rpc.proxy.AbstractProxyInvoker";
    private static final String POINT_CUT_METHOD = "doInvoke";
    
    // Dubbo Invocation 相关方法
    private static final String GET_METHOD_NAME = "getMethodName";
    private static final String GET_INTERFACE_NAME = "getInterfaceName";
    private static final String GET_ATTACHMENT = "getAttachment";

    @Override
    public String getName() {
        return "dubbo-provider";
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
        // AbstractProxyInvoker.doInvoke() 方法签名：
        // protected abstract Result doInvoke(T proxy, String methodName, 
        //                                    Class<?>[] parameterTypes, Object[] arguments)
        
        // 从 AbstractProxyInvoker 对象中获取服务接口名
        Object proxyInvoker = enhancerModel.getTarget();
        if (proxyInvoker == null) {
            logger.warn("Dubbo 提供者端 AbstractProxyInvoker 为空");
            return null;
        }
        
        try {
            // 尝试获取 type 字段（服务接口类型）
            Object typeObj = getField(proxyInvoker, "type");
            if (typeObj != null && typeObj instanceof Class) {
                Class<?> serviceInterface = (Class<?>) typeObj;
                String serviceName = serviceInterface.getName();
                logger.debug("Dubbo 提供者端服务名: {}", serviceName);
                return serviceName;
            }
        } catch (Exception e) {
            logger.debug("无法从 AbstractProxyInvoker 获取服务接口: {}", e.getMessage());
        }
        
        logger.warn("无法从 Dubbo 提供者端提取服务名");
        return null;
    }

    @Override
    protected String extractMethodName(EnhancerModel enhancerModel) throws Exception {
        // doInvoke() 方法的第二个参数是 methodName
        Object[] methodArguments = enhancerModel.getArguments();
        if (methodArguments == null || methodArguments.length < 2) {
            logger.warn("Dubbo 提供者端方法参数无效");
            return null;
        }
        
        // 第二个参数是方法名
        Object methodNameObj = methodArguments[1];
        if (methodNameObj == null) {
            logger.warn("Dubbo 提供者端方法名为空");
            return null;
        }
        
        String methodName = methodNameObj.toString();
        logger.debug("Dubbo 提供者端方法名: {}", methodName);
        return methodName;
    }

    @Override
    protected int extractTimeout(EnhancerModel enhancerModel) throws Exception {
        // Provider 端的超时配置通常在 URL 中
        // 这里简化处理，使用默认超时
        logger.debug("使用默认超时: {}ms", DEFAULT_TIMEOUT);
        return DEFAULT_TIMEOUT;
    }

    @Override
    protected void doEnhance(EnhancerModel enhancerModel, 
                            String serviceName, 
                            String methodName, 
                            int timeout) throws Exception {
        if (params == null) {
            logger.warn("Dubbo 提供者端参数为空，跳过增强");
            return;
        }
        
        String action = params.get("action");
        if (action == null || action.isEmpty()) {
            logger.warn("Dubbo 提供者端 action 为空，跳过增强");
            return;
        }
        
        logger.info("Dubbo 提供者端增强: 服务={}, 方法={}, 动作={}, 超时={}ms", 
                    serviceName, methodName, action, timeout);
        
        // 根据 action 执行不同的增强逻辑
        switch (action) {
            case "delay":
                doDelay(enhancerModel, serviceName, methodName);
                break;
            case "throws":
                doThrows(enhancerModel, serviceName, methodName);
                break;
            case "mock":
                doMock(enhancerModel, serviceName, methodName);
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
        
        logger.info("Dubbo 提供者端延迟: {}ms 用于 服务={}, 方法={}", 
                    delayTime, serviceName, methodName);
        
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            logger.warn("Dubbo 提供者端延迟被中断", e);
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
        
        logger.info("Dubbo 提供者端抛出异常: {} 用于 服务={}, 方法={}", 
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
    
    /**
     * 执行 Mock 返回值
     */
    private void doMock(EnhancerModel enhancerModel, String serviceName, String methodName) throws Exception {
        String returnValue = params.get("returnValue");
        if (returnValue == null || returnValue.isEmpty()) {
            logger.warn("未指定返回值，跳过 Mock");
            return;
        }
        
        logger.info("Dubbo 提供者端 Mock: 返回值={} 用于 服务={}, 方法={}", 
                    returnValue, serviceName, methodName);
        
        // 设置返回值到 EnhancerModel
        // 注意：这里需要根据实际的返回值类型进行转换
        enhancerModel.setReturnValue(returnValue);
    }
}

