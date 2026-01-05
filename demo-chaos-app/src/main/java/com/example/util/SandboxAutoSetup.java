package com.example.util;

import com.example.helper.chaos.config.DiscoveryConfig;
import com.example.helper.chaos.constant.ChaosConstants;
import com.example.helper.chaos.util.SandboxPortValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sandbox 自动设置工具
 * 
 * @author rakkaus
 */
public class SandboxAutoSetup {
    
    private static final Logger log = LoggerFactory.getLogger(SandboxAutoSetup.class);
    
    private static String getSandboxHome() {
        return com.example.helper.chaos.config.ChaosConfig.getSandboxHome();
    }
    
    private static String getSandboxScript() {
        return com.example.helper.chaos.config.ChaosConfig.getSandboxScript();
    }
    
    private static final String JAVA_HOME = System.getenv("JAVA_HOME");
    
    /**
     * 通过应用端口查找 PID 并注入 Sandbox
     * 
     * @param appPort 目标应用的 HTTP 端口
     * @param className 目标类名（用于日志）
     * @param methodName 目标方法名（用于日志）
     * @param sandboxHost Sandbox 主机地址
     * @return Sandbox 端口
     * @throws Exception 如果设置失败
     */
    public static int injectSandboxByPort(int appPort, String className, String methodName, String sandboxHost) throws Exception {
        log.info("通过应用端口查找 PID: appPort={}, className={}, methodName={}", appPort, className, methodName);
        int pid = findPidByPort(appPort);
        if (pid == -1) {
            throw new RuntimeException("未找到占用端口 " + appPort + " 的进程");
        }
        log.info("找到进程: PID={}", pid);
        return injectSandbox(pid, className, methodName, sandboxHost);
    }
    
    /**
     * 通过主类名查找 PID 并注入 Sandbox
     * 
     * @param mainClassName 目标应用主类名
     * @param className 目标类名（用于日志）
     * @param methodName 目标方法名（用于日志）
     * @param sandboxHost Sandbox 主机地址
     * @return Sandbox 端口
     * @throws Exception 如果设置失败
     */
    public static int injectSandboxByMainClass(String mainClassName, String className, String methodName, String sandboxHost) throws Exception {
        log.info("通过主类名查找 PID: mainClassName={}, className={}, methodName={}", mainClassName, className, methodName);
        int pid = findAppPid(mainClassName);
        if (pid == -1) {
            throw new RuntimeException("未找到主类为 " + mainClassName + " 的进程");
        }
        log.info("找到进程: PID={}", pid);
        return injectSandbox(pid, className, methodName, sandboxHost);
    }
    
    /**
     * 直接通过 PID 注入 Sandbox
     * 
     * @param pid 目标进程 PID
     * @param className 目标类名（用于日志）
     * @param methodName 目标方法名（用于日志）
     * @param sandboxHost Sandbox 主机地址
     * @return Sandbox 端口
     * @throws Exception 如果设置失败
     */
    public static int injectSandbox(int pid, String className, String methodName, String sandboxHost) throws Exception {
        log.info("检查 Sandbox 注入状态: PID={}, className={}, methodName={}", pid, className, methodName);
        
        // 先检查是否已经注入
        int existingPort = checkSandboxInjected(pid, sandboxHost);
        if (existingPort > 0) {
            log.info("Sandbox 已注入，端口: {}", existingPort);
            // 确保 ChaosBlade 模块已激活
            try {
                activateChaosBlade(pid);
                log.info("ChaosBlade 模块已激活");
            } catch (Exception e) {
                log.warn("激活 ChaosBlade 模块失败（可能已激活）: {}", e.getMessage());
            }
            return existingPort;
        }
        
        log.info("Sandbox 未注入，开始注入: PID={}", pid);
        injectSandboxInternal(pid);
        log.info("Sandbox 注入成功");
        
        refreshModules(pid);
        log.info("模块刷新成功");
        
        activateChaosBlade(pid);
        log.info("ChaosBlade 模块已激活");
        
        int port = getSandboxPort(pid, sandboxHost);
        log.info("Sandbox 端口: {}", port);
        
        return port;
    }
    
    /**
     * 检查 Sandbox 是否已经注入到指定进程
     * 
     * @param pid 目标进程 PID
     * @param sandboxHost Sandbox 主机地址
     * @return Sandbox 端口，如果未注入返回 -1
     */
    private static int checkSandboxInjected(int pid, String sandboxHost) {
        try {
            // 尝试获取 Sandbox 端口
            int port = findPortBySandboxVersion(pid);
            if (port > 0 && isSandboxPort(sandboxHost, port)) {
                return port;
            }
            
            // 尝试通过 lsof 查找
            port = findPortByPid(pid, sandboxHost);
            if (port > 0 && isSandboxPort(sandboxHost, port)) {
                return port;
            }
        } catch (Exception e) {
            log.debug("检查 Sandbox 注入状态失败: {}", e.getMessage());
        }
        return -1;
    }
    
