package com.example.helper.chaos.service;

/**
 * Sandbox 异常
 * 
 * 封装与 Sandbox 交互过程中的异常
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public class SandboxException extends Exception {
    
    public SandboxException(String message) {
        super(message);
    }
    
    public SandboxException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Sandbox 注入失败
     */
    public static class InjectionException extends SandboxException {
        public InjectionException(String message) {
            super(message);
        }
        
        public InjectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * 模块激活失败
     */
    public static class ModuleActivationException extends SandboxException {
        public ModuleActivationException(String message) {
            super(message);
        }
        
        public ModuleActivationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * 端口发现失败
     */
    public static class PortDiscoveryException extends SandboxException {
        public PortDiscoveryException(String message) {
            super(message);
        }
        
        public PortDiscoveryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

