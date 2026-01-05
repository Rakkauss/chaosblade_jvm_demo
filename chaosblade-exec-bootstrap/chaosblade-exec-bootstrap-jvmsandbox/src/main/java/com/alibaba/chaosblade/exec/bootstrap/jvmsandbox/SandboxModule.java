/*
 * Copyright 2025 The ChaosBlade Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.chaosblade.exec.bootstrap.jvmsandbox;


import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.handler.Handler;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.handler.CreateHandler;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.handler.DestroyHandler;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.handler.StatusHandler;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.handler.ListHandler;

import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.Enhancer;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.DelayEnhancer;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.ThrowsEnhancer;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.MockEnhancer;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.Okhttp3Enhancer;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.RestTemplateEnhancer;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.HttpServerEnhancer;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.DubboConsumerEnhancer;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.DubboProviderEnhancer;
import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.enhancer.DynamicDispatcherEnhancer;

import com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.listener.ListenerManager;

import com.alibaba.chaosblade.exec.common.transport.Request;
import com.alibaba.chaosblade.exec.common.transport.Response;
import com.alibaba.chaosblade.exec.common.transport.Response.Code;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.LoadCompleted;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.ModuleLifecycle;
import com.alibaba.jvm.sandbox.api.http.Http;
import com.alibaba.jvm.sandbox.api.annotation.Command;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplified Chaosblade Sandbox Module
 * 
 * [改造说明]
 * 1. 移除Plugin体系，改为内置Enhancer
 * 2. 移除DispatchService，改为简化的Handler静态分发
 * 3. 移除ManagerFactory，使用简单Map管理实验
 * @author rakkaus (simplified)
 */
@Information(id = "chaosblade", version = "1.8.0", author = "rakkaus", isActiveOnLoad = false)
public class SandboxModule implements Module, ModuleLifecycle, LoadCompleted {

  private static Logger LOGGER = LoggerFactory.getLogger(SandboxModule.class);
  
  /** cache sandbox watch id */
  private static Map<String, Integer> watchIds = new ConcurrentHashMap<String, Integer>();

  @Resource 
  private ModuleEventWatcher moduleEventWatcher;

  @Override
  public void onLoad() throws Throwable {
    LOGGER.info("加载 chaosblade 模块 - 版本 1.8.0");
    ListenerManager.setModuleEventWatcher(moduleEventWatcher);
  }

  @Override
  public void onUnload() throws Throwable {
    LOGGER.info("卸载 chaosblade 模块");
    ListenerManager.clear();
    watchIds.clear();
    LOGGER.info("chaosblade 模块卸载成功");
  }

  @Override
  public void onActive() throws Throwable {
    LOGGER.info("激活 chaosblade 模块");
    registerHandlers();
    registerEnhancers();
  }

  @Override
  public void onFrozen() throws Throwable {
    LOGGER.info("冻结 chaosblade 模块");
  }

  @Override
  public void loadCompleted() {
    LOGGER.info("chaosblade 模块加载完成");
  }

  /**
   * 新增：注册 Handler
   * 通过实例化 Handler 子类，触发构造函数自动注册
   */
  private void registerHandlers() {
    LOGGER.info("正在注册处理器...");
    new CreateHandler();
    new DestroyHandler();
    new StatusHandler();
    new ListHandler();
    LOGGER.info("处理器注册完成: {}", Handler.getRegisteredHandlers());
  }

  private void registerEnhancers() {
    LOGGER.info("正在注册内置增强器...");
    
    Enhancer.register("delay", DelayEnhancer.class);
    Enhancer.register("throws", ThrowsEnhancer.class);
    Enhancer.register("mock", MockEnhancer.class);
    Enhancer.register("okhttp3", Okhttp3Enhancer.class);
    Enhancer.register("resttemplate", RestTemplateEnhancer.class);
    Enhancer.register("httpserver", HttpServerEnhancer.class);
    Enhancer.register("dubbo-consumer", DubboConsumerEnhancer.class);
    Enhancer.register("dubbo-provider", DubboProviderEnhancer.class);
    Enhancer.register("dynamic", DynamicDispatcherEnhancer.class);
    
    LOGGER.info("内置增强器注册完成: delay, throws, mock, okhttp3, resttemplate, httpserver, dubbo-consumer, dubbo-provider, dynamic");
  }

  /**
   * 使用@Command注解定义HTTP端点
   * Sandbox会自动将这些方法注册为HTTP端点
   * URL格式: http://127.0.0.1:<PORT>/sandbox/default/module/http/chaosblade/<COMMAND>
   */
  
  @Command("create")
  public void create(HttpServletRequest request, HttpServletResponse response) {
    service("create", request, response);
  }

  @Command("destroy")
  public void destroy(HttpServletRequest request, HttpServletResponse response) {
    service("destroy", request, response);
  }
  
  @Command("status")
  public void status(HttpServletRequest request, HttpServletResponse response) {
    service("status", request, response);
  }
  
  @Command("list")
  public void list(HttpServletRequest request, HttpServletResponse response) {
    service("list", request, response);
  }

  private void service(
      String command,
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse) {
    Request request;
    
    // 根据请求方法解析参数
    if ("POST".equalsIgnoreCase(httpServletRequest.getMethod())) {
      try {
        // POST请求：从Body解析JSON
        request = getRequestFromBody(httpServletRequest);
      } catch (IOException e) {
        Response response = Response.ofFailure(
            Code.ILLEGAL_PARAMETER, 
            "解析 JSON 请求体失败: " + e.getMessage()
        );
        output(httpServletResponse, response);
        return;
      }
    } else {
      // GET请求：从Query String解析参数
      request = getRequestFromParams(httpServletRequest);
    }
    
    // 分发到具体的Handler
    Response response = Handler.dispatch(command, request.getParams());
    
    // 输出响应
    output(httpServletResponse, response);
  }

  /**
   * 输出HTTP响应
   * 
   * @param httpServletResponse HTTP响应对象
   * @param response 业务响应对象
   */
  private void output(HttpServletResponse httpServletResponse, Response response) {
    httpServletResponse.setContentType("application/json;charset=UTF-8");
    
    PrintWriter writer = null;
    try {
      writer = httpServletResponse.getWriter();
      writer.println(response.toString());
      writer.flush();
    } catch (Exception e) {
      LOGGER.warn("写入响应失败", e);
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  /**
   * 从Query String解析参数（GET请求）
   * 
   * @param httpServletRequest HTTP请求对象
   * @return Request对象
   */
  private Request getRequestFromParams(HttpServletRequest httpServletRequest) {
    Request request = new Request();
    Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
    
    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
      String value = "";
      String[] values = entry.getValue();
      if (values != null && values.length > 0) {
        value = values[0];
      }
      request.addParam(entry.getKey(), value);
    }
    
    return request;
  }

  /**
   * 从JSON Body解析参数（POST请求）
   * 
   * @param httpServletRequest HTTP请求对象
   * @return Request对象
   * @throws IOException JSON解析异常
   */
  private Request getRequestFromBody(HttpServletRequest httpServletRequest) throws IOException {
    ServletInputStream inputStream = httpServletRequest.getInputStream();
    
    MapType mapType = TypeFactory.defaultInstance()
        .constructMapType(HashMap.class, String.class, String.class);
    Map<String, String> parameters = new ObjectMapper().readValue(inputStream, mapType);
    
    Request request = new Request();
    request.addParams(parameters);
    return request;
  }
}
