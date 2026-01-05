package com.example.helper.chaos.service.impl;

import com.example.helper.chaos.service.PortDiscovery;
import com.example.helper.chaos.util.SandboxPortValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于 lsof 的端口发现策略（优先级最高）
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public class LsofPortDiscovery implements PortDiscovery {
    
    private static final Logger log = LoggerFactory.getLogger(LsofPortDiscovery.class);
    
    @Override
    public int findPort(int pid, String host) {
        try {
            Process process = Runtime.getRuntime().exec(
                new String[]{"lsof", "-i", "-P", "-a", "-p", String.valueOf(pid)}
            );
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("LISTEN")) {
                        Pattern pattern = Pattern.compile("\\*:(\\d+)");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            int port = Integer.parseInt(matcher.group(1));
                            // 排除系统端口和动态端口范围，只验证是否是 Sandbox 端口
                            if (SandboxPortValidator.isExcludedPort(port)) {
                                continue;
                            }
                            // 通过 HTTP 验证是否是 Sandbox 端口（更可靠）
                            if (SandboxPortValidator.isSandboxPort(host, port)) {
                                return port;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("lsof 查找端口失败: {}", e.getMessage());
        }
        return -1;
    }
    
    
    @Override
    public String getStrategyName() {
        return "lsof";
    }
    
    @Override
    public int getPriority() {
        return 1;  // 最高优先级
    }
}

