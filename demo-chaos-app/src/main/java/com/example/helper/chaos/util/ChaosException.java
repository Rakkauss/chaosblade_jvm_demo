package com.example.helper.chaos.util;

/**
 * Chaos 异常工具类
 * 
 * 统一管理异常创建和处理，提供一致的异常消息格式
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public final class ChaosException {
    
    /** 私有构造函数 */
    private ChaosException() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }
    
    /**
     * 包装异常为 RuntimeException
     * 
     * @param message 错误消息
     * @param cause 原始异常
     * @return RuntimeException
     */
    public static RuntimeException wrap(String message, Exception cause) {
        return new RuntimeException(message + ": " + cause.getMessage(), cause);
    }
    
    /**
     * 创建参数为 null 的异常
     * 
     * @param paramName 参数名称
     * @return IllegalArgumentException
     */
    public static IllegalArgumentException requireNotNull(String paramName) {
        return new IllegalArgumentException(paramName + " 不能为 null");
    }
    
    /**
     * 检查参数是否为 null，如果为 null 则抛出异常
     * 
     * @param obj 待检查的对象
     * @param paramName 参数名称
     * @throws IllegalArgumentException 如果对象为 null
     */
    public static void checkNotNull(Object obj, String paramName) {
        if (obj == null) {
            throw requireNotNull(paramName);
        }
    }
    
    /**
     * 检查字符串是否为空，如果为空则抛出异常
     * 
     * @param str 待检查的字符串
     * @param paramName 参数名称
     * @throws IllegalArgumentException 如果字符串为 null 或空
     */
    public static void checkNotBlank(String str, String paramName) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " 不能为 null 或空");
        }
    }
    
    /**
     * 检查端口号是否有效
     * 
     * @param port 端口号
     * @throws IllegalArgumentException 如果端口号无效
     */
    public static void checkPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("端口必须在 1-65535 之间，当前值: " + port);
        }
    }
}

