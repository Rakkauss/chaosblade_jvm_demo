# ChaosBlade JVM Demo

面向 JVM 的故障注入/混沌工程示例项目，基于开源 ChaosBlade 进行改造，提供快速注入平台和自动化注入能力。被测试注入模拟的业务项目暂不开源。

## 项目概述

本项目在开源 ChaosBlade 的基础上，提供了独立的快速注入平台（`demo-chaos-app`），使得对运行中的业务应用进行故障注入更加便捷。架构上分为**执行引擎**与**注入平台**两层：引擎负责字节码增强与实验管理，注入平台负责自动发现目标应用、自动注入 Sandbox 和远程调用。

## 与开源版本的主要区别

### 1. 快速注入平台

**开源版本**：
- 需要独立的客户端工具（如 `blade` 命令行工具）
- 客户端与业务应用分离
- 需要手动配置 Sandbox 连接信息

**本版本**：
- 提供独立的快速注入平台（`demo-chaos-app`），作为外部工具使用
- 平台与业务应用完全分离，无需修改业务代码
- 支持自动发现目标应用、自动注入 Sandbox 和自动配置连接信息
- 可以打包为 JAR 包，作为独立的注入工具使用

### 2. 自动化注入机制

**开源版本**：
- 需要手动执行 `sandbox.sh` 脚本注入 Sandbox
- 需要手动查找 PID 和端口
- 需要手动激活 ChaosBlade 模块

**本版本**：
- 自动查找目标应用 PID（支持端口、主类名、直接指定三种方式）
- 自动检测 Sandbox 是否已注入，避免重复操作
- 自动注入 Sandbox（如未注入）
- 自动激活 ChaosBlade 模块
- 自动发现 Sandbox 端口（三种策略：版本命令、lsof、端口扫描）

### 3. 零外部依赖

**开源版本**：
- 客户端可能依赖第三方 HTTP 库（如 OkHttp）
- 可能依赖 JSON 处理库（如 Jackson）

**本版本**：
- 客户端仅使用 JDK 8+ 标准库
- 使用 `HttpURLConnection` 进行 HTTP 通信
- 使用 JDK 原生 API 实现 JSON 序列化/反序列化
- 完全零外部依赖，易于集成

### 4. 动态代理 API

**开源版本**：
- 需要手动构造 HTTP 请求
- 需要手动处理 URL、参数、响应解析

**本版本**：
- 使用 Java 动态代理将接口方法调用转换为 HTTP 请求
- 使用 `@Module` 和 `@Command` 注解定义路由规则
- 提供类型安全的 API，代码简洁优雅

### 5. 配置化系统

**开源版本**：
- 配置方式相对简单

**本版本**：
- 提供完整的配置系统（28+ 配置项）
- 支持 4 级配置优先级：系统属性 > 环境变量 > 配置文件 > 默认值
- 支持变量替换和动态配置

### 6. 架构简化

**开源版本**：
- 使用复杂的 Plugin 机制
- 多层抽象（DispatchService → RequestHandler → ModelSpec → Plugin → Enhancer）

**本版本**：
- 简化架构：Handler → Enhancer → 字节码增强
- 减少中间层，代码更直观
- 异步字节码增强，快速响应

## 架构总览

```
注入平台 (demo-chaos-app)
  ├─ 自动发现目标应用（PID/端口/主类名）
  ├─ 自动注入 Sandbox 到目标应用
  ├─ 组装实验请求(Delay/Throws/Mock/HTTP/Dubbo)
  └─ HTTP 调用 -> Sandbox 模块

目标业务应用
  └─ 运行中的 JVM 进程（被注入目标）

Sandbox Module (chaosblade-exec-bootstrap-jvmsandbox)
  ├─ Handler: create/destroy/list/status
  ├─ Enhancer: 注入动作实现
  ├─ Matcher: 目标类/方法匹配
  └─ Listener: 事件回调

Common (chaosblade-exec-common)
  ├─ Model/Action/Matcher 规范
  ├─ 注入执行器与工具
  └─ 共享常量/异常
```

## 模块职责

### 1. chaosblade-exec-bootstrap

JVM-Sandbox 模块入口，负责将外部请求转化为字节码增强行为：

- **Handler 层**：接收 create/destroy/list/status 请求，解析参数并路由到增强逻辑
- **Enhancer 层**：定义具体注入行为（delay/throws/mock/HTTP/RPC）
- **Matcher 层**：负责类与方法的精确匹配
- **Listener 层**：监听方法调用事件并触发注入动作

### 2. chaosblade-exec-common

抽象出稳定的模型与执行规范，供引擎与客户端复用：

- **Model/Spec**：统一描述目标、动作、匹配条件
- **Action/Executor**：动作执行器（延迟、异常、Mock 等）
- **Util/Center**：通用工具与管理组件

### 3. demo-chaos-app

独立的快速注入平台，承担"自动发现 + 自动注入 + 远程调用"职责：

- **自动发现目标应用**：通过端口、主类名或 PID 自动查找目标 JVM 进程
- **自动注入 Sandbox**：自动将 Sandbox 注入到目标应用，无需手动操作
- **自动激活模块**：自动激活 ChaosBlade 模块并发现 Sandbox 端口
- **请求模型**：Delay/Throws/Mock/HTTP/Dubbo 请求对象
- **动态代理**：将接口调用转为 HTTP 请求
- **智能检测**：检测 Sandbox 是否已注入，避免重复操作

## 端到端工作流

