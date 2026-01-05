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

package com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.listener;

import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.Enhancer;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ListenerManager - 监听器管理器
 * 
 * [改造说明]
 * 简化版本的监听器管理器，主要功能：
 * 1. 管理 watchIds（experimentId -> watcherId）
 * 2. 注册字节码增强（watch）
 * 3. 取消字节码增强（delete）
 * 4. 查询实验是否存在
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class ListenerManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ListenerManager.class);
    
    /** watchIds 缓存：experimentId -> watcherId */
    private static final Map<String, Integer> watchIds = new ConcurrentHashMap<>();
    
    /** ModuleEventWatcher 实例（由 SandboxModule 注入） */
    private static ModuleEventWatcher moduleEventWatcher;
    
    /**
     * 设置 ModuleEventWatcher
     * 由 SandboxModule 在初始化时调用
     * 
     * @param watcher ModuleEventWatcher 实例
     */
    public static void setModuleEventWatcher(ModuleEventWatcher watcher) {
        moduleEventWatcher = watcher;
        logger.info("[监听器管理器] ModuleEventWatcher 已设置");
    }
    
    /**
     * 注册字节码增强
     * 
     * @param enhancer Enhancer 实例
     * @param filter Filter 实例（匹配规则）
     * @return watcherId
     */
    public static int watch(Enhancer enhancer, com.alibaba.jvm.sandbox.api.filter.Filter filter) {
        if (moduleEventWatcher == null) {
            throw new IllegalStateException("ModuleEventWatcher not initialized");
        }

        // 创建事件监听器
        MethodEventListener listener = new MethodEventListener(enhancer);
        
        // 注册字节码增强（只监听 BEFORE 事件）
        int watcherId = moduleEventWatcher.watch(
            filter,
            listener,
            com.alibaba.jvm.sandbox.api.event.Event.Type.BEFORE
        );
        
        // 缓存 watcherId
        String experimentId = enhancer.getUid();
        watchIds.put(experimentId, watcherId);
        
        logger.info("[监听器管理器] 监听已注册: 实验ID={}, 监听器ID={}", 
            experimentId, watcherId);
        
        return watcherId;
    }
    
    /**
     * 取消字节码增强
     * 
     * @param experimentId 实验 ID
     */
    public static void delete(String experimentId) {
        if (moduleEventWatcher == null) {
            logger.warn("[监听器管理器] ModuleEventWatcher 未初始化，无法删除");
            return;
        }
        
        Integer watcherId = watchIds.remove(experimentId);
        if (watcherId != null) {
            moduleEventWatcher.delete(watcherId);
            logger.info("[监听器管理器] 监听已删除: 实验ID={}, 监听器ID={}", 
                experimentId, watcherId);
        } else {
            logger.warn("[监听器管理器] 监听未找到: 实验ID={}", experimentId);
        }
    }
    
    /**
     * 查询实验是否存在
     * 
     * @param experimentId 实验 ID
     * @return true-存在，false-不存在
     */
    public static boolean exists(String experimentId) {
        return watchIds.containsKey(experimentId);
    }
    
    /**
     * 获取所有 watchIds
     * 
     * @return watchIds Map
     */
    public static Map<String, Integer> getWatchIds() {
        return watchIds;
    }
    
    /**
     * 清空所有 watchIds（用于模块卸载）
     */
    public static void clear() {
        watchIds.clear();
        logger.info("[监听器管理器] 所有监听器ID已清空");
    }
}

