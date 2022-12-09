package com.hubz.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 计算Hash值工具
 * @author hubz
 * @date 2021/9/18 18:40
 **/
public final class HashUtil {

    private HashUtil() {

    }

    /**
     * 计算多参数的hash值
     * @param params 参数
     * @return java.lang.String hash值
     *
     * @author hubz
     * @date 2021/9/18 18:52
     */
    public static String calculateUniqueStr(String... params) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String param : params) {
            stringBuilder.append(getHashCode(param));
        }
        return getHashCode(stringBuilder.toString());
    }

    /**
     * 计算单参数的hash值
     * @param param 参数
     * @return java.lang.String hash值
     *
     * @author hubz
     * @date 2021/9/18 18:52
     */
    public static String getHashCode(String param) {
        try {
            param = param.trim();
            // 为MD5创建MessageDigest实例
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return ccByMessageDigest(md, param);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("hash值计算失败", e);
        }
    }

    /**
     * 计算hash
     * @param md 信息
     * @param param 参数
     * @return java.lang.String 计算结果
     *
     * @author hubz
     * @date 2021/10/30 18:08
     */
    private static String ccByMessageDigest(MessageDigest md, String param) {
        md.update(param.getBytes());
        byte[] bytes = md.digest();
        //将其转换为十六进制格式
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        //得到完整的哈希密码在十六进制格式
        return sb.toString();
    }

}
