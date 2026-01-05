package com.example.helper.chaos.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 简单的日志工具类
 * 
 * 零外部依赖的日志实现，支持日志级别控制
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class Logger {
    
    /** 日志级别 */
    public enum Level {
        DEBUG(0, "DEBUG"),
        INFO(1, "INFO "),
        WARN(2, "WARN "),
        ERROR(3, "ERROR");
        
        private final int value;
        private final String name;
        
        Level(int value, String name) {
            this.value = value;
            this.name = name;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getName() {
            return name;
        }
    }
    
    private final String name;
    private static Level globalLevel = Level.INFO;  // 默认日志级别
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 获取 Logger 实例
     * 
     * @param clazz 类
     * @return Logger 实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getSimpleName());
    }
    
    /**
     * 获取 Logger 实例
     * 
     * @param name 名称
     * @return Logger 实例
     */
    public static Logger getLogger(String name) {
        return new Logger(name);
    }
    
    /**
     * 设置全局日志级别
     * 
     * @param level 日志级别
     */
    public static void setGlobalLevel(Level level) {
        globalLevel = level;
    }
    
    /**
     * 构造函数
     * 
     * @param name Logger 名称
     */
    private Logger(String name) {
        this.name = name;
    }
    
    /**
     * 记录 DEBUG 日志
     * 
     * @param message 消息
     */
    public void debug(String message) {
        log(Level.DEBUG, message, null);
    }
    
    /**
     * 记录 DEBUG 日志（带异常）
     * 
     * @param message 消息
     * @param throwable 异常
     */
    public void debug(String message, Throwable throwable) {
        log(Level.DEBUG, message, throwable);
    }
    
    /**
     * 记录 INFO 日志
     * 
     * @param message 消息
     */
    public void info(String message) {
        log(Level.INFO, message, null);
    }
    
    /**
     * 记录 INFO 日志（带异常）
     * 
     * @param message 消息
     * @param throwable 异常
     */
    public void info(String message, Throwable throwable) {
        log(Level.INFO, message, throwable);
    }
    
    /**
     * 记录 WARN 日志
     * 
     * @param message 消息
     */
    public void warn(String message) {
        log(Level.WARN, message, null);
    }
    
    /**
     * 记录 WARN 日志（带异常）
     * 
     * @param message 消息
     * @param throwable 异常
     */
    public void warn(String message, Throwable throwable) {
        log(Level.WARN, message, throwable);
    }
    
    /**
     * 记录 ERROR 日志
     * 
     * @param message 消息
     */
    public void error(String message) {
        log(Level.ERROR, message, null);
    }
    
    /**
     * 记录 ERROR 日志（带异常）
     * 
     * @param message 消息
     * @param throwable 异常
     */
    public void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }
    
    /**
     * 记录日志
     * 
     * @param level 日志级别
     * @param message 消息
     * @param throwable 异常（可选）
     */
    private void log(Level level, String message, Throwable throwable) {
        // 检查日志级别
        if (level.getValue() < globalLevel.getValue()) {
            return;
        }
        
        // 选择输出流
        PrintStream out = (level == Level.ERROR || level == Level.WARN) ? System.err : System.out;
        
        // 格式化日志
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] [%s] [%s] %s",
                timestamp, level.getName(), name, message);
        
        // 输出日志
        out.println(logMessage);
        
        // 输出异常堆栈
        if (throwable != null) {
            throwable.printStackTrace(out);
        }
    }
    
    /**
     * 判断 DEBUG 级别是否启用
     * 
     * @return 是否启用
     */
    public boolean isDebugEnabled() {
        return Level.DEBUG.getValue() >= globalLevel.getValue();
    }
    
    /**
     * 判断 INFO 级别是否启用
     * 
     * @return 是否启用
     */
    public boolean isInfoEnabled() {
        return Level.INFO.getValue() >= globalLevel.getValue();
    }
    
    /**
     * 判断 WARN 级别是否启用
     * 
     * @return 是否启用
     */
    public boolean isWarnEnabled() {
        return Level.WARN.getValue() >= globalLevel.getValue();
    }
    
    /**
     * 判断 ERROR 级别是否启用
     * 
     * @return 是否启用
     */
    public boolean isErrorEnabled() {
        return Level.ERROR.getValue() >= globalLevel.getValue();
    }
}

