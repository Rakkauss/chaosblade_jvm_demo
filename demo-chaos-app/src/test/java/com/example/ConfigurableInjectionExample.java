package com.example;

import com.example.helper.chaos.Chaos;
import com.example.helper.chaos.config.SandboxConfig;
import com.example.helper.chaos.model.DelayRequest;
import com.example.util.SandboxAutoSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 可配置注入示例
 * 
 * 演示如何使用不同的配置方式进行故障注入
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class ConfigurableInjectionExample {
    
    private static final Logger log = LoggerFactory.getLogger(ConfigurableInjectionExample.class);
    
    public static void main(String[] args) {
        log.info("可配置注入示例");
        
        // 示例 1: 手动指定 Sandbox host 和 port（Sandbox 已注入）
        example1_ManualConfig();
        
        // 示例 2: 通过应用端口自动注入 Sandbox
        example2_AutoInjectByPort();
        
        // 示例 3: 通过主类名自动注入 Sandbox
        example3_AutoInjectByMainClass();
    }
    
    /**
     * 示例 1: 手动指定 Sandbox host 和 port（Sandbox 已注入）
     */
    private static void example1_ManualConfig() {
        log.info("示例 1: 手动指定 Sandbox host 和 port");
        
        try {
            // 如果 Sandbox 已注入，直接指定 host 和 port
            Chaos chaos = new Chaos("127.0.0.1", 54973);
            
            // 创建延迟注入请求（必须指定完整的类名和方法名）
            DelayRequest request = DelayRequest.of(
                "com.example.demo.controller.HelloController",
                "hello",
                2000
            );
            
            // 执行注入
            String experimentId = chaos.chaosBlade.create(request);
            log.info("注入成功，实验 ID: {}", experimentId);
            
            chaos.chaosBlade.destroy(experimentId);
            log.info("实验已销毁");
            
        } catch (Exception e) {
            log.error("示例 1 失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 示例 2: 通过应用端口自动注入 Sandbox
     */
    private static void example2_AutoInjectByPort() {
        log.info("示例 2: 通过应用端口自动注入 Sandbox");
        
        try {
            String sandboxHost = "127.0.0.1";
            int appPort = 8080;  // 目标应用的 HTTP 端口
            String className = "com.example.demo.controller.HelloController";
            String methodName = "hello";
            
            // 通过应用端口查找 PID 并注入 Sandbox
            int sandboxPort = SandboxAutoSetup.injectSandboxByPort(
                appPort, className, methodName, sandboxHost
            );
            
            // 创建 Chaos 客户端
            Chaos chaos = new Chaos(sandboxHost, sandboxPort);
            
            // 创建延迟注入请求
            DelayRequest request = DelayRequest.of(className, methodName, 3000);
            String experimentId = chaos.chaosBlade.create(request);
            log.info("注入成功，实验 ID: {}", experimentId);
            
            chaos.chaosBlade.destroy(experimentId);
            log.info("实验已销毁");
            
        } catch (Exception e) {
            log.error("示例 2 失败: {}", e.getMessage(), e);
            log.warn("提示：请确保目标应用正在运行，并且端口正确");
        }
    }
    
    /**
     * 示例 3: 通过主类名自动注入 Sandbox
     */
    private static void example3_AutoInjectByMainClass() {
        log.info("示例 3: 通过主类名自动注入 Sandbox");
        
        try {
            String sandboxHost = "127.0.0.1";
            String mainClassName = "DemoApplication";  // 目标应用主类名
            String className = "com.example.demo.controller.HelloController";
            String methodName = "hello";
            
            // 通过主类名查找 PID 并注入 Sandbox
            int sandboxPort = SandboxAutoSetup.injectSandboxByMainClass(
                mainClassName, className, methodName, sandboxHost
            );
            
            // 创建 Chaos 客户端
            Chaos chaos = new Chaos(sandboxHost, sandboxPort);
            
            // 创建延迟注入请求
            DelayRequest request = DelayRequest.of(className, methodName, 2000);
            String experimentId = chaos.chaosBlade.create(request);
            log.info("注入成功，实验 ID: {}", experimentId);
            
            chaos.chaosBlade.destroy(experimentId);
            log.info("实验已销毁");
            
        } catch (Exception e) {
            log.error("示例 3 失败: {}", e.getMessage(), e);
            log.warn("提示：请确保目标应用正在运行，并且主类名正确");
        }
    }
}

