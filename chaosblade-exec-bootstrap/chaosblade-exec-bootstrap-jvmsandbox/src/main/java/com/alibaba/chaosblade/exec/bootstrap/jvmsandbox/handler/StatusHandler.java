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

import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.Enhancer;
import com.alibaba.chaosblade.exec.common.transport.Response;

import java.util.Map;
import java.util.Set;

/**
 * StatusHandler - 查询系统状态
 * 
 * [改造说明]
 * 简化版本的状态查询处理器，主要功能：
 * 1. 查询所有正在运行的实验
 * 2. 查询已注册的 Handler
 * 3. 系统健康检查
 * 
 * TODO: Phase 3 实现 Enhancer 后，添加实验列表查询
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class StatusHandler extends Handler {

    @Override
    protected String getHandlerName() {
        return "status";
    }

    @Override
    protected Response handle(Map<String, String> params) {
        logger.info("[状态处理器] 正在查询状态");

        // 1. 获取已注册的 Handler
        Set<String> registeredHandlers = Handler.getRegisteredHandlers();
        
        // 2. 获取已注册的 Enhancer
        Set<String> registeredEnhancers = Enhancer.getRegisteredEnhancers();
        
        // 3. 获取运行中的实验
        Map<String, Enhancer> experiments = CreateHandler.getExperiments();
        
        // 4. 构建状态信息
        String result = buildStatusResult(registeredHandlers, registeredEnhancers, experiments);

        logger.info("[状态处理器] 状态查询完成");
        return Response.ofSuccess(result);
    }

    /**
     * 构建状态结果信息
     * 
     * @param registeredHandlers 已注册的 Handler
     * @param registeredEnhancers 已注册的 Enhancer
     * @param experiments 运行中的实验
     * @return JSON 格式的结果
     */
    private String buildStatusResult(
            Set<String> registeredHandlers,
            Set<String> registeredEnhancers,
            Map<String, Enhancer> experiments) {
        
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("\"code\":200,");
        result.append("\"success\":true,");
        result.append("\"result\":{");
        result.append("\"version\":\"1.8.0\",");
        result.append("\"author\":\"rakkaus\",");
        result.append("\"status\":\"running\",");
        
        // 已注册的 Handler
        result.append("\"registeredHandlers\":[");
        boolean first = true;
        for (String handler : registeredHandlers) {
            if (!first) result.append(",");
            result.append("\"").append(handler).append("\"");
            first = false;
        }
        result.append("],");
        
        // 已注册的 Enhancer
        result.append("\"registeredEnhancers\":[");
        first = true;
        for (String enhancer : registeredEnhancers) {
            if (!first) result.append(",");
            result.append("\"").append(enhancer).append("\"");
            first = false;
        }
        result.append("],");
        
        // 运行中的实验
        result.append("\"experimentCount\":").append(experiments.size()).append(",");
        result.append("\"experiments\":[");
        first = true;
        for (Map.Entry<String, Enhancer> entry : experiments.entrySet()) {
            if (!first) result.append(",");
            Enhancer enhancer = entry.getValue();
            result.append("{");
            result.append("\"uid\":\"").append(entry.getKey()).append("\",");
            result.append("\"action\":\"").append(enhancer.getName()).append("\",");
            result.append("\"effectCount\":").append(enhancer.getEffectCount()).append(",");
            result.append("\"limit\":").append(enhancer.getLimit());
            result.append("}");
            first = false;
        }
        result.append("]");
        
        result.append("}");
        result.append("}");
        
        return result.toString();
    }
}

