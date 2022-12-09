package com.hubz.common.response.v2;

import lombok.Data;

/**
 * @author hubz
 * @date 2021/3/13 19:59
 **/
@Data
public class RestResponse {

    /**
     * 返回状态吗
     */
    private String code;

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

    public RestResponse(ErrorEnum errorEnum) {
        this.code = errorEnum.getErrorCode();
        this.msg = errorEnum.getDesc();
    }

    public RestResponse(ErrorEnum errorEnum, Object data) {
        this.code = errorEnum.getErrorCode();
        this.msg = errorEnum.getDesc();
        this.data = data;
    }

    public RestResponse(ErrorEnum errorEnum, Object data, Integer total) {
        this.code = errorEnum.getErrorCode();
        this.msg = errorEnum.getDesc();
        this.data = data;
        this.total = total;
    }

    public static RestResponse ok(String msg) {
        return new RestResponse(ErrorEnum.SUCCESS, msg, null);
    }

    public static RestResponse ok(Object data) {
        return new RestResponse(ErrorEnum.SUCCESS, data);
    }

    public static RestResponse ok(Object data, Integer total) {
        return new RestResponse(ErrorEnum.SUCCESS, data, total);
    }

    public static RestResponse ok(Object data, Long total) {
        return new RestResponse(ErrorEnum.SUCCESS, data, Math.toIntExact(total));
    }

    public static RestResponse ok() {
        return new RestResponse(ErrorEnum.SUCCESS);
    }

    public static RestResponse error() {
        return new RestResponse(ErrorEnum.UNKNOWN_ERROR_CODE);
    }

    public static RestResponse error(String msg) {
        return new RestResponse(ErrorEnum.UNKNOWN_ERROR_CODE, msg);
    }

    public static RestResponse error(ErrorEnum errorEnum, String msg) {
        return new RestResponse(errorEnum, msg);
    }

}
