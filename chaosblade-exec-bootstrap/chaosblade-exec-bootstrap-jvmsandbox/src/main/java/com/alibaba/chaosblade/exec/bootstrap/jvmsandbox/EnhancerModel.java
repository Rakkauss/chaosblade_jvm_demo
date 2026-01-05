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

import java.lang.reflect.Method;

/**
 * EnhancerModel - 增强器执行上下文
 * 
 * [改造说明]
 * 简化版本的执行上下文，包含方法执行所需的基本信息：
 * 1. 目标对象和方法
 * 2. 方法参数
 * 3. 返回值（用于Mock）
 * 4. ClassLoader（用于类型转换）
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class EnhancerModel {
    
    /** 目标对象实例 */
    private Object target;
    
    /** 目标方法 */
    private Method method;
    
    /** 方法参数 */
    private Object[] arguments;
    
    /** 返回值（用于Mock） */
    private Object returnValue;
    
    /** 是否设置了返回值（用于区分null值和未设置） */
    private boolean hasReturnValue = false;
    
    /** ClassLoader */
    private ClassLoader classLoader;
    
    /** 类名 */
    private String className;
    
    /** 方法名 */
    private String methodName;
    
    // Getters and Setters
    
    public Object getTarget() {
        return target;
    }
    
    public void setTarget(Object target) {
        this.target = target;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public void setMethod(Method method) {
        this.method = method;
        if (method != null) {
            this.methodName = method.getName();
        }
    }
    
    public Object[] getArguments() {
        return arguments;
    }
    
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }
    
    public Object getReturnValue() {
        return returnValue;
    }
    
    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
        this.hasReturnValue = true;  // 标记已设置返回值
    }
    
    public boolean hasReturnValue() {
        return hasReturnValue;
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    @Override
    public String toString() {
        return "EnhancerModel{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", argumentCount=" + (arguments != null ? arguments.length : 0) +
                '}';
    }
}



