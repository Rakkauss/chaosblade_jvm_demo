package com.example;

import com.example.helper.chaos.Chaos;
import com.example.helper.chaos.model.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 所有注入类型测试类（完整版）
 * 
 * 运行所有测试：
 * mvn test -Dtest=AllInjectionTypesTest
 * 
 * 运行单个测试：
 * mvn test -Dtest=AllInjectionTypesTest#testJvmDelay
 *
 * @author rakkaus
 * @since 1.0.0
 */
public class AllInjectionTypesTest extends InjectionTestBase {
    
    /** Chaos 客户端 - demo-app */
    private static Chaos chaosForDemoApp;
    
    /** Chaos 客户端 - RPC Provider */
    private static Chaos chaosForRpcProvider;
    
    /** Chaos 客户端 - RPC Consumer */
    private static Chaos chaosForRpcConsumer;
    
    /** 当前实验 ID（用于清理） */
    private String currentExperimentId;
    
    /** 当前使用的 Chaos 客户端 */
    private Chaos currentChaos;
    
    /**
     * 测试前初始化（只执行一次）
     * 
     * 支持多种初始化方式：
     * 1. 通过应用端口自动注入（推荐）
     * 2. 通过主类名自动注入
     * 3. 如果 Sandbox 已注入，直接指定端口
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        log.info("========== 开始初始化测试环境 ==========");
        
        // 初始化 demo-app 客户端
        // 方式1：通过应用端口自动注入（推荐）
        chaosForDemoApp = initChaosByPort(
            TestConfig.DemoApp.HTTP_PORT,
            TestConfig.DemoApp.TARGET_CLASS,
            TestConfig.DemoApp.TARGET_METHOD
        );
        
        // 如果方式1失败，尝试方式2：通过主类名注入
        if (chaosForDemoApp == null) {
            chaosForDemoApp = initChaosByMainClass(
                TestConfig.DemoApp.MAIN_CLASS,
                TestConfig.DemoApp.TARGET_CLASS,
                TestConfig.DemoApp.TARGET_METHOD
            );
        }
        
        // 如果方式2也失败，尝试方式3：如果 Sandbox 已注入，直接指定端口（需要手动修改端口号）
        if (chaosForDemoApp == null) {
            log.warn("自动注入失败，如果 Sandbox 已注入，可以取消注释以下代码并指定正确的端口：");
            log.warn("chaosForDemoApp = initChaosBySandboxPort(54973);");
            // chaosForDemoApp = initChaosBySandboxPort(54973);
        }
        
        // 初始化 RPC Provider 客户端
        log.info("初始化 RPC Provider Chaos 客户端");
        chaosForRpcProvider = initChaosByMainClass(
            TestConfig.RpcProvider.MAIN_CLASS,
            TestConfig.RpcProvider.TARGET_CLASS,
            TestConfig.RpcProvider.TARGET_METHOD
        );
        
        // 如果主类名方式失败，尝试应用端口方式
        if (chaosForRpcProvider == null) {
            chaosForRpcProvider = initChaosByPort(
                TestConfig.RpcProvider.HTTP_PORT,
                TestConfig.RpcProvider.TARGET_CLASS,
                TestConfig.RpcProvider.TARGET_METHOD
            );
        }
        
        // 初始化 RPC Consumer 客户端
        log.info("初始化 RPC Consumer Chaos 客户端");
        chaosForRpcConsumer = initChaosByPort(
            TestConfig.RpcConsumer.HTTP_PORT,
            TestConfig.RpcConsumer.TARGET_CLASS,
            TestConfig.RpcConsumer.TARGET_METHOD
        );
        
        // 如果应用端口方式失败，尝试主类名方式
        if (chaosForRpcConsumer == null) {
            chaosForRpcConsumer = initChaosByMainClass(
                TestConfig.RpcConsumer.MAIN_CLASS,
                TestConfig.RpcConsumer.TARGET_CLASS,
                TestConfig.RpcConsumer.TARGET_METHOD
            );
        }
        
        log.info("========== 测试环境初始化完成 ==========");
        log.info("demo-app 客户端: {}", chaosForDemoApp != null ? "已连接" : "未连接");
        log.info("RPC Provider 客户端: {}", chaosForRpcProvider != null ? "已连接" : "未连接");
        log.info("RPC Consumer 客户端: {}", chaosForRpcConsumer != null ? "已连接" : "未连接");
    }
    
    /**
     * 测试后清理（每个测试方法执行后）
     */
    @After
    public void tearDown() throws Exception {
        if (currentExperimentId != null && !currentExperimentId.isEmpty() && currentChaos != null) {
            try {
                currentChaos.chaosBlade.destroy(currentExperimentId);
                log.info("实验已销毁: {}", currentExperimentId);
            } catch (Exception e) {
                log.error("销毁实验失败: {}", e.getMessage());
            }
            currentExperimentId = null;
            currentChaos = null;
        }
    }
    
