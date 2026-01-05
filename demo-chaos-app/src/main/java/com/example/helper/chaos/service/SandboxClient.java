package com.example.helper.chaos.service;

/**
 * Sandbox 客户端接口
 * 
 * 定义与 Sandbox 交互的标准操作
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public interface SandboxClient {
    
    /**
     * 注入 Sandbox 到目标进程
     * 
     * @param pid 目标进程 PID
     * @throws SandboxException 注入失败
     */
    void inject(int pid) throws SandboxException;
    
    /**
     * 刷新 Sandbox 模块
     * 
     * @param pid 目标进程 PID
     * @throws SandboxException 刷新失败
     */
    void refreshModules(int pid) throws SandboxException;
    
    /**
     * 激活指定模块
     * 
     * @param pid 目标进程 PID
     * @param moduleId 模块 ID
     * @throws SandboxException 激活失败
     */
    void activateModule(int pid, String moduleId) throws SandboxException;
    
    /**
     * 检查 Sandbox 是否已注入
     * 
     * @param pid 目标进程 PID
     * @return 是否已注入
     */
    boolean isInjected(int pid);
    
    /**
     * 检查模块是否已激活
     * 
     * @param pid 目标进程 PID
     * @param moduleId 模块 ID
     * @return 是否已激活
     */
    boolean isModuleActive(int pid, String moduleId);
}

