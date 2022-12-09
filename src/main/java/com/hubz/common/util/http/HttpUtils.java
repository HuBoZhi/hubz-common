package com.hubz.common.util.http;


import com.hubz.common.util.JsonUtil;
import org.apache.commons.collections4.MapUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author hubz
 * @date 2022/1/20 21:44
 **/
public final class HttpUtils {


    private HttpUtils() {
    }

    /**
     * 请求超时时间
     */
    private static final Long REQUEST_TIME_OUT = 30000L;
    private static final Duration TIME_OUT = Duration.ofMillis(REQUEST_TIME_OUT);

    private static final HttpClient.Builder HTTP_CLIENT_BUILDER = HttpClient.newBuilder().connectTimeout(TIME_OUT);
    private static final HttpClient CLIENT = HTTP_CLIENT_BUILDER.build();

    public static HttpResponse.BodyHandler<String> stringBodyHandler = HttpResponse.BodyHandlers.ofString();
    public static HttpResponse.BodyHandler<byte[]> byteArrayBodyHandler = HttpResponse.BodyHandlers.ofByteArray();
    public static HttpResponse.BodyHandler<InputStream> inputStreamBodyHandler = HttpResponse.BodyHandlers.ofInputStream();

    public static final List<Integer> REDIRECT_CODE_LIST = Arrays.asList(301, 302, 303, 307, 308);
    /**
     * 最大重定向深度
     */
    public static final Integer MAX_REDIRECT_TIMES = 3;
    /**
     * 初始化重定向深度
     */
    public static final Integer INIT_REDIRECT_TIMES = 0;
    /**
     * 永久重定向响应码
     */
    public static final Integer PERMANENT_REDIRECT_CODE = 301;

    /**
     * GET 请求
     * @param url 请求链接
     * @param headers 请求头
     * @param responseBodyHandler 返回值类型
     * @return java.net.http.HttpResponse<java.lang.String> 请求结果
     *
     * @author hubz
     * @date 2022/1/20 21:54
     */
    public static <T> HttpResponse<T> get(String url, Map<String, String> headers, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = createGetRequestBuilder(url, headers);
        HttpRequest request = requestBuilder.build();
        // 发送同步请求
        return CLIENT.send(request, responseBodyHandler);
    }

    /**
     * GET 请求
     * @param url 请求链接
     * @param responseBodyHandler 返回值类型
     * @return java.net.http.HttpResponse<java.lang.String> 请求结果
     *
     * @author hubz
     * @date 2022/1/20 21:54
     */
    public static <T> HttpResponse<T> get(String url, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        return get(url, null, responseBodyHandler);
    }