    // ========================================
    // JVM 方法注入测试（demo-app）
    // ========================================
    
    /**
     * 测试 1：JVM 方法延迟注入
     */
    @Test
    public void testJvmDelay() throws Exception {
        if (chaosForDemoApp == null) {
            log.warn("跳过测试：demo-app 客户端未初始化");
            return;
        }
        
        log.info("【测试 1】JVM 方法延迟注入（demo-app）");
        log.info("目标应用: demo-app, Sandbox: {}:{}", chaosForDemoApp.getHost(), chaosForDemoApp.getPort());
        log.info("注入参数: className={}, methodName={}, time=2000ms", 
            TestConfig.DemoApp.TARGET_CLASS, TestConfig.DemoApp.TARGET_METHOD);
        
        currentChaos = chaosForDemoApp;
        DelayRequest request = DelayRequest.of(
            TestConfig.DemoApp.TARGET_CLASS, 
            TestConfig.DemoApp.TARGET_METHOD, 
            2000
        );
        
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: http://localhost:8080/hello (应该会延迟 2 秒)", currentExperimentId);
        
        Thread.sleep(TEST_WAIT_SECONDS * 1000L);
    }
    
    /**
     * 测试 2：JVM 方法异常注入
     */
    @Test
    public void testJvmThrows() throws Exception {
        if (chaosForDemoApp == null) {
            log.warn("跳过测试：demo-app 客户端未初始化");
            return;
        }
        
        log.info("【测试 2】JVM 方法异常注入（demo-app）");
        log.info("目标应用: demo-app, Sandbox: {}:{}", chaosForDemoApp.getHost(), chaosForDemoApp.getPort());
        log.info("注入参数: className={}, methodName={}, exception=java.lang.RuntimeException, exceptionMessage=测试异常注入", 
            TestConfig.DemoApp.TARGET_CLASS, TestConfig.DemoApp.TARGET_METHOD);
        
        currentChaos = chaosForDemoApp;
        ThrowsRequest request = ThrowsRequest.of(
            TestConfig.DemoApp.TARGET_CLASS,
            TestConfig.DemoApp.TARGET_METHOD,
            "java.lang.RuntimeException",
            "测试异常注入"
        );
        
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: http://localhost:8080/hello (应该会返回 500 错误)", currentExperimentId);
        
        Thread.sleep(TEST_WAIT_SECONDS * 3000L);
    }
    
    /**
     * 测试 3：JVM 方法 Mock 注入
     */
    @Test
    public void testJvmMock() throws Exception {
        if (chaosForDemoApp == null) {
            log.warn("跳过测试：demo-app 客户端未初始化");
            return;
        }
        
        log.info("【测试 3】JVM 方法 Mock 注入（demo-app）");
        log.info("目标应用: demo-app, Sandbox: {}:{}", chaosForDemoApp.getHost(), chaosForDemoApp.getPort());
        log.info("注入参数: className={}, methodName={}, value=mock",
            TestConfig.DemoApp.TARGET_CLASS, TestConfig.DemoApp.TARGET_METHOD);
        
        currentChaos = chaosForDemoApp;
        MockRequest request = MockRequest.of(
            TestConfig.DemoApp.TARGET_CLASS, 
            TestConfig.DemoApp.TARGET_METHOD, 
            "mock"
        );
        
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: http://localhost:8080/hello (应该会返回 Mock 值)", currentExperimentId);
        
        Thread.sleep(TEST_WAIT_SECONDS * 3000L);
    }
    
    // ========================================
    // HTTP 客户端注入测试（demo-app）
    // ========================================
    
    /**
     * 测试 4：OkHttp3 延迟注入
     */
    @Test
    public void testOkHttp3Delay() throws Exception {
        if (chaosForDemoApp == null) {
            log.warn("跳过测试：demo-app 客户端未初始化");
            return;
        }
        
        log.info("【测试 4】OkHttp3 延迟注入（demo-app）");
        log.info("目标应用: demo-app, Sandbox: {}:{}", chaosForDemoApp.getHost(), chaosForDemoApp.getPort());
        log.info("注入参数: target=okhttp3, action=delay, time=2000ms");
        
        currentChaos = chaosForDemoApp;
        OkHttp3Request request = OkHttp3Request.delay(2000);
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: http://localhost:8080/api/call-okhttp ", currentExperimentId);
        
        Thread.sleep(TEST_WAIT_SECONDS * 1000L);
    }
    
