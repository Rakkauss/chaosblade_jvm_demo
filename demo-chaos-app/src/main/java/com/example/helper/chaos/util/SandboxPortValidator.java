package com.example.helper.chaos.util;

import com.example.helper.chaos.constant.ChaosConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Sandbox 端口验证工具类
 * 
 * 统一管理端口验证逻辑，避免代码重复
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public final class SandboxPortValidator {
    
    private static final Logger log = LoggerFactory.getLogger(SandboxPortValidator.class);
    
    /** 私有构造函数 */
    private SandboxPortValidator() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }
    
    /**
     * 验证是否是 Sandbox 端口
     * 
     * 通过 HTTP 请求验证端口是否是 Sandbox 端口
     * 
     * @param host 主机地址
     * @param port 端口号
     * @return 是否是 Sandbox 端口
     */
    public static boolean isSandboxPort(String host, int port) {
        try {
            URL url = new URL("http://" + host + ":" + port + ChaosConstants.SANDBOX_STATUS_PATH);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(ChaosConstants.HTTP_REQUEST_TIMEOUT_MS);
            conn.setReadTimeout(ChaosConstants.HTTP_REQUEST_TIMEOUT_MS);
            int responseCode = conn.getResponseCode();
            // Sandbox 端口可能返回 200（成功）、404（路径不存在但端口正确）、405（方法不允许但端口正确）
            return responseCode == 200 || responseCode == 404 || responseCode == 405;
        } catch (Exception e) {
            log.debug("端口验证失败: {}:{} - {}", host, port, e.getMessage());
            return false;
        }
    }
    
    /**
     * 排除系统端口和动态端口
     * 
     * @param port 端口号
     * @return 是否应该排除
     */
    public static boolean isExcludedPort(int port) {
        // 排除系统端口（< 1000）和动态端口（> 60000）
        // Sandbox 端口通常在 50000-60000 范围内
        return port < 1000 || port > 60000;
    }
}

