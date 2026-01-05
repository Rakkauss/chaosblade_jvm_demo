package com.example.helper.chaos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * 命令执行工具类
 * 
 * 统一管理进程执行逻辑，包括环境变量设置、错误处理等
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public final class CommandExecutor {
    
    private static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);
    
    /** 私有构造函数 */
    private CommandExecutor() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }
    
    /**
     * 命令执行结果
     */
    public static class ProcessResult {
        private final int exitCode;
        private final String output;
        private final String error;
        
        public ProcessResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }
        
        public int getExitCode() {
            return exitCode;
        }
        
        public String getOutput() {
            return output;
        }
        
        public String getError() {
            return error;
        }
        
        public boolean isSuccess() {
            return exitCode == 0;
        }
    }
    
    /**
     * 执行命令并返回结果
     * 
     * @param command 命令数组
     * @return 执行结果
     * @throws Exception 执行失败
     */
    public static ProcessResult execute(String... command) throws Exception {
        return execute(null, null, command);
    }
    
    /**
     * 执行命令并返回结果（带环境变量和工作目录）
     * 
     * @param env 环境变量（可以为 null）
     * @param workingDir 工作目录（可以为 null）
     * @param command 命令数组
     * @return 执行结果
     * @throws Exception 执行失败
     */
    public static ProcessResult execute(Map<String, String> env, File workingDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        
        // 设置环境变量
        if (env != null) {
            pb.environment().putAll(env);
        }
        
        // 设置工作目录
        if (workingDir != null) {
            pb.directory(workingDir);
        }
        
        pb.redirectErrorStream(false);
        
        Process process = pb.start();
        
        // 读取标准输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // 读取错误输出
        StringBuilder error = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        
        return new ProcessResult(exitCode, output.toString().trim(), error.toString().trim());
    }
    
    /**
     * 执行命令并返回输出（简化方法）
     * 
     * @param command 命令数组
     * @return 命令输出，如果失败返回 null
     */
    public static String executeAndGetOutput(String... command) {
        try {
            ProcessResult result = execute(command);
            if (result.isSuccess()) {
                return result.getOutput();
            } else {
                log.warn("命令执行失败，退出码: {}, 错误: {}", result.getExitCode(), result.getError());
                return null;
            }
        } catch (Exception e) {
            log.warn("命令执行异常: {}", e.getMessage());
            return null;
        }
    }
}

