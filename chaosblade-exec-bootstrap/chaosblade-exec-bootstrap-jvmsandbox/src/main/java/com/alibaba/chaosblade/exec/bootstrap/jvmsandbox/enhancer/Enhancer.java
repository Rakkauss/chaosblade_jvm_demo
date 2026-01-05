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
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.matcher.PointCut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhancer - 增强器基类
 * 
 * [改造说明]
 * 简化版本的增强器基类，主要功能：
 * 1. 增强器注册机制（静态注册）
 * 2. 基本属性管理（uid、pointCut、params）
 * 3. 抽象方法定义（enhance、filter）
 * 
 * 简化点：
 * - 移除了复杂的Flag系统
 * - 移除了Event.Type复杂判断
 * - 使用简单的Map存储参数
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public abstract class Enhancer {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /** 增强器注册表 */
    private static final Map<String, Class<? extends Enhancer>> enhancerRegistry = 
        new ConcurrentHashMap<>();
    
    /** 实验唯一ID */
    protected String uid;
    
    /** 匹配规则 */
    protected PointCut pointCut;
    
    /** 实验参数 */
    protected Map<String, String> params;
    
    /** 限制生效次数（0表示不限制） */
    protected int limit = 0;
    
    /** 已执行次数 */
    protected AtomicInteger effectCount = new AtomicInteger(0);
    
    /**
     * 注册增强器
     * 
     * @param name 增强器名称
     * @param enhancerClass 增强器类
     */
    public static void register(String name, Class<? extends Enhancer> enhancerClass) {
        enhancerRegistry.put(name, enhancerClass);
        LoggerFactory.getLogger(Enhancer.class)
            .info("[Enhancer] Registered enhancer: {} -> {}", name, enhancerClass.getSimpleName());
    }
    
    /**
     * 获取增强器类
     * 
     * @param name 增强器名称
     * @return 增强器类
     */
    public static Class<? extends Enhancer> getEnhancer(String name) {
        return enhancerRegistry.get(name);
    }
    
    /**
     * 获取所有已注册的增强器名称
     * 
     * @return 增强器名称集合
     */
    public static java.util.Set<String> getRegisteredEnhancers() {
        return enhancerRegistry.keySet();
    }
    
    /**
     * 增强逻辑（核心方法）
     * 子类必须实现具体的增强逻辑
     * 
     * @param model 执行上下文
     * @throws Exception 增强过程中的异常
     */
    public abstract void enhance(EnhancerModel model) throws Exception;
    
    /**
     * 过滤逻辑
     * 判断是否应该执行增强
     * 
     * @param model 执行上下文
     * @return true-执行增强，false-跳过
     */
    public abstract boolean filter(EnhancerModel model);
    
    /**
     * 获取增强器名称
     * 子类应该override此方法返回自己的名称
     * 
     * @return 增强器名称
     */
    public abstract String getName();
    
    /**
     * 检查是否达到限制次数
     * 
     * @return true-已达到限制，false-未达到
     */
    public boolean isLimit() {
        return limit > 0 && effectCount.get() >= limit;
    }
    
    /**
     * 增加执行次数
     */
    public void increaseCount() {
        effectCount.incrementAndGet();
    }
    
    // Getters and Setters
    
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public PointCut getPointCut() {
        return pointCut;
    }
    
    public void setPointCut(PointCut pointCut) {
        this.pointCut = pointCut;
    }
    
    public Map<String, String> getParams() {
        return params;
    }
    
    public void setParams(Map<String, String> params) {
        this.params = params;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public int getEffectCount() {
        return effectCount.get();
    }
    
    /**
     * 获取增强器动作名称（用于List展示）
     * 默认返回getName()，子类可以override
     * 
     * @return 动作名称
     */
    public String getAction() {
        return getName();
    }
    
    @Override
    public String toString() {
        return getName() + "{" +
                "uid='" + uid + '\'' +
                ", pointCut=" + pointCut +
                ", limit=" + limit +
                ", effectCount=" + effectCount.get() +
                '}';
    }
}



