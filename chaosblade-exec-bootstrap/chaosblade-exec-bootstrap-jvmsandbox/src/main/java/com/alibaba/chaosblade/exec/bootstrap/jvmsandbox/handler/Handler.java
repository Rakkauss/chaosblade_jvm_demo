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

package com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.handler;

import com.alibaba.chaosblade.exec.common.transport.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler 基类 - 简化的请求处理器
 * 
 * [改造说明]
 * 1. 替代原版的 DispatchService，使用静态分发机制
 * 2. Handler 通过构造函数自动注册到 handlers Map
 * 3. dispatch() 方法根据命令名称分发到对应的 Handler
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public abstract class Handler {

    protected static final Logger logger = LoggerFactory.getLogger(Handler.class);

    /**
     * Handler 注册表
     * Key: 命令名称 (create, destroy, status)
     * Value: Handler 实例
     */
    private static final Map<String, Handler> handlers = new ConcurrentHashMap<>();

    /**
     * 构造函数自动注册
     * 子类实例化时会自动调用，将自己注册到 handlers Map
     */
    public Handler() {
        handlers.put(getHandlerName(), this);
        logger.info("[处理器] 已注册处理器: {}", getHandlerName());
    }

    /**
     * 获取 Handler 名称（命令名称）
     * 子类必须实现，返回对应的命令名称
     * 
     * @return 命令名称，如 "create", "destroy", "status"
     */
    protected abstract String getHandlerName();

    /**
     * 处理请求
     * 子类必须实现具体的处理逻辑
     * 
     * @param params 请求参数
     * @return 响应结果
     */
    protected abstract Response handle(Map<String, String> params);

    /**
     * 静态分发方法
     * 根据命令名称找到对应的 Handler 并执行
     * 
     * @param command 命令名称
     * @param params  请求参数
     * @return 响应结果
     */
    public static Response dispatch(String command, Map<String, String> params) {
        logger.info("[处理器] 分发命令: {} 参数: {}", command, params);

        Handler handler = handlers.get(command);
        if (handler == null) {
            logger.error("[处理器] 命令未找到: {}", command);
            return Response.ofFailure(
                Response.Code.NOT_FOUND,
                "命令未找到: " + command
            );
        }

        try {
            Response response = handler.handle(params);
            logger.info("[处理器] 命令 {} 执行成功", command);
            return response;
        } catch (Exception e) {
            logger.error("[处理器] 命令 {} 执行失败", command, e);
            return Response.ofFailure(
                Response.Code.SERVER_ERROR,
                "Command execution failed: " + e.getMessage()
            );
        }
    }

    /**
     * 获取所有已注册的 Handler 名称
     * 用于调试和状态查询
     * 
     * @return Handler 名称集合
     */
    public static java.util.Set<String> getRegisteredHandlers() {
        return handlers.keySet();
    }
}

