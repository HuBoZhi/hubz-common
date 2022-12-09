package com.hubz.common.util;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.List;
import java.util.Map;

/**
 * @author hubz
 * @date 2021/8/30 21:10
 **/
public final class JsonUtil {

    /**
     * Map类型
     */
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private static final TypeReference<List<Map<String, Object>>> LIST_MAP_TYPE = new TypeReference<>() {
    };

    private JsonUtil() {
    }

    /**
     * 获取JSON字符串中指定键的值
     * @param jsonString JSON字符串
     * @param key 指定的键
     * @return java.lang.String 返回指定键的值
     *
     * @author hubz
     * @date 2021/8/30 21:14
     */
    public static String getJsonValue(String jsonString, String key) {
        JSONObject jsonObject = JSONUtil.parseObj(jsonString);
        return jsonObject.getStr(key);
    }

    /**
     * 将任意对象转换为JSON字
     * @param object 任意对象
     * @return java.lang.String JSON字符串
     *
     * @author hubz
     * @date 2021/8/31 22:25
     */
    public static <T> String toString(T object) {
        return JSONUtil.toJsonStr(object);
    }

    /**
     * 将Json字符串转换成任意对象
     * @param jsonStr Json字符串
     * @param clazz 任意对象
     * @return T
     *
     * @author hubz
     * @date 2021/12/13 22:37
     */
    public static <T> T strToObject(String jsonStr, Class<T> clazz) {
        return JSONUtil.toBean(jsonStr, clazz);
    }

    /**
     * 将字符串转换为Map<String, Object>
     * @param toJsonStr 字符串
     * @return java.util.Map<java.lang.String, java.lang.Object>
     *
     * @author hubz
     * @date 2021/12/21 22:21
     */
    public static Map<String, Object> strToMap(String toJsonStr) {
        return JSONUtil.toBean(toJsonStr, MAP_TYPE, false);
    }

    /**
     *  List<Map<String,Object>>
     * @param object object 对象
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     *
     * @author hubz
     * @date 2021/12/21 22:28
     */
    public static List<Map<String, Object>> objToListMap(Object object) {
        return JSONUtil.toBean(toString(object), LIST_MAP_TYPE, false);
    }
}
