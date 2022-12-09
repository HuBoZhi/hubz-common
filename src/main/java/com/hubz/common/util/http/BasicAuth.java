package com.hubz.common.util.http;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.config.RequestConfig;

import java.nio.charset.StandardCharsets;

/**
 * @author hubz
 * @date 2022/8/14 22:23
 **/
public class BasicAuth implements Auth {

    private String username;

    private String password;

    public BasicAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getAuth() {
        String auth = String.format("%s:%s", this.username, this.password);

        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        return "Basic " + new String(encodedAuth);
    }

    /**
     * 设置 HTTP 请求超时时间
     *
     * @param connectTimeout tcp 连接超时时间
     * @param readTimeout    读取数据超时时间
     * @return RequestConfig
     */
    private RequestConfig getRequestConfig(int connectTimeout, int readTimeout) {
        return RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build();
    }
}