package com.example.helper.chaos.util;

/**
 * 重试工具类
 * 
 * 提供异常重试机制，支持指数退避
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class RetryUtil {
    
    /**
     * 获取默认最大重试次数（从配置读取）
     */
    private static int getDefaultMaxRetries() {
        return com.example.helper.chaos.config.ChaosConfig.getHttpRetryMax();
    }
    
    /**
     * 获取默认初始延迟（从配置读取）
     */
    private static long getDefaultInitialDelay() {
        return com.example.helper.chaos.config.ChaosConfig.getHttpRetryDelay();
    }
    
    /**
     * 获取默认延迟倍数（从配置读取）
     */
    private static double getDefaultMultiplier() {
        return com.example.helper.chaos.config.ChaosConfig.getHttpRetryMultiplier();
    }
    
    /**
     * 可重试的操作接口
     */
    public interface RetryableOperation<T> {
        /**
         * 执行操作
         * 
         * @return 操作结果
         * @throws Exception 操作失败
         */
        T execute() throws Exception;
    }
    
    /**
     * 执行带重试的操作（使用默认配置）
     * 
     * @param operation 操作
     * @param <T> 返回类型
     * @return 操作结果
     * @throws Exception 重试失败后抛出最后一次异常
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation) throws Exception {
        return executeWithRetry(operation, getDefaultMaxRetries());
    }
    
    /**
     * 执行带重试的操作
     * 
     * @param operation 操作
     * @param maxRetries 最大重试次数
     * @param <T> 返回类型
     * @return 操作结果
     * @throws Exception 重试失败后抛出最后一次异常
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation, int maxRetries) throws Exception {
        return executeWithRetry(operation, maxRetries, getDefaultInitialDelay(), getDefaultMultiplier());
    }
    
    /**
     * 执行带重试的操作（完整配置）
     * 
     * @param operation 操作
     * @param maxRetries 最大重试次数
     * @param initialDelay 初始延迟（毫秒）
     * @param multiplier 延迟倍数（指数退避）
     * @param <T> 返回类型
     * @return 操作结果
     * @throws Exception 重试失败后抛出最后一次异常
     */
    public static <T> T executeWithRetry(
            RetryableOperation<T> operation,
            int maxRetries,
            long initialDelay,
            double multiplier) throws Exception {
        
        Exception lastException = null;
        long delay = initialDelay;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                
                // 如果是最后一次尝试，直接抛出异常
                if (attempt == maxRetries) {
                    break;
                }
                
                // 等待后重试（指数退避）
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
                
                // 增加延迟时间（指数退避）
                delay = (long) (delay * multiplier);
            }
        }
        
        // 抛出最后一次异常
        throw lastException;
    }
    
    /**
     * 执行带重试的操作（静默模式，不抛出异常）
     * 
     * @param operation 操作
     * @param defaultValue 失败时的默认值
     * @param <T> 返回类型
     * @return 操作结果或默认值
     */
    public static <T> T executeWithRetrySilent(RetryableOperation<T> operation, T defaultValue) {
        try {
            return executeWithRetry(operation);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 判断异常是否可重试
     * 
     * @param e 异常
     * @return 是否可重试
     */
    public static boolean isRetryable(Exception e) {
        // 网络相关异常通常可重试
        String message = e.getMessage();
        if (message != null) {
            return message.contains("timeout") ||
                   message.contains("Connection refused") ||
                   message.contains("Connection reset") ||
                   message.contains("Network is unreachable");
        }
        return false;
    }
}

