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

import java.util.*;

/**
 * 列出所有活跃实验
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class ListHandler extends Handler {
    
    @Override
    public String getHandlerName() {
        return "list";
    }
    
    @Override
    protected Response handle(Map<String, String> params) {
        // 获取所有实验
        Map<String, Enhancer> experiments = CreateHandler.getExperiments();
        
        // 构建实验列表（模仿 zz 版本的格式）
        List<Map<String, Object>> experimentList = new ArrayList<>();
        for (Map.Entry<String, Enhancer> entry : experiments.entrySet()) {
            Enhancer enhancer = entry.getValue();
            Map<String, Object> expInfo = new HashMap<>();
            
            // 基本信息
            expInfo.put("uid", entry.getKey());
            expInfo.put("action", enhancer.getAction());
            expInfo.put("target", enhancer.getParams().get("target"));
            
            // 统计信息
            expInfo.put("effectCount", enhancer.getEffectCount());
            expInfo.put("limit", enhancer.getLimit());
            
            // 切点信息
            Map<String, String> enhancerParams = enhancer.getParams();
            if (enhancerParams != null) {
                expInfo.put("className", enhancerParams.get("classname"));
                expInfo.put("methodName", enhancerParams.get("methodname"));
                
                // 动作特定参数
                if ("delay".equals(enhancer.getAction())) {
                    expInfo.put("time", enhancerParams.get("time"));
                } else if ("throws".equals(enhancer.getAction())) {
                    expInfo.put("exception", enhancerParams.get("exception"));
                    expInfo.put("message", enhancerParams.get("message"));
                } else if ("mock".equals(enhancer.getAction())) {
                    expInfo.put("value", enhancerParams.get("value"));
                    expInfo.put("type", enhancerParams.get("type"));
                }
            }
            
            // 状态信息
            expInfo.put("status", "running");
            expInfo.put("createTime", System.currentTimeMillis());
            
            experimentList.add(expInfo);
        }
        
        // 构建响应（JSON 格式）
        StringBuilder result = new StringBuilder();
        result.append("[");
        
        for (int i = 0; i < experimentList.size(); i++) {
            Map<String, Object> exp = experimentList.get(i);
            result.append("{");
            
            // 基本信息
            result.append("\"uid\":\"").append(escapeJson(exp.get("uid"))).append("\",");
            result.append("\"action\":\"").append(escapeJson(exp.get("action"))).append("\",");
            result.append("\"target\":\"").append(escapeJson(exp.get("target"))).append("\",");
            
            // 统计信息
            result.append("\"effectCount\":").append(exp.get("effectCount")).append(",");
            result.append("\"limit\":").append(exp.get("limit")).append(",");
            
            // 切点信息
            if (exp.get("className") != null) {
                result.append("\"className\":\"").append(escapeJson(exp.get("className"))).append("\",");
            }
            if (exp.get("methodName") != null) {
                result.append("\"methodName\":\"").append(escapeJson(exp.get("methodName"))).append("\",");
            }
            
            // 动作特定参数
            if (exp.get("time") != null) {
                result.append("\"time\":\"").append(escapeJson(exp.get("time"))).append("\",");
            }
            if (exp.get("exception") != null) {
                result.append("\"exception\":\"").append(escapeJson(exp.get("exception"))).append("\",");
            }
            if (exp.get("message") != null) {
                result.append("\"message\":\"").append(escapeJson(exp.get("message"))).append("\",");
            }
            if (exp.get("value") != null) {
                result.append("\"value\":\"").append(escapeJson(exp.get("value"))).append("\",");
            }
            if (exp.get("type") != null) {
                result.append("\"type\":\"").append(escapeJson(exp.get("type"))).append("\",");
            }
            
            // 状态信息
            result.append("\"status\":\"").append(exp.get("status")).append("\",");
            result.append("\"createTime\":").append(exp.get("createTime"));
            
            result.append("}");
            if (i < experimentList.size() - 1) {
                result.append(",");
            }
        }
        
        result.append("]");
        
        logger.info("[列表处理器] 已列出 {} 个实验", experiments.size());
        
        return Response.ofSuccess(result.toString());
    }
    
    /**
     * 转义 JSON 字符串
     * 
     * @param obj 对象
     * @return 转义后的字符串
     */
    private String escapeJson(Object obj) {
        if (obj == null) {
            return "";
        }
        String str = String.valueOf(obj);
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

