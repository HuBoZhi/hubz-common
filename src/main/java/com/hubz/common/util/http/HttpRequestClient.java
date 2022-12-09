package com.hubz.common.util.http;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author wxweven
 */
@Slf4j
public class HttpRequestClient {

    /**
     * 默认 连接/读取数据 超时时间都是 10s
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 15_000;
    private static final int DEFAULT_READ_TIMEOUT = 15_000;


    private final Auth auth;
    private final HttpClient httpClient;

    /**
     * 用户名密码认证，默认的超时时间(10s)
     * @param username 用户名
     * @param password 密码
     */
    public HttpRequestClient(String username, String password) {
        this(username, password, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 用户名密码认证，自定义超时时间
     * @param username 用户名
     * @param password 密码
     * @param connectTimeout 连接超时时间
     * @param readTimeout 读超时时间
     */
    public HttpRequestClient(String username, String password,
                             int connectTimeout, int readTimeout) {

        this.auth = new BasicAuth(username, password);
        this.httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(getRequestConfig(connectTimeout, readTimeout))
                .setMaxConnPerRoute(50)
                .setMaxConnTotal(200)
                .build();
    }

    /**
     * BearerToken 认证，默认的超时时间(10s)
     * @author hubz
     * @date 2022/8/14 22:20
     *
     * @param bearerToken token字符串
     **/
    public HttpRequestClient(String bearerToken) {
        this(bearerToken, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * BearerToken 认证，自定义超时时间
     * @author hubz
     * @date 2022/8/14 22:20
     *
     * @param bearerToken token字符串
     * @param connectTimeout 连接超时时间
     * @param readTimeout 读超时时间
     **/
    public HttpRequestClient(String bearerToken, int connectTimeout, int readTimeout) {
        this.auth = new BearerAuth(bearerToken);
        this.httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(getRequestConfig(connectTimeout, readTimeout))
                .build();
    }

    private RequestConfig getRequestConfig(int connectTimeout, int readTimeout) {
        return RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build();
    }


    public String doGetString(String url) {
        return doGetString(url, new HashMap<>());
    }


    public String doGetString(URI uri) {
        return doGetString(uri, new HashMap<>());
    }


    public String doGetString(String url, Map<String, String> params) {
        RequestBuilder reqBuilder = RequestBuilder.get(url);
        return doGetString(reqBuilder, params);
    }


    public String doGetString(URI uri, Map<String, String> params) {
        RequestBuilder reqBuilder = RequestBuilder.get(uri);
        return doGetString(reqBuilder, params);
    }

    public String doGetString(RequestBuilder reqBuilder, Map<String, String> params) {
        reqBuilder.addHeader(HttpHeaders.AUTHORIZATION, auth.getAuth());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            reqBuilder.addParameter(entry.getKey(), entry.getValue());
        }
        try {
            HttpResponse resp = httpClient.execute(reqBuilder.build());
            return EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("doGet 异常: reqBuilder={}, params={}", reqBuilder, params, e);
            return null;
        }
    }

    public HttpResponse doGetHttpResponse(String url) throws IOException {
        RequestBuilder reqBuilder = RequestBuilder.get(url);
        reqBuilder.addHeader(HttpHeaders.AUTHORIZATION, auth.getAuth());
        return httpClient.execute(reqBuilder.build());
    }


    public String doPost(String url, HttpEntity httpEntity) {
        try {
            HttpResponse resp = this.execute(HttpClientMethod.POST.toString(), url, httpEntity);
            return EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("doPost 异常: url={}", url, e);
            return null;
        }
    }


    public HttpResponse doPostResp(String url, HttpEntity httpEntity) throws IOException {
        return this.execute(HttpClientMethod.POST.toString(), url, httpEntity);
    }


    public HttpResponse doPut(String url, HttpEntity httpEntity) throws IOException {
        return this.execute(HttpClientMethod.PUT.toString(), url, httpEntity);
    }

    /**
     * 发送 PUT 请求，JSON 形式
     *
     * @param url 请求URL
     * @param httpEntity 请求内容
     * @return HttpResponse 返回结果
     */
    public HttpResponse doDelete(String url, HttpEntity httpEntity) throws IOException {
        return this.execute(HttpClientMethod.DELETE.toString(), url, httpEntity);
    }

    public HttpResponse execute(String method, String url, HttpEntity httpEntity) throws IOException {
        RequestBuilder reqBuilder = RequestBuilder.create(method)
                .setUri(url)
                .addHeader(HttpHeaders.AUTHORIZATION, auth.getAuth())
                .addHeader("Accept", ContentType.APPLICATION_JSON.toString())
                .addHeader("Content-type", ContentType.APPLICATION_JSON.toString());

        if (!Objects.isNull(httpEntity)) {
            reqBuilder.setEntity(httpEntity);
        }

        return httpClient.execute(reqBuilder.build());
    }

    public HttpResponse execute(String method, String url) throws IOException {
        RequestBuilder reqBuilder = RequestBuilder.create(method)
                .setUri(url)
                .addHeader(HttpHeaders.AUTHORIZATION, auth.getAuth());
        HttpUriRequest httpUriRequest = reqBuilder.build();
        return httpClient.execute(httpUriRequest);
    }
}
