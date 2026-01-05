package com.example.helper.chaos.constant;

/**
 * Chaos 常量定义
 * 
 * 将分散在各处的魔法数字和字符串集中管理
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public class ChaosConstants {
    
    // ==================== Sandbox 相关 ====================
    
    /** Sandbox 端口扫描起始端口（仅当命令查询失败时使用） */
    public static final int PORT_SCAN_START = 54970;
    
    /** Sandbox 端口扫描结束端口（仅当命令查询失败时使用） */
    public static final int PORT_SCAN_END = 55000;
    
    /** Sandbox HTTP 路径前缀 */
    public static final String SANDBOX_PATH_PREFIX = "/sandbox/default/module/http";
    
    /** Sandbox 状态检查路径 */
    public static final String SANDBOX_STATUS_PATH = "/sandbox/default/module/http/chaosblade/status";
    
    // ==================== 超时设置 ====================
    
    /** 端口连接超时（毫秒） */
    public static final int PORT_CONNECT_TIMEOUT_MS = 500;
    
    /** HTTP 请求超时（毫秒） */
    public static final int HTTP_REQUEST_TIMEOUT_MS = 1000;
    
    /** 模块激活等待时间（毫秒） */
    public static final int MODULE_ACTIVATION_WAIT_MS = 200;
    
    /** 命令重试等待时间（毫秒） */
    public static final int COMMAND_RETRY_WAIT_MS = 500;
    
    // ==================== 测试相关 ====================
    
    /** 测试等待时间（秒） */
    public static final int TEST_WAIT_SECONDS = 5;
    
    // ==================== 私有构造函数 ====================
    
    private ChaosConstants() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }
}

