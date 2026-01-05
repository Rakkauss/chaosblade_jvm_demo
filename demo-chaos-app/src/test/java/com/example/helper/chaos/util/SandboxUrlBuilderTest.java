package com.example.helper.chaos.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SandboxUrlBuilder 单元测试
 * 
 * @author rakkaus
 * @since 2.0.0
 */
public class SandboxUrlBuilderTest {
    
    @Test
    public void testBuildCommandUrl() {
        String url = SandboxUrlBuilder.buildCommandUrl("127.0.0.1", 54973, "chaosblade", "create");
        assertEquals("http://127.0.0.1:54973/sandbox/default/module/http/chaosblade/create", url);
    }
    
    @Test
    public void testBuildStatusUrl() {
        String url = SandboxUrlBuilder.buildStatusUrl("127.0.0.1", 54973);
        assertTrue(url.contains("127.0.0.1"));
        assertTrue(url.contains("54973"));
        assertTrue(url.contains("/sandbox/default/module/http/chaosblade/status"));
    }
    
    @Test
    public void testBuildModuleActiveUrl() {
        String url = SandboxUrlBuilder.buildModuleActiveUrl("127.0.0.1", 54973, "chaosblade");
        assertEquals("http://127.0.0.1:54973/sandbox/default/module/http/sandbox-module-mgr/active?ids=chaosblade", url);
    }
    
    @Test
    public void testBuildCommandUrlWithDifferentPort() {
        String url = SandboxUrlBuilder.buildCommandUrl("localhost", 8080, "test-module", "destroy");
        assertEquals("http://localhost:8080/sandbox/default/module/http/test-module/destroy", url);
    }
}

