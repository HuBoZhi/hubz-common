package com.hubz.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Sunday算法：用于快速字符串匹配，性能优于KMP算法和BM算法
 * @author hubz
 * @date 2021/9/14 21:45
 **/
public final class SundayAlgorithm {

    private SundayAlgorithm() {
    }

    /**
     * 查询主串中是否包含子串
     * @param string 主串
     * @param pattern 子串
     * @return java.lang.Boolean true：包含
     *
     * @author hubz
     * @date 2021/9/14 22:35
     */
    public static Boolean sundayHandler(String string, String pattern) {
        // 获取主串和模式串的长度
        int strLength = string.length();
        int patternLength = pattern.length();
        // 如果主串长度为0 或者子串的长度大于主串，则无匹配项
        if (patternLength == 0 || patternLength > strLength) {
            return false;
        }
        // 将两个字符串转换为数组形式
        char[] stringArray = string.toCharArray();
        char[] patternArray = pattern.toCharArray();

        // 偏移表
        Map<Character, Integer> offsetTable = new HashMap<>(patternArray.length);
        for (int i = 0; i < patternLength; i++) {
            offsetTable.put(patternArray[i], patternLength - i);
        }

        // 匹配成功的位置
        int idx = 0;
        // 如果当前位置+子串长度<主串长度，则继续查找
        while (idx + patternLength <= strLength) {
            int k = 0;
            // 如果k小于子串长度，并且主串当前位置的字符和子串当前的字符一致时：k++
            while (k < patternLength && patternArray[k] == stringArray[idx + k]) {
                k++;
            }
            // 如果K的值和子串的长度一致时说明当前匹配的字符串和子串一致
            if (k == patternLength) {
                return true;
            } else {
                if (idx + patternLength < strLength) {
                    // 如果偏移表中不存在该字符，则移动距离为idx+(子串的长度+1)
                    idx += offsetTable.get(stringArray[idx + patternLength]) == null ? patternLength + 1 : offsetTable.get(stringArray[idx + patternLength]);
                } else {
                    // 大于主串的长度，查找失败
                    return false;
                }
            }
        }
        return false;
    }
}
