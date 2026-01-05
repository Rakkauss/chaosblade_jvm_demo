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

import com.alibaba.jvm.sandbox.api.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FilterUtil - Filter 工具类
 * 
 * [改造说明]
 * 简化的 Filter 创建工具，用于将 PointCut 转换为 Sandbox 的 Filter
 * 
 * @author rakkaus
 * @since 1.8.0
 */
public class FilterUtil {
    
    private static final Logger log = LoggerFactory.getLogger(FilterUtil.class);
    
    /**
     * 根据 PointCut 创建 Filter
     * 
     * @param pointCut PointCut 实例
     * @return Filter 实例
     */
    public static Filter createFilter(final PointCut pointCut) {
        if (pointCut == null) {
            return new Filter() {
                @Override
                public boolean doClassFilter(int access, String javaClassName, 
                        String superClassTypeJavaClassName,
                        String[] interfaceTypeJavaClassNameArray,
                        String[] annotationTypeJavaClassNameArray) {
                    return false; // 如果没有 PointCut，不匹配任何类
                }
                
                @Override
                public boolean doMethodFilter(int access, String javaMethodName,
                        String[] parameterTypeJavaClassNameArray,
                        String[] throwsTypeJavaClassNameArray,
                        String[] annotationTypeJavaClassNameArray) {
                    return false;
                }
            };
        }
        
        return new Filter() {
            @Override
            public boolean doClassFilter(int access, String javaClassName,
                    String superClassTypeJavaClassName,
                    String[] interfaceTypeJavaClassNameArray,
                    String[] annotationTypeJavaClassNameArray) {
                // 使用 PointCut 的 matchClass 方法
                boolean matched = pointCut.matchClass(javaClassName);
                // 只在 DEBUG 级别记录匹配的类，避免日志过多
                if (matched) {
                    log.debug("[Filter] 类匹配成功: {} (期望: {})", javaClassName, pointCut.getClassName());
                }
                return matched;
            }
            
            @Override
            public boolean doMethodFilter(int access, String javaMethodName,
                    String[] parameterTypeJavaClassNameArray,
                    String[] throwsTypeJavaClassNameArray,
                    String[] annotationTypeJavaClassNameArray) {
                // 使用 PointCut 的 matchMethod 方法
                boolean matched = pointCut.matchMethod(javaMethodName);
                // 只在 DEBUG 级别记录匹配的方法，避免日志过多
                if (matched) {
                    log.debug("[Filter] 方法匹配成功: {} (期望: {})", javaMethodName, pointCut.getMethodName());
                }
                return matched;
            }
        };
    }
}

