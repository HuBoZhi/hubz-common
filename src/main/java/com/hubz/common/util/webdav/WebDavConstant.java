package com.hubz.common.util.webdav;

/**
 * @author hubz
 * @date 2022/7/30 14:26
 **/
public final class WebDavConstant {

    public static Integer SAME_VERSION = 0;

    public static Integer LOCAL_IS_LAST = 1;

    public static Integer WEBDAV_IS_LAST = -1;

    public static final String HTTP_METHOD_HEAD = "HEAD";
    public static final String HTTP_METHOD_MKCOL = "MKCOL";
    public static final String HTTP_METHOD_PROPFIND = "PROPFIND";

    /**
     * 请求成功状态码
     */
    public static final Integer REQUEST_OK = 200;

    public static final Integer WEBDAV_SUCCESS = 201;
    public static final Integer WEBDAV_SUCCESS_2 = 204;

    public static final Integer NOT_FOUND_STATUS_CODE = 404;
    public static final Integer FOUND_STATUS_CODE = 207;

    /**
     * 失败状态码前缀
     */
    public static final String CODE_40 = "40";

}