1. 注入平台调用 `SandboxAutoSetup.injectSandboxByPort()` 自动发现目标应用并注入 Sandbox
2. 注入平台创建 `Chaos` 客户端实例，连接到目标应用的 Sandbox
3. 注入平台组装请求模型，调用 `chaos.chaosBlade.create(...)`
4. Sandbox 模块的 Handler 解析请求，创建 Enhancer 与 Matcher
5. Listener 拦截目标方法，执行延迟/异常/Mock 动作
6. 注入平台调用 `destroy` 清理实验与增强

## 目录结构

```
chaosblade-jvm-demo/
├── chaosblade-exec-bootstrap/
│   └── chaosblade-exec-bootstrap-jvmsandbox/
│       ├── handler/        # create/destroy/list/status 处理
│       ├── enhancer/       # delay/throws/mock 等增强器
│       ├── listener/       # 事件监听
│       └── matcher/        # 类/方法匹配
├── chaosblade-exec-common/ # 公共模型/动作/工具
└── demo-chaos-app/         # 快速注入平台
    ├── helper/chaos/       # 平台核心
    │   ├── Chaos.java      # 客户端入口
    │   ├── module/         # 模块接口（ChaosBlade）
    │   ├── model/          # 请求模型
    │   ├── proxy/          # 动态代理
    │   └── util/           # 工具类
    └── util/               # 平台工具
        ├── SandboxAutoSetup.java  # Sandbox 自动注入
        └── RpcAppDiscovery.java   # RPC 应用发现
```

## 快速开始

注入平台作为独立工具使用，可以对运行中的业务应用进行故障注入。以下示例展示如何使用注入平台：

### 方式一：通过应用端口注入（推荐）

```java
import com.example.helper.chaos.Chaos;
import com.example.helper.chaos.model.DelayRequest;
import com.example.util.SandboxAutoSetup;

// 1. 注入平台自动发现目标应用并注入 Sandbox（通过应用端口）
int sandboxPort = SandboxAutoSetup.injectSandboxByPort(
    8080,  // 目标业务应用的端口
    "com.example.demo.controller.HelloController",
    "hello",
    "127.0.0.1"
);

// 2. 创建注入平台客户端
Chaos chaos = new Chaos("127.0.0.1", sandboxPort);

// 3. 对目标应用注入延迟故障
String id = chaos.chaosBlade.create(
    DelayRequest.of("com.example.demo.controller.HelloController", "hello", 2000)
);

// 4. 销毁实验
chaos.chaosBlade.destroy(id);
```

### 方式二：通过主类名注入

```java
// 注入平台通过主类名自动发现目标应用
int sandboxPort = SandboxAutoSetup.injectSandboxByMainClass(
    "DemoApplication",  // 目标业务应用的主类名
    "com.example.demo.controller.HelloController",
    "hello",
    "127.0.0.1"
);
Chaos chaos = new Chaos("127.0.0.1", sandboxPort);
```

### 方式三：直接指定 PID

```java
// 注入平台直接使用已知的 PID
int sandboxPort = SandboxAutoSetup.injectSandbox(
    12345,  // 目标业务应用的 PID
    "com.example.demo.controller.HelloController",
    "hello",
    "127.0.0.1"
);
Chaos chaos = new Chaos("127.0.0.1", sandboxPort);
```

## 支持的注入类型

### JVM 方法注入

```java
// 延迟注入
DelayRequest.of("ClassName", "methodName", 2000)

// 异常注入
ThrowsRequest.of("ClassName", "methodName", "java.lang.RuntimeException", "message")

// Mock 注入
MockRequest.of("ClassName", "methodName", "returnValue")
```

### HTTP 客户端注入

```java
// OkHttp3
OkHttp3Request.delay(3000)
OkHttp3Request.throwsException("ExceptionClass", "message")

// RestTemplate
RestTemplateRequest.delay(3000)
RestTemplateRequest.throwsException("ExceptionClass", "message")
```

### HTTP 服务端注入

```java
HttpServerRequest.delay("ControllerClass", "methodName", 2000)
HttpServerRequest.throwsException("ControllerClass", "methodName", "ExceptionClass", "message")
HttpServerRequest.mock("ControllerClass", "methodName", "returnValue")
```

### Dubbo 注入

```java
// Consumer
DubboConsumerRequest.delay(5000)
DubboConsumerRequest.throwsException("ExceptionClass", "message")

// Provider
DubboProviderRequest.delay(3000)
DubboProviderRequest.throwsException("ExceptionClass", "message")
DubboProviderRequest.mock("returnValue")
```

## 技术特点

1. **独立平台**：注入平台与业务应用完全分离，无需修改业务代码
2. **自动化注入**：自动发现目标应用、自动注入 Sandbox、自动激活模块
3. **零外部依赖**：仅使用 JDK 8+，无需第三方库
4. **动态代理**：基于 JDK Proxy 实现接口到 HTTP 的转换
5. **手动 JSON 解析**：使用 JDK 原生 API 实现 JSON 序列化/反序列化
6. **智能检测**：自动检测 Sandbox 注入状态，避免重复操作
7. **异步增强**：字节码增强异步执行，快速响应
8. **配置化**：支持多级配置优先级，灵活配置


## 构建和运行

### 构建项目

```bash
mvn clean package
```

### 运行测试

```bash
cd demo-chaos-app
mvn test -Dtest=AllInjectionTypesTest
```

## 许可证

Apache License 2.0
