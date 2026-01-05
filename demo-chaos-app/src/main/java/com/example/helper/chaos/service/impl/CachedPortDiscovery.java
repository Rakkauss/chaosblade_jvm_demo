package com.example.helper.chaos.service.impl;

import com.example.helper.chaos.service.PortDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 带缓存的端口发现策略（装饰器模式）
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public class CachedPortDiscovery implements PortDiscovery {
    
    private static final Logger log = LoggerFactory.getLogger(CachedPortDiscovery.class);
    
    private final PortDiscovery delegate;
    private final Map<String, Integer> cache = new ConcurrentHashMap<>();
    
    public CachedPortDiscovery(PortDiscovery delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public int findPort(int pid, String host) {
        String key = pid + "@" + host;
        
        // 检查缓存
        Integer cachedPort = cache.get(key);
        if (cachedPort != null && cachedPort > 0) {
            log.debug("从缓存获取端口: {} -> {}", key, cachedPort);
            return cachedPort;
        }
        
        // 查找端口
        int port = delegate.findPort(pid, host);
        
        // 缓存结果
        if (port > 0) {
            cache.put(key, port);
            log.debug("缓存端口: {} -> {}", key, port);
        }
        
        return port;
    }
    
    @Override
    public String getStrategyName() {
        return "cached-" + delegate.getStrategyName();
    }
    
    @Override
    public int getPriority() {
        return delegate.getPriority();
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        cache.clear();
        log.debug("清除端口缓存");
    }
}

