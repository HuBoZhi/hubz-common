package com.hubz.common.response.v1;


import lombok.Data;

/**
 * @author hubz
 * @date 2021/3/13 19:59
 **/
@Data
public class RestResponse {
    /**
     * 成功返回码
     */
    public static final Integer CODE_SUCCESS = 0;

    /**
     * 失败返回码
     */
    public static final Integer CODE_ERROR = 500;
    public static final Integer CODE_400 = 400;

    /**
     * 成功返回消息
     */
    private static final String MSG_SUCCESS = "操作成功";

    /**
     * 失败返回消息
     */
    private static final String MSG_ERROR = "操作失败";

    /**
     * 返回状态吗
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String msg;

    /**
     * 返回数据
     */
    private Object data;

    /**
     * 数据总量
     */
    private Integer total;

    public RestResponse() {

    }

    public RestResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public RestResponse(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public RestResponse(Integer code, String msg, Object data, Integer total) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.total = total;
    }

    public static RestResponse ok(String msg) {
        return new RestResponse(CODE_SUCCESS, msg, null);
    }

    public static RestResponse ok(String msg, Object data) {
        return new RestResponse(CODE_SUCCESS, msg, data);
    }

    public static RestResponse ok(Object data) {
        return new RestResponse(CODE_SUCCESS, MSG_SUCCESS, data);
    }

    public static RestResponse ok(Object data, Integer total) {
        return new RestResponse(CODE_SUCCESS, MSG_SUCCESS, data, total);
    }

    public static RestResponse ok(Object data, Long total) {
        return new RestResponse(CODE_SUCCESS, MSG_SUCCESS, data, Math.toIntExact(total));
    }

    public static RestResponse ok() {
        return new RestResponse(CODE_SUCCESS, MSG_SUCCESS);
    }

    public static RestResponse error() {
        return new RestResponse(CODE_ERROR, MSG_ERROR);
    }

    public static RestResponse error(String msg) {
        return new RestResponse(CODE_ERROR, msg);
    }

    public static RestResponse error(Integer errorCode, String msg) {
        return new RestResponse(errorCode, msg);
    }
}