    /**
     * 测试 5：RestTemplate 异常注入
     */
    @Test
    public void testRestTemplateThrows() throws Exception {
        if (chaosForDemoApp == null) {
            log.warn("跳过测试：demo-app 客户端未初始化");
            return;
        }
        
        log.info("【测试 5】RestTemplate 异常注入（demo-app）");
        log.info("目标应用: demo-app, Sandbox: {}:{}", chaosForDemoApp.getHost(), chaosForDemoApp.getPort());
        log.info("注入参数: target=resttemplate, action=throws, exception=java.net.SocketTimeoutException, exceptionMessage=Read timed out");
        
        currentChaos = chaosForDemoApp;
        RestTemplateRequest request = RestTemplateRequest.throwsException(
            "java.net.SocketTimeoutException",
            "Read timed out"
        );
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: http://localhost:8080/api/call-rest", currentExperimentId);
        
        Thread.sleep(TEST_WAIT_SECONDS * 1000L);
    }
    
    // ========================================
    // HTTP 服务端注入测试（demo-app）
    // ========================================
    
    /**
     * 测试 6：HTTP 服务端延迟注入
     */
    @Test
    public void testHttpServerDelay() throws Exception {
        if (chaosForDemoApp == null) {
            log.warn("跳过测试：demo-app 客户端未初始化");
            return;
        }
        
        log.info("【测试 6】HTTP 服务端延迟注入（demo-app）");
        log.info("目标应用: demo-app, Sandbox: {}:{}", chaosForDemoApp.getHost(), chaosForDemoApp.getPort());
        log.info("注入参数: target=httpserver, action=delay, time=3000ms");
        
        currentChaos = chaosForDemoApp;
        HttpServerRequest request = HttpServerRequest.delay(3000);
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: http://localhost:8080/api/call-rest", currentExperimentId);
        log.info("注入成功，实验ID: {}, 验证: http://localhost:8080/hello (应该会返回 500 错误)", currentExperimentId);

        Thread.sleep(TEST_WAIT_SECONDS * 1000L);
    }
    
    /**
     * 测试 7：HTTP 服务端异常注入
     */
    @Test
    public void testHttpServerThrows() throws Exception {
        if (chaosForDemoApp == null) {
            log.warn("跳过测试：demo-app 客户端未初始化");
            return;
        }
        
        log.info("【测试 7】HTTP 服务端异常注入（demo-app）");
        log.info("目标应用: demo-app, Sandbox: {}:{}", chaosForDemoApp.getHost(), chaosForDemoApp.getPort());
        log.info("注入参数: target=httpserver, action=throws, exception=java.lang.RuntimeException, exceptionMessage=Service unavailable");
        
        currentChaos = chaosForDemoApp;
        HttpServerRequest request = HttpServerRequest.throwsException(
            "java.lang.RuntimeException",
            "Service unavailable"
        );
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: http://localhost:8080/api/call-rest", currentExperimentId);
        log.info("注入成功，实验ID: {}, 验证: http://localhost:8080/hello (应该会返回 500 错误)", currentExperimentId);

        Thread.sleep(TEST_WAIT_SECONDS * 1000L);
    }
    
    /**
     * 测试 8：HTTP 服务端 Mock 注入
     */
    @Test
    public void testHttpServerMock() throws Exception {
        if (chaosForDemoApp == null) {
            log.warn("跳过测试：demo-app 客户端未初始化");
            return;
        }
        
        log.info("【测试 8】HTTP 服务端 Mock 注入（demo-app）");
        log.info("目标应用: demo-app, Sandbox: {}:{}", chaosForDemoApp.getHost(), chaosForDemoApp.getPort());
        log.info("注入参数: target=httpserver, action=mock, value={\"code\":500,\"message\":\"Mock error\"}, type=java.lang.String");
        
        currentChaos = chaosForDemoApp;
        HttpServerRequest request = HttpServerRequest.mock(
            "{\"code\":500,\"message\":\"Mock error\"}",
            "java.lang.String"
        );
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 预期: 所有 HTTP 服务端请求都会返回 Mock 值", currentExperimentId);
        
        Thread.sleep(TEST_WAIT_SECONDS * 1000L);
    }
    
    // ========================================
    // Dubbo 消费者注入测试（RpcConsumerApplication）
    // ========================================
    
