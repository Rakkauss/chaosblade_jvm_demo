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
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.matcher.FilterUtil;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.matcher.PointCut;
import com.alibaba.chaosblade.exec.common.transport.Response;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CreateHandler - 创建混沌实验
 * 
 * [改造说明]
 * 简化版本的实验创建处理器，主要功能：
 * 1. 解析实验参数（target, action, matchers）
 * 2. 生成实验 ID
 * 3. 创建并注册 Enhancer
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class CreateHandler extends Handler {
    
    /** 实验注册表：experimentId -> Enhancer */
    private static final Map<String, Enhancer> experiments = new ConcurrentHashMap<>();

    @Override
    protected String getHandlerName() {
        return "create";
    }
    
    /**
     * 获取所有实验
     * 
     * @return 实验注册表
     */
    public static Map<String, Enhancer> getExperiments() {
        return experiments;
    }

    @Override
    protected Response handle(Map<String, String> params) {
        logger.info("[创建处理器] 正在创建实验，参数: {}", params);

        try {
            // 1. 参数验证
            String target = params.get("target");
            String action = params.get("action");
            
            if (target == null || target.isEmpty()) {
                return Response.ofFailure(
                    Response.Code.ILLEGAL_PARAMETER,
                    "Missing required parameter: target"
                );
            }
            
            if (action == null || action.isEmpty()) {
                return Response.ofFailure(
                    Response.Code.ILLEGAL_PARAMETER,
                    "Missing required parameter: action"
                );
            }

            // 2. 生成实验 ID
            String experimentId = generateExperimentId();
            params.put("uid", experimentId); // 添加uid到参数中
            logger.info("[创建处理器] 已生成实验 ID: {}", experimentId);

            // 3. 创建 Enhancer
            Class<? extends Enhancer> enhancerClass = Enhancer.getEnhancer(target);
            if (enhancerClass == null) {
                // 如果 target 找不到，尝试使用 action
                enhancerClass = Enhancer.getEnhancer(action);
                if (enhancerClass == null) {
                    return Response.ofFailure(
                        Response.Code.ILLEGAL_PARAMETER,
                        "Unknown target/action: " + target + "/" + action
                    );
                }
                logger.info("[创建处理器] 使用基于 action 的增强器: {}", action);
            } else {
                logger.info("[创建处理器] 使用基于 target 的增强器: {}", target);
            }
            
            Enhancer enhancer = enhancerClass.newInstance();
            
            // 4. 设置 Enhancer 属性
            enhancer.setUid(experimentId);
            enhancer.setParams(params);
            
            // 5. 创建 PointCut
            String className = params.get("classname");
            String methodName = params.get("methodname");
            PointCut pointCut = new PointCut(className, methodName);
            enhancer.setPointCut(pointCut);
            
            // 6. 设置限制次数
            String limitStr = params.get("limit");
            if (limitStr != null && !limitStr.isEmpty()) {
                try {
                    int limit = Integer.parseInt(limitStr);
                    enhancer.setLimit(limit);
                } catch (NumberFormatException e) {
                    logger.warn("[创建处理器] 无效的限制次数参数: {}", limitStr);
                }
            }
            
            // 7. 注册实验到 Map
            experiments.put(experimentId, enhancer);
            logger.info("[创建处理器] 增强器已注册: {}", enhancer);

            // 8. 注册字节码增强（Phase 4）
            // 注意：watch 操作会扫描所有已加载的类，可能耗时较长（几十秒甚至更久）
            // 使用异步执行，避免阻塞 HTTP 响应
            // 立即返回响应，不等待 watch 完成
            final Enhancer finalEnhancer = enhancer;
            final PointCut finalPointCut = pointCut;
            final String finalExperimentId = experimentId;  // 确保线程安全
            
            Thread watchThread = new Thread(() -> {
                try {
                    logger.info("[创建处理器] 开始异步字节码增强，实验 ID: {}", finalExperimentId);
                    com.alibaba.jvm.sandbox.api.filter.Filter filter = FilterUtil.createFilter(finalPointCut);
                    ListenerManager.watch(finalEnhancer, filter);
                    logger.info("[创建处理器] 字节码增强注册成功，实验 ID: {}", finalExperimentId);
                } catch (Exception e) {
                    logger.error("[创建处理器] 注册字节码增强失败，实验 ID: {}", finalExperimentId, e);
                    // 即使字节码增强失败，实验已创建，不影响返回
                }
            }, "ChaosBlade-Watch-" + experimentId);
            
            // 设置为守护线程，避免阻止 JVM 退出
            watchThread.setDaemon(true);
            watchThread.start();
            
            logger.info("[创建处理器] 字节码增强注册已启动（异步），实验 ID: {}", experimentId);

            // 9. 构建响应
            String result = buildExperimentResult(experimentId, target, action, params);

            logger.info("[创建处理器] 实验创建成功: {}", experimentId);
            return Response.ofSuccess(result);
            
        } catch (Exception e) {
            logger.error("[创建处理器] 创建实验失败", e);
            return Response.ofFailure(
                Response.Code.SERVER_ERROR,
                "Failed to create experiment: " + e.getMessage()
            );
        }
    }

    /**
     * 生成实验 ID
     * 格式：UUID 的前 8 位
     * 
     * @return 实验 ID
     */
    private String generateExperimentId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 构建实验结果信息
     * 
     * @param experimentId 实验 ID
     * @param target       目标类型
     * @param action       操作类型
     * @param params       所有参数
     * @return JSON 格式的结果
     */
    private String buildExperimentResult(
            String experimentId,
            String target,
            String action,
            Map<String, String> params) {
        
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("\"code\":200,");
        result.append("\"success\":true,");
        result.append("\"result\":{");
        result.append("\"experimentId\":\"").append(experimentId).append("\",");
        result.append("\"target\":\"").append(target).append("\",");
        result.append("\"action\":\"").append(action).append("\",");
        result.append("\"status\":\"created\"");
        
        // 添加其他重要参数
        if (params.containsKey("classname")) {
            result.append(",\"classname\":\"").append(params.get("classname")).append("\"");
        }
        if (params.containsKey("methodname")) {
            result.append(",\"methodname\":\"").append(params.get("methodname")).append("\"");
        }
        
        result.append("}");
        result.append("}");
        
        return result.toString();
    }
}

