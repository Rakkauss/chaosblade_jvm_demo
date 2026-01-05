package com.example;

import com.example.helper.chaos.Chaos;
import com.example.util.SandboxAutoSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 注入测试基类
 * 
 * 提供统一的测试初始化和配置管理
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class InjectionTestBase {
    
    protected static final Logger log = LoggerFactory.getLogger(InjectionTestBase.class);
    
    /** Sandbox 主机地址 */
    protected static final String SANDBOX_HOST = "127.0.0.1";
    
    /** 测试等待时间（秒） */
    protected static final int TEST_WAIT_SECONDS = 5;
    
    /**
     * 测试配置
     */
    public static class TestConfig {
        /** demo-app 配置 */
        public static class DemoApp {
            public static final int HTTP_PORT = 8080;
            public static final String MAIN_CLASS = "DemoApplication";
            public static final String TARGET_CLASS = "com.example.demo.controller.HelloController";
            public static final String TARGET_METHOD = "hello";
        }
        
        /** RPC Provider 配置 */
        public static class RpcProvider {
            public static final int HTTP_PORT = 8090;
            public static final String MAIN_CLASS = "RpcProviderApplication";
            public static final String TARGET_CLASS = "com.example.rpc.provider.service.UserService";
            public static final String TARGET_METHOD = "getUserById";
        }
        
        /** RPC Consumer 配置 */
        public static class RpcConsumer {
            public static final int HTTP_PORT = 8081;
            public static final String MAIN_CLASS = "RpcConsumerApplication";
            public static final String TARGET_CLASS = "com.example.rpc.consumer.controller.UserController";
            public static final String TARGET_METHOD = "getUser";
        }
    }
    
    /**
     * 初始化 Chaos 客户端（通过应用端口）
     * 
     * @param appPort 应用端口
     * @param className 目标类名
     * @param methodName 目标方法名
     * @return Chaos 客户端，如果初始化失败返回 null
     */
    protected static Chaos initChaosByPort(int appPort, String className, String methodName) {
        try {
            log.info("通过应用端口初始化 Chaos 客户端: appPort={}, className={}, methodName={}", 
                appPort, className, methodName);
            int sandboxPort = SandboxAutoSetup.injectSandboxByPort(
                appPort, className, methodName, SANDBOX_HOST
            );
            Chaos chaos = new Chaos(SANDBOX_HOST, sandboxPort);
            log.info("Chaos 客户端已连接: {}:{}", chaos.getHost(), chaos.getPort());
            return chaos;
        } catch (Exception e) {
            log.warn("Chaos 客户端初始化失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 初始化 Chaos 客户端（通过主类名）
     * 
     * @param mainClassName 主类名
     * @param className 目标类名
     * @param methodName 目标方法名
     * @return Chaos 客户端，如果初始化失败返回 null
     */
    protected static Chaos initChaosByMainClass(String mainClassName, String className, String methodName) {
        try {
            log.info("通过主类名初始化 Chaos 客户端: mainClassName={}, className={}, methodName={}", 
                mainClassName, className, methodName);
            int sandboxPort = SandboxAutoSetup.injectSandboxByMainClass(
                mainClassName, className, methodName, SANDBOX_HOST
            );
            Chaos chaos = new Chaos(SANDBOX_HOST, sandboxPort);
            log.info("Chaos 客户端已连接: {}:{}", chaos.getHost(), chaos.getPort());
            return chaos;
        } catch (Exception e) {
            log.warn("Chaos 客户端初始化失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 初始化 Chaos 客户端（直接指定 PID）
     * 
     * @param pid 进程 PID
     * @param className 目标类名
     * @param methodName 目标方法名
     * @return Chaos 客户端，如果初始化失败返回 null
     */
    protected static Chaos initChaosByPid(int pid, String className, String methodName) {
        try {
            log.info("通过 PID 初始化 Chaos 客户端: pid={}, className={}, methodName={}", 
                pid, className, methodName);
            int sandboxPort = SandboxAutoSetup.injectSandbox(
                pid, className, methodName, SANDBOX_HOST
            );
            Chaos chaos = new Chaos(SANDBOX_HOST, sandboxPort);
            log.info("Chaos 客户端已连接: {}:{}", chaos.getHost(), chaos.getPort());
            return chaos;
        } catch (Exception e) {
            log.warn("Chaos 客户端初始化失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 初始化 Chaos 客户端（Sandbox 已注入，直接指定端口）
     * 
     * @param sandboxPort Sandbox 端口
     * @return Chaos 客户端
     */
    protected static Chaos initChaosBySandboxPort(int sandboxPort) {
        log.info("使用已注入的 Sandbox 初始化 Chaos 客户端: port={}", sandboxPort);
        return new Chaos(SANDBOX_HOST, sandboxPort);
    }
}

