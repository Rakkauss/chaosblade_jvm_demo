package com.example.helper.chaos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Chaos 配置管理类
 * 
 * 配置优先级：
 * 1. 系统属性（-Dchaos.xxx=value）
 * 2. 环境变量（CHAOS_XXX=value）
 * 3. 配置文件（chaos.properties）
 * 4. 默认值
 * 
 * @author rakkaus
 * @since 1.0.0
 */
public class ChaosConfig {
    
    private static final Logger log = LoggerFactory.getLogger(ChaosConfig.class);
    
    private static final String CONFIG_FILE = "chaos.properties";
    private static final Properties properties = new Properties();
    private static boolean loaded = false;
    
    // ========== 默认值 ==========
    private static final String DEFAULT_SANDBOX_HOST = "127.0.0.1";
    private static final int DEFAULT_SANDBOX_PORT = 0;  // 0 表示自动发现
    private static final String DEFAULT_SANDBOX_HOME = System.getProperty("user.home") + "/sandbox";
    private static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_HTTP_READ_TIMEOUT = 10000;
    private static final int DEFAULT_HTTP_RETRY_MAX = 3;
    private static final long DEFAULT_HTTP_RETRY_DELAY = 100;
    private static final double DEFAULT_HTTP_RETRY_MULTIPLIER = 2.0;
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static final String DEFAULT_TEST_APP_HOST = "localhost";
    private static final int DEFAULT_TEST_APP_PORT = 8080;
    
    static {
        loadConfig();
    }
    
    /**
     * 加载配置文件
     */
    private static void loadConfig() {
        if (loaded) {
            return;
        }
        
        try (InputStream is = ChaosConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                properties.load(is);
                log.debug("配置文件加载成功: {}", CONFIG_FILE);
            } else {
                log.debug("配置文件不存在，使用默认配置: {}", CONFIG_FILE);
            }
        } catch (IOException e) {
            log.warn("配置文件加载失败: {}", e.getMessage());
        }
        
        loaded = true;
    }
    
    /**
     * 获取配置值
     * 
     * 优先级：系统属性 > 环境变量 > 配置文件 > 默认值
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private static String getProperty(String key, String defaultValue) {
        // 1. 系统属性
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }
        
        // 2. 环境变量（将 . 替换为 _，转大写）
        String envKey = key.replace('.', '_').toUpperCase();
        value = System.getenv(envKey);
        if (value != null) {
            return value;
        }
        
        // 3. 配置文件
        value = properties.getProperty(key);
        if (value != null) {
            // 支持变量替换：${key}
            return resolveVariables(value);
        }
        
        // 4. 默认值
        return defaultValue;
    }
    
    /**
     * 解析变量
     * 
     * @param value 原始值
     * @return 解析后的值
     */
    private static String resolveVariables(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }
        
        int start = value.indexOf("${");
        while (start != -1) {
            int end = value.indexOf("}", start);
            if (end == -1) {
                break;
            }
            
            String varName = value.substring(start + 2, end);
            String varValue = getProperty(varName, "");
            value = value.substring(0, start) + varValue + value.substring(end + 1);
            
            start = value.indexOf("${");
        }
        
        return value;
    }
    
    /**
     * 获取整数配置
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取长整数配置
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private static long getLongProperty(String key, long defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取浮点数配置
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private static double getDoubleProperty(String key, double defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔配置
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
    
    // ========== Sandbox 配置 ==========
    
    public static String getSandboxHost() {
        return getProperty("chaos.sandbox.host", DEFAULT_SANDBOX_HOST);
    }
    
    public static int getSandboxPort() {
        return getIntProperty("chaos.sandbox.port", DEFAULT_SANDBOX_PORT);
    }
    
    public static String getSandboxHome() {
        return getProperty("chaos.sandbox.home", DEFAULT_SANDBOX_HOME);
    }
    
    public static String getSandboxScript() {
        return getProperty("chaos.sandbox.script", getSandboxHome() + "/bin/sandbox.sh");
    }
    
    // ========== HTTP 配置 ==========
    
    public static int getHttpConnectTimeout() {
        return getIntProperty("chaos.http.connect.timeout", DEFAULT_HTTP_CONNECT_TIMEOUT);
    }
    
    public static int getHttpReadTimeout() {
        return getIntProperty("chaos.http.read.timeout", DEFAULT_HTTP_READ_TIMEOUT);
    }
    
    public static int getHttpRetryMax() {
        return getIntProperty("chaos.http.retry.max", DEFAULT_HTTP_RETRY_MAX);
    }
    
    public static long getHttpRetryDelay() {
        return getLongProperty("chaos.http.retry.delay", DEFAULT_HTTP_RETRY_DELAY);
    }
    
    public static double getHttpRetryMultiplier() {
        return getDoubleProperty("chaos.http.retry.multiplier", DEFAULT_HTTP_RETRY_MULTIPLIER);
    }
    
    // ========== 日志配置 ==========
    
    public static String getLogLevel() {
        return getProperty("chaos.log.level", DEFAULT_LOG_LEVEL);
    }
    
    public static boolean isLogFileEnabled() {
        return getBooleanProperty("chaos.log.file.enabled", false);
    }
    
    public static String getLogFilePath() {
        return getProperty("chaos.log.file.path", "./logs/chaos.log");
    }
    
    // ========== 测试应用配置 ==========
    
    public static String getTestAppHost() {
        return getProperty("chaos.test.app.host", DEFAULT_TEST_APP_HOST);
    }
    
    public static int getTestAppPort() {
        return getIntProperty("chaos.test.app.port", DEFAULT_TEST_APP_PORT);
    }
    
    public static String getTestAppName() {
        return getProperty("chaos.test.app.name", "demo-app");
    }
    
    public static String getTestAppUrl() {
        return "http://" + getTestAppHost() + ":" + getTestAppPort();
    }
    
    // ========== 实验配置 ==========
    
    public static int getExperimentTimeout() {
        return getIntProperty("chaos.experiment.timeout", 300);
    }
    
    public static boolean isExperimentAutoCleanup() {
        return getBooleanProperty("chaos.experiment.auto.cleanup", true);
    }
    
    // ========== 高级配置 ==========
    
    public static boolean isDebugEnabled() {
        return getBooleanProperty("chaos.debug.enabled", false);
    }
    
    public static boolean isPerformanceMonitorEnabled() {
        return getBooleanProperty("chaos.performance.monitor.enabled", false);
    }
    
    /**
     * 重新加载配置
     */
    public static void reload() {
        loaded = false;
        properties.clear();
        loadConfig();
    }
    
    /**
     * 打印所有配置
     */
    public static void printConfig() {
        log.info("Chaos 配置:");
        log.info("  Sandbox Host: {}", getSandboxHost());
        log.info("  Sandbox Port: {}", getSandboxPort());
        log.info("  Sandbox Home: {}", getSandboxHome());
        log.info("  HTTP Timeout: connect={}ms, read={}ms", 
            getHttpConnectTimeout(), getHttpReadTimeout());
        log.info("  HTTP Retry: max={}, delay={}ms, multiplier={}", 
            getHttpRetryMax(), getHttpRetryDelay(), getHttpRetryMultiplier());
    }
}

