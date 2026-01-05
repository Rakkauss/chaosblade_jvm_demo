package com.example.service;

import com.example.helper.chaos.Chaos;
import com.example.helper.chaos.config.SandboxConfig;
import com.example.helper.chaos.model.DelayRequest;
import com.example.helper.chaos.model.MockRequest;
import com.example.helper.chaos.model.ThrowsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 混沌工程服务类
 * 
 * @author rakkaus
 * @since 1.0.0
 * @see com.example.helper.chaos.Chaos Chaos 客户端核心类
 * @see com.example.helper.chaos.config.SandboxConfig Sandbox 配置类
 */
public class ChaosService {
    
    private static final Logger log = LoggerFactory.getLogger(ChaosService.class);
    
    private final Chaos chaos;
    
    
    /**
     * 使用配置对象创建服务
     * 
     * @param config Sandbox 配置
     */
    public ChaosService(SandboxConfig config) {
        this(config.getHost(), config.getPort());
    }
    
    /**
     * 构造函数
     * 
     * @param sandboxHost Sandbox 主机地址
     * @param sandboxPort Sandbox 端口
     */
    public ChaosService(String sandboxHost, int sandboxPort) {
        this.chaos = new Chaos(sandboxHost, sandboxPort);  // 只创建一次
    }
    
    /**
     * 注入延迟故障
     * 
     * @param className 类名
     * @param methodName 方法名
     * @param delayMs 延迟时间（毫秒）
     * @return 实验 ID
     */
    public String injectDelay(String className, String methodName, int delayMs) {
        DelayRequest request = DelayRequest.of(className, methodName, delayMs);
        String experimentId = chaos.chaosBlade.create(request);
        
        log.info("延迟注入成功，实验 ID: {}", experimentId);
        return experimentId;
    }
    
    /**
     * 注入异常故障
     * 
     * @param className 类名
     * @param methodName 方法名
     * @param exception 异常类型
     * @param message 异常消息
     * @return 实验 ID
     */
    public String injectException(String className, String methodName, 
                                   String exception, String message) {
        ThrowsRequest request = ThrowsRequest.of(className, methodName, exception, message);
        String experimentId = chaos.chaosBlade.create(request);
        
        log.info("异常注入成功，实验 ID: {}", experimentId);
        return experimentId;
    }
    
    /**
     * 注入返回值 Mock
     * 
     * @param className 类名
     * @param methodName 方法名
     * @param mockValue Mock 的返回值
     * @return 实验 ID
     */
    public String injectMock(String className, String methodName, String mockValue) {
        MockRequest request = MockRequest.of(className, methodName, mockValue);
        String experimentId = chaos.chaosBlade.create(request);
        
        log.info("Mock 注入成功，实验 ID: {}", experimentId);
        return experimentId;
    }
    
    /**
     * 销毁实验
     * 
     * @param experimentId 实验 ID
     */
    public void destroyExperiment(String experimentId) {
        chaos.chaosBlade.destroy(experimentId);
        log.info("实验已销毁: {}", experimentId);
    }
    
    /**
     * 查询状态
     * 
     * @return 状态信息
     */
    public String getStatus() {
        return chaos.chaosBlade.status();
    }
    
    /**
     * 列出所有实验
     * 
     * @return 实验列表
     */
    public String listExperiments() {
        return chaos.chaosBlade.list();
    }
    
    /**
     * 获取 Chaos 客户端实例
     * 
     * @return Chaos 实例
     */
    public Chaos getChaos() {
        return chaos;
    }
}

