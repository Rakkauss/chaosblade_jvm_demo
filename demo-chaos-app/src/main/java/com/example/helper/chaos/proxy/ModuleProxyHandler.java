package com.example.helper.chaos.proxy;

import com.example.helper.chaos.annotation.Command;
import com.example.helper.chaos.annotation.Module;
import com.example.helper.chaos.model.ChaosResponse;
import com.example.helper.chaos.util.ChaosException;
import com.example.helper.chaos.util.HttpUtil;
import com.example.helper.chaos.util.JsonUtil;
import com.example.helper.chaos.util.SandboxUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 模块动态代理处理器
 * 
 * 使用 Java 动态代理将接口方法调用转换为 HTTP 请求。
 * 
 * @author rakkaus
 */
public class ModuleProxyHandler implements InvocationHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ModuleProxyHandler.class);
    
    private static final int MODULE_ACTIVATION_WAIT_MS = 200;
    private static final int COMMAND_RETRY_WAIT_MS = 500;
    
    private final String host;
    private final int port;
    private final Class<?> targetInterface;
    
    /**
     * 构造函数
     * 
     * @param host Sandbox 主机地址
     * @param port Sandbox 端口
     * @param targetInterface 目标接口
     */
    public ModuleProxyHandler(String host, int port, Class<?> targetInterface) {
        ChaosException.checkNotBlank(host, "host");
        ChaosException.checkPort(port);
        ChaosException.checkNotNull(targetInterface, "targetInterface");
        if (!targetInterface.isInterface()) {
            throw new IllegalArgumentException("targetInterface 必须是接口类型");
        }
        
        this.host = host;
        this.port = port;
        this.targetInterface = targetInterface;
    }
    
    /**
     * 创建代理实例
     * 
     * @param <T> 接口类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy() {
        return (T) Proxy.newProxyInstance(
            targetInterface.getClassLoader(),
            new Class[]{targetInterface},
            this
        );
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Module moduleAnnotation = targetInterface.getAnnotation(Module.class);
        if (moduleAnnotation == null) {
            String error = String.format("接口缺少 @Module 注解: %s", targetInterface.getName());
            log.error(error);
            throw new IllegalStateException(error);
        }
        
        Command commandAnnotation = method.getAnnotation(Command.class);
        if (commandAnnotation == null) {
            String error = String.format("方法缺少 @Command 注解: %s.%s", 
                targetInterface.getName(), method.getName());
            log.error(error);
            throw new IllegalStateException(error);
        }
        
        String moduleId = moduleAnnotation.value();
        String commandName = commandAnnotation.value();
        String url = SandboxUrlBuilder.buildCommandUrl(host, port, moduleId, commandName);
        
        log.info("【调用方法】{}:{} -> {}:{}", targetInterface.getSimpleName(), method.getName(), host, port);
        log.info("【请求URL】{}", url);
        
        String jsonBody = serializeParameters(args);
        ChaosResponse<Map<String, Object>> response = sendRequest(url, jsonBody, commandName);
        
        return parseResponse(response, method, commandName);
    }
    
    /**
     * 序列化方法参数
     * 
     * @param args 方法参数数组
     * @return JSON 字符串
     */
    private String serializeParameters(Object[] args) {
        if (args == null || args.length == 0) {
            return "{}";
        }
        
        Object arg = args[0];
        if (arg instanceof String) {
            String jsonBody = JsonUtil.toJson(java.util.Collections.singletonMap("uid", arg.toString()));
            log.info("【注入参数】uid: {}", arg);
            return jsonBody;
        } else {
            String jsonBody = JsonUtil.toJson(arg);
            log.info("【注入参数】{}", jsonBody);
            return jsonBody;
        }
    }
    
    /**
     * 发送 HTTP 请求
     * 
     * @param url 请求 URL
     * @param jsonBody JSON 请求体
     * @param commandName 命令名称
     * @return 响应对象
     * @throws RuntimeException 如果请求失败
     */
    private ChaosResponse<Map<String, Object>> sendRequest(String url, String jsonBody, String commandName) {
        try {
            String responseJson = HttpUtil.post(url, jsonBody);
            ChaosResponse<Map<String, Object>> response = ChaosResponse.fromJson(responseJson);
            
            log.info("【响应结果】success={}, code={}, experimentId={}, error={}", 
                response.isSuccess(), 
                response.getCode(),
                response.getExperimentId(),
                response.getError());
            
            if (response.isFailure()) {
                String error = response.getError();
                if (error != null && error.contains("Command not found")) {
                    log.warn("命令未找到，尝试激活模块: {}", commandName);
                    handleCommandNotFound(url, jsonBody, commandName);
                    responseJson = HttpUtil.post(url, jsonBody);
                    response = ChaosResponse.fromJson(responseJson);
                } else {
                    log.error("请求失败: {}, error={}", commandName, error);
                }
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("HTTP 请求异常: url={}, command={}", url, commandName, e);
            throw ChaosException.wrap("HTTP 请求失败", e);
        }
    }
    
    /**
     * 处理命令未找到的情况
     * 
     * @param url 请求 URL
     * @param jsonBody JSON 请求体
     * @param commandName 命令名称
     */
    private void handleCommandNotFound(String url, String jsonBody, String commandName) {
        log.warn("命令未找到，尝试激活模块");
        String moduleId = extractModuleIdFromUrl(url);
        ensureModuleActivated(moduleId);
        try {
            Thread.sleep(COMMAND_RETRY_WAIT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 从 URL 中提取模块 ID
     * 
     * @param url 请求 URL
     * @return 模块 ID
     */
    private String extractModuleIdFromUrl(String url) {
        // URL 格式: http://host:port/sandbox/default/module/http/{moduleId}/{commandName}
        int start = url.indexOf("/module/http/") + "/module/http/".length();
        int end = url.indexOf("/", start);
        if (end == -1) {
            end = url.length();
        }
        log.info("模块 URL: {}", url.substring(start, end));
        return url.substring(start, end);
    }
    
    /**
     * 解析响应并返回结果
     * 
     * @param response 响应对象
     * @param method 调用的方法
     * @param commandName 命令名称
     * @return 解析后的结果
     * @throws RuntimeException 如果操作失败
     */
    private Object parseResponse(ChaosResponse<Map<String, Object>> response, Method method, String commandName) {
        response.throwIfFailure();
        
        Class<?> returnType = method.getReturnType();
        
        if (returnType == boolean.class || returnType == Boolean.class) {
            return response.isSuccess();
        } else if (returnType == String.class) {
            if ("create".equals(commandName)) {
                String experimentId = response.getExperimentId();
                if (experimentId == null || experimentId.isEmpty()) {
                    log.error("create 命令无法获取 experimentId，响应: {}", response);
                    throw new RuntimeException("无法获取实验ID");
                }
                log.info("【注入成功】experimentId: {}", experimentId);
                return experimentId;
            } else {
                Map<String, Object> result = response.getResult();
                String resultJson = result != null ? JsonUtil.toJson(result) : "";
                return resultJson;
            }
        } else if (returnType == void.class) {
            return null;
        }
        
        return response;
    }
    
    /**
     * 确保模块已激活
     * 
     * @param moduleId 模块 ID
     */
    private void ensureModuleActivated(String moduleId) {
        try {
            // 尝试激活模块（如果已激活，这个调用不会报错）
            // 注意：模块管理模块的 ID 是 "sandbox-module-mgr"
            String activateUrl = SandboxUrlBuilder.buildModuleActiveUrl(host, port, moduleId);
            
            String response = HttpUtil.get(activateUrl);
            if (response != null && (response.contains("activated") || response.contains("total"))) {
                Thread.sleep(MODULE_ACTIVATION_WAIT_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("激活模块失败: {}", e.getMessage());
        }
    }
}
