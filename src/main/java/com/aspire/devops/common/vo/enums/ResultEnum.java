package com.aspire.devops.common.vo.enums;

public enum ResultEnum {
    SUCCESS(10001,"请求成功"),
    SYSTEM_ERROR(99999,"系统异常，请联系管理员"),
    VALID_ERROR(10002,"参数校验异常"),
    CUSTOM_ERROR(10003,""),
    JSON_PARSE_ERROR(10004,"JSON转换异常"),
    XML_PARSE_ERROR(10005,"XML转换异常"),
    REMOTE_FALLBACK(10007,"服务调用异常"),
    CONCURRENT_BUILD_ERROR(10006,"不允许并发构建");

    private Integer code;
    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
