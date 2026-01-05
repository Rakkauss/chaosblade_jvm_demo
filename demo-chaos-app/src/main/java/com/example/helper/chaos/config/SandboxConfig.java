package com.example.helper.chaos.config;

import com.example.util.SandboxAutoSetup;

/**
 * Sandbox 配置类
 * 
 * 手动指定 Sandbox 的 host 和 port
 * 
 * 使用示例：
 * <pre>
 * // 手动指定 Sandbox host 和 port
 * SandboxConfig config = SandboxConfig.of("127.0.0.1", 54973);
 * Chaos chaos = new Chaos(config);
 * </pre>
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class SandboxConfig {
    
    private final String host;
    private final int port;
    
    private SandboxConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    
    /**
     * 创建配置（手动指定）
     * 
     * @param host 主机地址
     * @param port 端口
     * @return 配置对象
     */
    public static SandboxConfig of(String host, int port) {
        return new SandboxConfig(host, port);
    }
    
    /**
     * 获取主机地址
     */
    public String getHost() {
        return host;
    }
    
    /**
     * 获取端口
     */
    public int getPort() {
        return port;
    }
    
    @Override
    public String toString() {
        return "SandboxConfig{host='" + host + "', port=" + port + "}";
    }
}

