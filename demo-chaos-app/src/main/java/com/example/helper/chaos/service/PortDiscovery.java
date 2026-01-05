package com.example.helper.chaos.service;

/**
 * 端口发现接口
 * 
 * 定义查找 Sandbox 端口的策略
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public interface PortDiscovery {
    
    /**
     * 查找指定进程的 Sandbox 端口
     * 
     * @param pid 目标进程 PID
     * @param host 主机地址
     * @return Sandbox 端口，如果未找到返回 -1
     */
    int findPort(int pid, String host);
    
    /**
     * 获取策略名称
     * 
     * @return 策略名称
     */
    String getStrategyName();
    
    /**
     * 获取策略优先级（数字越小优先级越高）
     * 
     * @return 优先级
     */
    int getPriority();
}

