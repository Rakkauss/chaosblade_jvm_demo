package com.example.helper.chaos.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sandbox 模块注解
 * 
 * 用于标识一个接口代表一个 Sandbox 模块
 * 
 * @author rakkaus
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Module {
    
    /**
     * 模块名称
     * 例如："chaosblade"
     * 
     * @return 模块名称
     */
    String value();
}

