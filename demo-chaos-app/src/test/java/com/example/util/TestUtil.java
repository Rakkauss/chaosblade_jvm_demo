package com.example.util;

/**
 * 测试工具类
 * 
 * @author rakkaus
 */
public class TestUtil {
    
    /**
     * 重复字符
     * 
     * @param ch 字符
     * @param count 重复次数
     * @return 重复后的字符串
     */
    public static String repeatChar(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
