package com.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC 应用发现工具
 * 
 * @author rakkaus
 */
public class RpcAppDiscovery {
    
    private static final Logger log = LoggerFactory.getLogger(RpcAppDiscovery.class);
    
    /**
     * 通过主类名查找 PID
     * 
     * @param mainClass 主类名
     * @return PID，如果未找到返回 -1
     */
    public static int findPidByMainClass(String mainClass) {
        try {
            Process process = Runtime.getRuntime().exec("jps -l");
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(mainClass)) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length > 0) {
                            return Integer.parseInt(parts[0]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查找 PID 失败: {}", e.getMessage());
        }
        return -1;
    }
    
    /**
     * 检查端口是否开放
     * 
     * @param host 主机地址
     * @param port 端口
     * @return 如果端口开放返回 true
     */
    public static boolean isPortOpen(String host, int port) {
        try {
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress(host, port), 1000);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
