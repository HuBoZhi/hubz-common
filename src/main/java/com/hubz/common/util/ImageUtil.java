package com.hubz.common.util;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * @author hubozhi
 * @date 2021/10/1 22:41
 **/
@Slf4j
public final class ImageUtil {

    private ImageUtil() {
    }

    /**
     * 将图片InputStream转换成base64编码
     * @param inputStream InputStream对象
     * @return java.lang.String base64编码
     *
     * @author hubz
     * @date 2021/10/30 18:09
     */
    public static String convertImageToBase64Str(InputStream inputStream) {
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            int available;
            available = inputStream.available();
            byte[] bytes = new byte[available];
            int read = inputStream.read(bytes);
            return encoder.encodeToString(bytes);
        } catch (IOException e) {
            log.error("图片转换失败", e);
            return "";
        }
    }

    /**
     * 键图片文件转换成base64编码
     * @param path 图片文件的路径
     * @return java.lang.Stringbase64编码
     *
     * @author hubz
     * @date 2021/10/30 18:10
     */
    public static String convertImageToBase64Str(String path) {
        try (FileInputStream inputStream = new FileInputStream(path)) {
            return convertImageToBase64Str(inputStream);
        } catch (IOException e) {
            log.error("图片转换失败", e);
            return "";
        }
    }

    /**
     * 将图片写入临时文件
     * @author hubz
     * @date 2022/4/11 11:47
     *
     * @param inputStream 图片源
     * @param imageType 图片类型
     * @param prefix 临时文件前缀
     * @return java.lang.String
     **/
    public static String writeToTmpDir(InputStream inputStream, String prefix, String imageType) throws IOException {
        Path tempDir = Files.createTempFile(prefix, imageType);
        FileUtil.writeBytes(inputStream.readAllBytes(), tempDir.toFile());
        String tmpFilePath = tempDir.toAbsolutePath().toString();
        log.info("临时文件路径：{}", tempDir.toAbsolutePath());
        return tmpFilePath;
    }
}
