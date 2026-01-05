package com.example.helper.chaos;

import com.example.helper.chaos.config.DiscoveryConfig;
import com.example.helper.chaos.config.SandboxConfig;
import com.example.helper.chaos.module.ChaosBlade;
import com.example.helper.chaos.proxy.ModuleProxyHandler;

/**
 * Chaos 客户端入口类
 * 
 * 简化的客户端实现，参考 zz 的设计
 * 
 * 使用示例：
 * <pre>
 * // 方式1：手动指定 Sandbox host 和 port（Sandbox 已注入）
 * Chaos chaos = new Chaos("127.0.0.1", 54973);
 * 
 * // 方式2：使用配置对象
 * SandboxConfig config = SandboxConfig.of("127.0.0.1", 54973);
 * Chaos chaos = new Chaos(config);
 * 
 * // 使用
 * DelayRequest request = DelayRequest.of("com.example.demo.controller.HelloController", "hello", 2000);
 * chaos.chaosBlade.create(request);
 * </pre>
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class Chaos {
    
    private final String host;
    private final int port;
    
    // ChaosBlade 模块代理
    public final ChaosBlade chaosBlade;
    
    
    /**
     * 使用配置对象创建客户端
     * 
     * @param config Sandbox 配置
     */
    public Chaos(SandboxConfig config) {
        this(config.getHost(), config.getPort());
    }
    
    /**
     * 构造函数
     * 
     * @param host Sandbox 主机地址
     * @param port Sandbox 端口
     */
    public Chaos(String host, int port) {
        this.host = host;
        this.port = port;
        this.chaosBlade = createProxy(ChaosBlade.class);
    }
    
    /**
     * 创建模块代理
     */
    private <T> T createProxy(Class<T> moduleClass) {
        return new ModuleProxyHandler(host, port, moduleClass).getProxy();
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
}