    /**
     * 测试 9：Dubbo 消费者延迟注入
     */
    @Test
    public void testDubboConsumerDelay() throws Exception {
        if (chaosForRpcConsumer == null) {
            log.warn("跳过测试：RPC Consumer 客户端未初始化");
            return;
        }
        
        log.info("【测试 9】Dubbo 消费者延迟注入（RpcConsumerApplication）");
        log.info("目标应用: RpcConsumerApplication (端口 8081), Sandbox: {}:{}", chaosForRpcConsumer.getHost(), chaosForRpcConsumer.getPort());
        log.info("注入参数: target=dubbo-consumer, action=delay, time=5000ms");
        
        currentChaos = chaosForRpcConsumer;
        DubboConsumerRequest request = DubboConsumerRequest.delay(5000);
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: curl http://localhost:8081/user/1 (所有 Dubbo 消费者调用都会延迟 5 秒)", currentExperimentId);
        
        Thread.sleep(TEST_WAIT_SECONDS * 1000L);
    }
    
    /**
     * 测试 10：Dubbo 消费者异常注入
     */
    @Test
    public void testDubboConsumerThrows() throws Exception {
        if (chaosForRpcConsumer == null) {
            log.warn("跳过测试：RPC Consumer 客户端未初始化");
            return;
        }
        
        log.info("【测试 10】Dubbo 消费者异常注入（RpcConsumerApplication）");
        log.info("目标应用: RpcConsumerApplication (端口 8081), Sandbox: {}:{}", chaosForRpcConsumer.getHost(), chaosForRpcConsumer.getPort());
        log.info("注入参数: target=dubbo-consumer, action=throws, exception=com.alibaba.dubbo.rpc.RpcException, exceptionMessage=Service not available");
        
        currentChaos = chaosForRpcConsumer;
        DubboConsumerRequest request = DubboConsumerRequest.throwsException(
            "com.alibaba.dubbo.rpc.RpcException",
            "Service not available"
        );
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: curl http://localhost:8081/user/1 (所有 Dubbo 消费者调用都会抛出 RPC 异常)", currentExperimentId);
        
        Thread.sleep(TEST_WAIT_SECONDS * 1000L);
    }
    
    // ========================================
    // Dubbo 提供者注入测试（RpcProviderApplication）
    // ========================================
    
    /**
     * 测试 11：Dubbo 提供者延迟注入
     */
    @Test
    public void testDubboProviderDelay() throws Exception {
        if (chaosForRpcProvider == null) {
            log.warn("跳过测试：RPC Provider 客户端未初始化");
            return;
        }
        
        log.info("【测试 11】Dubbo 提供者延迟注入（RpcProviderApplication）");
        log.info("目标应用: RpcProviderApplication (端口 8090, Dubbo 端口 20880), Sandbox: {}:{}", chaosForRpcProvider.getHost(), chaosForRpcProvider.getPort());
        log.info("注入参数: target=dubbo-provider, action=delay, time=3000ms");
        
        currentChaos = chaosForRpcProvider;
        DubboProviderRequest request = DubboProviderRequest.delay(3000);
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: curl http://localhost:8081/user/1 (通过 Consumer 调用，所有 Dubbo 提供者处理都会延迟 3 秒)", currentExperimentId);
        
        Thread.sleep(TEST_WAIT_SECONDS * 1000L);
    }
    
    /**
     * 测试 12：Dubbo 提供者异常注入
     */
    @Test
    public void testDubboProviderThrows() throws Exception {
        if (chaosForRpcProvider == null) {
            log.warn("跳过测试：RPC Provider 客户端未初始化");
            return;
        }
        
        log.info("【测试 12】Dubbo 提供者异常注入（RpcProviderApplication）");
        log.info("目标应用: RpcProviderApplication (端口 8090, Dubbo 端口 20880), Sandbox: {}:{}", chaosForRpcProvider.getHost(), chaosForRpcProvider.getPort());
        log.info("注入参数: target=dubbo-provider, action=throws, exception=java.lang.RuntimeException, exceptionMessage=Database connection failed");
        
        currentChaos = chaosForRpcProvider;
        DubboProviderRequest request = DubboProviderRequest.throwsException(
            "java.lang.RuntimeException",
            "Database connection failed"
        );
        currentExperimentId = currentChaos.chaosBlade.create(request);
        log.info("注入成功，实验ID: {}, 验证: curl http://localhost:8081/user/1 (通过 Consumer 调用，所有 Dubbo 提供者处理都会抛出异常)", currentExperimentId);
        
        Thread.sleep(TEST_WAIT_SECONDS * 1000L);
    }
    
    // 注意：Dubbo Provider Mock 注入暂未实现，待后续补充
    // @Test
    // public void testDubboProviderMock() throws Exception { ... }
}
