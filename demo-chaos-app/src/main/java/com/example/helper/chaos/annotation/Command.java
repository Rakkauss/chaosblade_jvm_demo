package com.example.helper.chaos.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sandbox 命令注解
 * 
 * 用于标识一个方法代表一个 Sandbox 命令
 * 
 * @author rakkaus
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    
    /**
     * 命令名称
     * 例如："create", "destroy", "status", "list"
     * 
     * @return 命令名称
     */
    String value();
}

