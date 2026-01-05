package com.example.helper.chaos.module;

import com.example.helper.chaos.annotation.Command;
import com.example.helper.chaos.annotation.Module;

/**
 * ChaosBlade 模块接口
 * 
 * 定义与 Sandbox ChaosBlade 模块交互的方法
 * 
 * @author rakkaus
 * @since 1.0.0
 */
@Module("chaosblade")
public interface ChaosBlade {
    
    /**
     * 创建混沌实验
     * 
     * @param request 请求对象
     * @return 实验 ID
     */
    @Command("create")
    String create(Object request);
    
    /**
     * 销毁混沌实验
     * 
     * @param experimentId 实验 ID
     */
    @Command("destroy")
    void destroy(String experimentId);
    
    /**
     * 查询模块状态
     * 
     * @return 状态信息
     */
    @Command("status")
    String status();
    
    /**
     * 列出所有实验
     * 
     * @return 实验列表
     */
    @Command("list")
    String list();
}