    /**
     * 使用代理的GET请求
     * @param url 请求链接
     * @param headers 请求头
     * @param proxyIp 代理IP
     * @param proxyPort 代理端口
     * @param responseBodyHandler 返回值类型
     * @return java.net.http.HttpResponse<java.lang.String> 请求结果
     *
     * @author hubz
     * @date 2022/1/20 21:54
     */
    public static <T> HttpResponse<T> getWithProxy
    (String url, Map<String, String> headers, HttpResponse.BodyHandler<T> responseBodyHandler, String proxyIp, Integer proxyPort)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = createGetRequestBuilder(url, headers);
        HttpRequest request = requestBuilder.build();
        return clientWithProxy(request, responseBodyHandler, proxyIp, proxyPort);
    }

    /**
     * 使用代理的GET请求
     * @param url 请求链接
     * @param proxyIp 代理IP
     * @param proxyPort 代理端口
     * @param responseBodyHandler 返回值类型
     * @return java.net.http.HttpResponse<java.lang.String> 请求结果
     *
     * @author hubz
     * @date 2022/1/20 21:54
     */
    public static <T> HttpResponse<T> getWithProxy
    (String url, HttpResponse.BodyHandler<T> responseBodyHandler, String proxyIp, Integer proxyPort)
            throws IOException, InterruptedException {
        return getWithProxy(url, null, responseBodyHandler, proxyIp, proxyPort);
    }


    /**
     * 创建GET请求的Builder
     * @param url 请求链接
     * @param headers 请求头
     * @return java.net.http.HttpRequest.Builder GET请求的Builder
     *
     * @author hubz
     * @date 2022/1/20 22:23
     */
    private static HttpRequest.Builder createGetRequestBuilder(String url, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36")
                .timeout(TIME_OUT)
                .GET();
        if (MapUtils.isNotEmpty(headers)) {
            headers.forEach(requestBuilder::setHeader);
        }
        return requestBuilder;
    }

    /**
     * POST请求
     * @param url 请求链接
     * @param headers 请求头
     * @param body 请求参数
     * @param responseBodyHandler 返回值类型
     * @return java.net.http.HttpResponse<java.lang.String> 请求结果
     *
     * @author hubz
     * @date 2022/1/20 22:04
     */
    public static <T> HttpResponse<T> post
    (String url, Map<String, String> headers, Map<String, Object> body, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = createPostRequestBuilder(url, headers, body);
        HttpRequest request = requestBuilder.build();
        return CLIENT.send(request, responseBodyHandler);
    }

    /**
     * POST请求
     * @param url 请求链接
     * @param body 请求参数
     * @param responseBodyHandler 返回值类型
     * @return java.net.http.HttpResponse<java.lang.String> 请求结果
     *
     * @author hubz
     * @date 2022/1/20 22:04
     */
    public static <T> HttpResponse<T> post(String url, Map<String, Object> body, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        return post(url, null, body, responseBodyHandler);
    }

    /**
     * 使用代理的POST请求
     * @param url 请求链接
     * @param headers 请求头
     * @param body 请求参数
     * @param responseBodyHandler 返回值类型
     * @return java.net.http.HttpResponse<java.lang.String> 请求结果
     *
     * @author hubz
     * @date 2022/1/20 22:04
     */
    public static <T> HttpResponse<T> postWithProxy
    (String url, Map<String, String> headers, Map<String, Object> body, HttpResponse.BodyHandler<T> responseBodyHandler,
     String proxyIp, Integer proxyPort) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = createPostRequestBuilder(url, headers, body);
        HttpRequest request = requestBuilder.build();
        return clientWithProxy(request, responseBodyHandler, proxyIp, proxyPort);
    }

    /**
     * 使用代理的POST请求
     * @param url 请求链接
     * @param body 请求参数
     * @param responseBodyHandler 返回值类型
     * @return java.net.http.HttpResponse<java.lang.String> 请求结果
     *
     * @author hubz
     * @date 2022/1/20 22:04
     */
    public static <T> HttpResponse<T> postWithProxy
    (String url, Map<String, Object> body, HttpResponse.BodyHandler<T> responseBodyHandler, String proxyIp, Integer proxyPort)
            throws IOException, InterruptedException {
        return postWithProxy(url, null, body, responseBodyHandler, proxyIp, proxyPort);
    }

    /**
     * 创建Post请求的Builder
     * @param url 请求链接
     * @param headers 请求头
     * @param body 请求参数
     * @return java.net.http.HttpRequest.Builder POST请求的Builder
     *
     * @author hubz
     * @date 2022/1/20 22:23
     */
    private static HttpRequest.Builder createPostRequestBuilder
    (String url, Map<String, String> headers, Map<String, Object> body) {
        // 转换requestBody
        String requestBody = JsonUtil.toString(body);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIME_OUT)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        if (MapUtils.isNotEmpty(headers)) {
            headers.forEach(requestBuilder::setHeader);
        }
        return requestBuilder;
    }

    /**
     * HttpClient发送代理请求
     * @param request HttpRequest对象
     * @param proxyIp 代理IP
     * @param proxyPort 代理端口
     * @return java.net.http.HttpResponse<java.lang.String>
     *
     * @author hubz
     * @date 2022/1/20 22:30
     */
    public static <T> HttpResponse<T> clientWithProxy
    (HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, String proxyIp, Integer proxyPort)
            throws IOException, InterruptedException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(proxyIp, proxyPort);
        ProxySelector proxySelector = ProxySelector.of(inetSocketAddress);
        HTTP_CLIENT_BUILDER.proxy(proxySelector);
        HttpClient httpClient = HTTP_CLIENT_BUILDER.build();
        // 发送同步请求
        return httpClient.send(request, responseBodyHandler);
    }

}
