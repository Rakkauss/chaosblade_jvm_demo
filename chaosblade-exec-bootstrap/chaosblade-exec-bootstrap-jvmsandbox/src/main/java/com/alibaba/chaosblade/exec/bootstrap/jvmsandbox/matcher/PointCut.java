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

package com.alibaba.chaosblade.exec.bootstrap.jvmsandbox.matcher;

/**
 * PointCut - 切点定义（匹配规则）
 * 
 * [改造说明]
 * 简化的切点定义，用于匹配需要增强的类和方法
 * 
 * TODO: Phase 3 后续可以扩展为更复杂的匹配规则
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class PointCut {
    
    /** 类名匹配规则 */
    private String className;
    
    /** 方法名匹配规则 */
    private String methodName;
    
    public PointCut(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
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
    
    /**
     * 匹配类名
     * 
     * @param targetClassName 目标类名
     * @return 是否匹配
     */
    public boolean matchClass(String targetClassName) {
        if (className == null || className.isEmpty()) {
            return true; // 空表示匹配所有
        }
        
        // 精确匹配
        if (className.equals(targetClassName)) {
            return true;
        }
        
        // 转换为内部格式（. -> /）进行匹配
        String internalClassName = className.replace('.', '/');
        if (internalClassName.equals(targetClassName)) {
            return true;
        }
        
        // 包含匹配（用于模糊匹配）
        if (targetClassName.contains(className) || targetClassName.contains(internalClassName)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 匹配方法名
     * 
     * @param targetMethodName 目标方法名
     * @return 是否匹配
     */
    public boolean matchMethod(String targetMethodName) {
        if (methodName == null || methodName.isEmpty()) {
            return true; // 空表示匹配所有
        }
        return methodName.equals(targetMethodName);
    }
    
    @Override
    public String toString() {
        return "PointCut{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}



