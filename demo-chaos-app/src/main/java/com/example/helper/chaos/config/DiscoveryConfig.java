package com.example.helper.chaos.config;

/**
 * Sandbox 自动发现配置
 * 
 * 用于配置 Sandbox 自动发现过程中需要的参数，如目标应用名称、主机地址等。

 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class DiscoveryConfig {
    
    /** 目标应用名称（用于日志输出） */
    private final String targetAppName;
    
    /** 目标应用主类名（用于 jps 查找 PID） */
    private final String targetAppMainClass;
    
    /** Sandbox 主机地址 */
    private final String sandboxHost;
    
    /** 是否启用详细日志 */
    private final boolean verbose;
    
    private DiscoveryConfig(Builder builder) {
        this.targetAppName = builder.targetAppName;
        this.targetAppMainClass = builder.targetAppMainClass;
        this.sandboxHost = builder.sandboxHost;
        this.verbose = builder.verbose;
    }
    
    /**
     * 创建配置构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    
    public String getTargetAppName() {
        return targetAppName;
    }
    
    public String getTargetAppMainClass() {
        return targetAppMainClass;
    }
    
    public String getSandboxHost() {
        return sandboxHost;
    }
    
    public boolean isVerbose() {
        return verbose;
    }
    
    @Override
    public String toString() {
        return "DiscoveryConfig{" +
                "targetAppName='" + targetAppName + '\'' +
                ", targetAppMainClass='" + targetAppMainClass + '\'' +
                ", sandboxHost='" + sandboxHost + '\'' +
                ", verbose=" + verbose +
                '}';
    }
    
    /**
     * 配置构建器
     */
    public static class Builder {
        private String targetAppName;
        private String targetAppMainClass;
        private String sandboxHost;
        private boolean verbose = false;
        
        /**
         * 设置目标应用名称
         * 
         * @param targetAppName 应用名称（如 "demo-app", "demo-rpc-app"）
         */
        public Builder targetAppName(String targetAppName) {
            this.targetAppName = targetAppName;
            return this;
        }
        
        /**
         * 设置目标应用主类名
         * 
         * @param targetAppMainClass 主类名（如 "DemoApplication", "RpcApplication"）
         */
        public Builder targetAppMainClass(String targetAppMainClass) {
            this.targetAppMainClass = targetAppMainClass;
            return this;
        }
        
        /**
         * 设置 Sandbox 主机地址
         * 
         * @param sandboxHost 主机地址（如 "127.0.0.1", "192.168.1.100"）
         */
        public Builder sandboxHost(String sandboxHost) {
            this.sandboxHost = sandboxHost;
            return this;
        }
        
        /**
         * 设置是否启用详细日志
         * 
         * @param verbose 是否启用
         */
        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }
        
        /**
         * 构建配置对象
         */
        public DiscoveryConfig build() {
            if (targetAppName == null || targetAppName.isEmpty()) {
                throw new IllegalArgumentException("targetAppName 不能为空");
            }
            if (targetAppMainClass == null || targetAppMainClass.isEmpty()) {
                throw new IllegalArgumentException("targetAppMainClass 不能为空");
            }
            if (sandboxHost == null || sandboxHost.isEmpty()) {
                throw new IllegalArgumentException("sandboxHost 不能为空");
            }
            return new DiscoveryConfig(this);
        }
    }
}

