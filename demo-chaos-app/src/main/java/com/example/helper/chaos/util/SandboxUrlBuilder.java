package com.example.helper.chaos.util;

import com.example.helper.chaos.constant.ChaosConstants;

/**
 * Sandbox URL 构建工具类
 * 
 * 统一管理所有 Sandbox HTTP API 的 URL 构建逻辑
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public final class SandboxUrlBuilder {
    
    /** 私有构造函数 */
    private SandboxUrlBuilder() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }
    
    /**
     * 构建命令 URL
     * 
     * @param host 主机地址
     * @param port 端口
     * @param moduleId 模块 ID（如：chaosblade）
     * @param commandName 命令名称（如：create, destroy, status）
     * @return 完整的 URL
     */
    public static String buildCommandUrl(String host, int port, String moduleId, String commandName) {
        return String.format("http://%s:%d/sandbox/default/module/http/%s/%s",
            host, port, moduleId, commandName);
    }
    
    /**
     * 构建状态检查 URL
     * 
     * @param host 主机地址
     * @param port 端口
     * @return 状态检查 URL
     */
    public static String buildStatusUrl(String host, int port) {
        return String.format("http://%s:%d%s", host, port, ChaosConstants.SANDBOX_STATUS_PATH);
    }
    
    /**
     * 构建模块激活 URL
     * 
     * @param host 主机地址
     * @param port 端口
     * @param moduleId 模块 ID
     * @return 模块激活 URL
     */
    public static String buildModuleActiveUrl(String host, int port, String moduleId) {
        return String.format("http://%s:%d/sandbox/default/module/http/sandbox-module-mgr/active?ids=%s",
            host, port, moduleId);
    }
}

