package com.example.helper.chaos.service.impl;

import com.example.helper.chaos.service.PortDiscovery;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * CachedPortDiscovery 单元测试
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public class CachedPortDiscoveryTest {
    
    private PortDiscovery mockDiscovery;
    private CachedPortDiscovery cachedDiscovery;
    
    @Before
    public void setUp() {
        mockDiscovery = Mockito.mock(PortDiscovery.class);
        cachedDiscovery = new CachedPortDiscovery(mockDiscovery);
    }
    
    @Test
    public void testCacheHit() {
        // 第一次调用，返回 54973
        when(mockDiscovery.findPort(12345, "127.0.0.1")).thenReturn(54973);
        
        // 第一次查找
        int port1 = cachedDiscovery.findPort(12345, "127.0.0.1");
        assertEquals(54973, port1);
        
        // 第二次查找（应该从缓存读取，不调用 delegate）
        int port2 = cachedDiscovery.findPort(12345, "127.0.0.1");
        assertEquals(54973, port2);
        
        // 验证 delegate 只被调用了一次
        verify(mockDiscovery, times(1)).findPort(12345, "127.0.0.1");
    }
    
    @Test
    public void testCacheMiss() {
        // Mock 返回 -1（未找到）
        when(mockDiscovery.findPort(12345, "127.0.0.1")).thenReturn(-1);
        
        int port = cachedDiscovery.findPort(12345, "127.0.0.1");
        assertEquals(-1, port);
        
        // 未找到的结果不应该被缓存，再次调用应该再次查找
        cachedDiscovery.findPort(12345, "127.0.0.1");
        verify(mockDiscovery, times(2)).findPort(12345, "127.0.0.1");
    }
    
    @Test
    public void testClearCache() {
        when(mockDiscovery.findPort(12345, "127.0.0.1")).thenReturn(54973);
        
        // 第一次查找
        cachedDiscovery.findPort(12345, "127.0.0.1");
        
        // 清除缓存
        cachedDiscovery.clearCache();
        
        // 再次查找，应该重新调用 delegate
        cachedDiscovery.findPort(12345, "127.0.0.1");
        verify(mockDiscovery, times(2)).findPort(12345, "127.0.0.1");
    }
}

