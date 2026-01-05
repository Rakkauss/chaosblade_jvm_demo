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

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * DelayEnhancer - 延迟增强器
 * 
 * [改造说明]
 * 简化版本的延迟注入，在方法执行前休眠指定时间

 * @author rakkaus
 * @since 1.8.0
 */
public class DelayEnhancer extends Enhancer {
    
    /** 延迟时间（毫秒） */
    private long delayTime = 0;
    
    /** 时间偏移量（毫秒） */
    private int offset = 0;
    
    @Override
    public String getName() {
        return "delay";
    }
    
    @Override
    public void enhance(EnhancerModel model) throws Exception {
        // 1. 解析延迟参数
        parseParams();
        
        // 2. 计算实际延迟时间（加上随机偏移）
        long actualDelay = calculateActualDelay();
        
        // 3. 执行延迟
        logger.info("[延迟增强器] 开始延迟: {}ms (基础: {}ms, 偏移: {}ms) 用于 {}#{}",
                actualDelay, delayTime, offset, 
                model.getClassName(), model.getMethodName());
        
        try {
            TimeUnit.MILLISECONDS.sleep(actualDelay);
        } catch (InterruptedException e) {
            logger.warn("[延迟增强器] 延迟被中断", e);
            Thread.currentThread().interrupt();
        }
        
        logger.info("[延迟增强器] 延迟完成");
        
        // 4. 增加执行计数
        increaseCount();
    }
    
    @Override
    public boolean filter(EnhancerModel model) {
        // 1. 检查是否达到限制次数
        if (isLimit()) {
            logger.debug("[延迟增强器] 已达到限制次数，跳过增强");
            return false;
        }
        
        // 2. 检查是否匹配类和方法
        if (pointCut != null) {
            boolean classMatch = pointCut.matchClass(model.getClassName());
            boolean methodMatch = pointCut.matchMethod(model.getMethodName());
            
            if (!classMatch || !methodMatch) {
                logger.debug("[延迟增强器] 不匹配，跳过增强");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 解析延迟参数
     */
    private void parseParams() {
        if (params == null) {
            logger.warn("[延迟增强器] 参数为空，使用默认延迟: 0ms");
            return;
        }
        
        // 解析延迟时间
        String timeStr = params.get("time");
        if (timeStr != null && !timeStr.isEmpty()) {
            try {
                delayTime = Long.parseLong(timeStr);
            } catch (NumberFormatException e) {
                logger.error("[延迟增强器] 无效的时间参数: {}", timeStr);
                delayTime = 0;
            }
        }
        
        // 解析偏移量
        String offsetStr = params.get("offset");
        if (offsetStr != null && !offsetStr.isEmpty()) {
            try {
                offset = Integer.parseInt(offsetStr);
            } catch (NumberFormatException e) {
                logger.error("[延迟增强器] 无效的偏移量参数: {}", offsetStr);
                offset = 0;
            }
        }
    }
    
    /**
     * 计算实际延迟时间（加上随机偏移）
     * 
     * @return 实际延迟时间（毫秒）
     */
    private long calculateActualDelay() {
        if (offset <= 0) {
            return delayTime;
        }
        
        Random random = new Random();
        int randomOffset = random.nextInt(offset);
        
        // 50%概率增加，50%概率减少
        long actualDelay;
        if (randomOffset % 2 == 0) {
            actualDelay = delayTime + randomOffset;
        } else {
            actualDelay = delayTime - randomOffset;
        }
        
        // 确保延迟时间不为负数
        if (actualDelay <= 0) {
            actualDelay = offset;
        }
        
        return actualDelay;
    }
}