    /**
     * 通过应用端口查找 PID
     * 
     * @param appPort 应用端口
     * @return PID，如果未找到返回 -1
     */
    private static int findPidByPort(int appPort) {
        try {
            Process process = Runtime.getRuntime().exec(
                new String[]{"lsof", "-i", ":" + appPort}
            );
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                // 跳过第一行（标题行）
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    if (line.contains("LISTEN")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length > 1) {
                            try {
                                return Integer.parseInt(parts[1]);
                            } catch (NumberFormatException e) {
                                // 继续查找
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("通过端口查找 PID 失败: {}", e.getMessage());
        }
        return -1;
    }
    
    /**
     * 通过主类名查找 PID
     * 
     * @param mainClassName 主类名
     * @return PID，如果未找到返回 -1
     */
    private static int findAppPid(String mainClassName) throws Exception {
        Process process = Runtime.getRuntime().exec("jps -l");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(mainClassName)) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 0) {
                        return Integer.parseInt(parts[0]);
                    }
                }
            }
        }
        return -1;
    }
    
    private static void injectSandboxInternal(int pid) throws Exception {
        executeCommand(getSandboxScript(), "-p", String.valueOf(pid));
    }
    
    private static void refreshModules(int pid) throws Exception {
        executeCommand(getSandboxScript(), "-p", String.valueOf(pid), "-F");
    }
    
    private static void activateChaosBlade(int pid) throws Exception {
        executeCommand(getSandboxScript(), "-p", String.valueOf(pid), "-a", "chaosblade");
    }
    
    private static int getSandboxPort(int pid, String sandboxHost) throws Exception {
        // 方法1：sandbox.sh -v 解析（最准确，优先使用）
        int port = findPortBySandboxVersion(pid);
        if (port > 0 && isSandboxPort(sandboxHost, port)) {
            log.info("从 sandbox.sh -v 获取 Sandbox 端口: {}", port);
            return port;
        }
        
        // 方法2：lsof 查找
        port = findPortByPid(pid, sandboxHost);
        if (port > 0 && isSandboxPort(sandboxHost, port)) {
            log.info("从 lsof 获取 Sandbox 端口: {}", port);
            return port;
        }
        
        // 方法3：端口范围扫描
        port = findPortByScanning(sandboxHost);
        if (port > 0 && isSandboxPort(sandboxHost, port)) {
            log.info("从端口扫描获取 Sandbox 端口: {}", port);
            return port;
        }
        
        throw new RuntimeException("无法找到有效的 Sandbox 端口。请确保：" +
            "\n1. Sandbox 已正确注入到进程 " + pid);
        }
    
    private static int findPortByPid(int pid, String host) {
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
                            if (isSandboxPort(host, port)) {
                return port;
            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        return -1;
            }
    
    private static int findPortBySandboxVersion(int pid) {
        try {
            Process process = executeCommand(getSandboxScript(), "-p", String.valueOf(pid), "-v");
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            String outputStr = output.toString();
            Pattern pattern = Pattern.compile("SERVER_PORT\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(outputStr);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // 忽略
        }
        return -1;
            }
    
    private static int findPortByKnownPorts(String host) {
        // 不再使用硬编码的已知端口列表
        // 直接使用端口范围扫描（更通用）
        return -1;
                }
    
    private static int findPortByScanning(String host) {
        // 使用常量定义的端口范围（仅当命令查询失败时使用）
        for (int port = ChaosConstants.PORT_SCAN_START; port <= ChaosConstants.PORT_SCAN_END; port++) {
            if (isPortOpen(host, port) && isSandboxPort(host, port)) {
                    return port;
                }
        }
        return -1;
    }
    
    private static boolean isPortOpen(String host, int port) {
        try {
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress(host, port), 500);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean isSandboxPort(String host, int port) {
        return SandboxPortValidator.isSandboxPort(host, port);
                                }
    
    private static Process executeCommand(String... command) throws Exception {
        String javaHomeValue = JAVA_HOME != null && !JAVA_HOME.isEmpty() 
            ? JAVA_HOME : getJavaHome();
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("JAVA_HOME", javaHomeValue);
        String sandboxHomePath = getSandboxHome().replace("${user.home}", 
            System.getProperty("user.home"));
        pb.environment().put("SANDBOX_HOME_DIR", sandboxHomePath);
        pb.directory(new File(sandboxHomePath + "/bin"));
        pb.redirectErrorStream(false);
        
        Process process = pb.start();
        process.waitFor();
        return process;
    }
    
    private static String getJavaHome() throws Exception {
        try {
            Process process = Runtime.getRuntime().exec("/usr/libexec/java_home -v 1.8");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String javaHome = reader.readLine();
                if (javaHome != null && !javaHome.isEmpty()) {
                    return javaHome.trim();
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            File jreDir = new File(javaHome);
            if (jreDir.getName().equals("jre")) {
                return jreDir.getParent();
            }
            return javaHome;
        }
        
        throw new RuntimeException("无法获取 JAVA_HOME");
    }
}
