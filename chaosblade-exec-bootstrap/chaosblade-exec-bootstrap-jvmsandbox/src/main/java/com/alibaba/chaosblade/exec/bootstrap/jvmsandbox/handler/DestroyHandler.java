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
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.listener.ListenerManager;
import com.alibaba.chaosblade.exec.common.transport.Response;

import java.util.Map;

/**
 * DestroyHandler - 销毁混沌实验
 * 
 * [改造说明]
 * 简化版本的实验销毁处理器，主要功能：
 * 1. 解析实验 ID
 * 2. 从实验注册表中移除
 * 3. 取消字节码增强（后续实现）
 * 
 *
 * @author rakkaus
 * @since 1.8.0
 */
public class DestroyHandler extends Handler {

    @Override
    protected String getHandlerName() {
        return "destroy";
    }

    @Override
    protected Response handle(Map<String, String> params) {
        logger.info("[销毁处理器] 正在销毁实验，参数: {}", params);

        // 1. 获取实验 ID
        String experimentId = params.get("uid");
        
        if (experimentId == null || experimentId.isEmpty()) {
            return Response.ofFailure(
                Response.Code.ILLEGAL_PARAMETER,
                "缺少必需参数: uid (实验ID)"
            );
        }

        // 2. 从实验注册表中移除
        Map<String, Enhancer> experiments = CreateHandler.getExperiments();
        Enhancer removed = experiments.remove(experimentId);
        
        if (removed == null) {
            logger.warn("[销毁处理器] 实验未找到: {}", experimentId);
            return Response.ofFailure(
                Response.Code.NOT_FOUND,
                "实验未找到: " + experimentId
            );
        }
        
        // 3. 取消字节码增强（Phase 4）
        try {
            ListenerManager.delete(experimentId);
            logger.info("[销毁处理器] 已移除字节码增强，实验ID: {}", experimentId);
        } catch (Exception e) {
            logger.error("[销毁处理器] 移除字节码增强失败", e);
            // 即使取消增强失败，也继续执行（实验已从Map移除）
        }
        
        logger.info("[销毁处理器] 实验已移除: {}", removed);

        // 3. 构建响应
        String result = buildDestroyResult(experimentId);

        logger.info("[销毁处理器] 实验销毁成功: {}", experimentId);
        return Response.ofSuccess(result);
    }

    /**
     * 构建销毁结果信息
     * 
     * @param experimentId 实验 ID
     * @return JSON 格式的结果
     */
    private String buildDestroyResult(String experimentId) {
        return "{" +
                "\"code\":200," +
                "\"success\":true," +
                "\"result\":{" +
                "\"experimentId\":\"" + experimentId + "\"," +
                "\"status\":\"destroyed\"" +
                "}" +
                "}";
    }
}

