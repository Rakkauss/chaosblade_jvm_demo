package com.example.helper.chaos.util;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 工具类（使用 OkHttp 实现）
 * 
 * @author rakkaus
 */
public class HttpUtil {
    
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType FORM_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded");
    
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(new LoggingInterceptor())
        .build();
    
    /**
     * 发送 POST 请求（JSON 格式）
     * 
     * @param url URL
     * @param jsonBody JSON 请求体
     * @return 响应内容
     */
    public static String post(String url, String jsonBody) {
        RequestBody body = RequestBody.create(jsonBody, JSON_MEDIA_TYPE);
        
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        try (Response response = CLIENT.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String responseText = responseBody != null ? responseBody.string() : "";
            
            if (!response.isSuccessful()) {
                // 即使状态码不是 2xx，也返回响应内容（Sandbox 可能返回 JSON 格式的错误信息）
                log.warn("HTTP POST 请求返回非成功状态码: {} {}, 响应内容: {}", 
                    response.code(), response.message(), responseText);
                
                // 如果是 404，可能是端口错误（连接到了应用而不是 Sandbox）
                if (response.code() == 404) {
                    // 检查是否是 Sandbox 返回的 JSON 错误
                    if (responseText != null && (responseText.contains("\"success\"") || responseText.contains("\"error\""))) {
                        // 是 Sandbox 返回的 JSON，直接返回让上层处理
                        return responseText;
                    } else {
                        // 不是 Sandbox 响应，可能是端口错误
                        throw new IOException("请求失败: 404 Not Found。可能原因：" +
                            "\n1. 端口错误（连接到了应用端口而不是 Sandbox 端口）" +
                            "\n2. Sandbox 未正确注入或模块未激活" +
                            "\n3. URL 路径错误: " + url);
                    }
                }
                
                // 其他错误状态码，返回响应内容让上层处理
                return responseText;
            }
            
            return responseText;
            
        } catch (IOException e) {
            log.error("HTTP POST 请求失败: {}", url, e);
            throw ChaosException.wrap("HTTP POST 请求失败", e);
        }
    }
    
    /**
     * 发送 GET 请求
     * 
     * @param url URL
     * @return 响应内容
     */
    public static String get(String url) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        
        try (Response response = CLIENT.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String responseText = responseBody != null ? responseBody.string() : "";
            
            if (!response.isSuccessful()) {
                return handleHttpError(response, responseText, url);
            }
            
            return responseText;
            
        } catch (IOException e) {
            log.error("HTTP GET 请求失败: {}", url, e);
            throw ChaosException.wrap("HTTP GET 请求失败", e);
        }
    }
    
    /**
     * 统一处理 HTTP 错误响应
     * 
     * @param response HTTP 响应
     * @param responseText 响应内容
     * @param url 请求 URL
     * @return 响应内容（可能是错误信息）
     * @throws IOException 如果是非 Sandbox 错误
     */
    private static String handleHttpError(Response response, String responseText, String url) throws IOException {
        // 即使状态码不是 2xx，也返回响应内容（Sandbox 可能返回 JSON 格式的错误信息）
        log.warn("HTTP 请求返回非成功状态码: {} {}, 响应内容: {}", 
            response.code(), response.message(), responseText);
        
        // 如果是 404，可能是端口错误（连接到了应用而不是 Sandbox）
        if (response.code() == 404) {
            // 检查是否是 Sandbox 返回的 JSON 错误
            if (responseText != null && (responseText.contains("\"success\"") || responseText.contains("\"error\""))) {
                // 是 Sandbox 返回的 JSON，直接返回让上层处理
                return responseText;
            } else {
                // 不是 Sandbox 响应，可能是端口错误
                throw new IOException("请求失败: 404 Not Found。可能原因：" +
                    "\n1. 端口错误（连接到了应用端口而不是 Sandbox 端口）" +
                    "\n2. Sandbox 未正确注入或模块未激活" +
                    "\n3. URL 路径错误: " + url);
            }
        }
        
        // 其他错误状态码，返回响应内容让上层处理
        return responseText;
    }
    
    /**
     * 日志拦截器
     */
    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long startTime = System.currentTimeMillis();
            
            Response response = chain.proceed(request);
            
            long duration = System.currentTimeMillis() - startTime;
            log.debug("{} {} -> {} ({}ms)", request.method(), request.url(), response.code(), duration);
            
            return response;
        }
    }
}
