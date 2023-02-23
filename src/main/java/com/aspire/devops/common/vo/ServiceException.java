package com.aspire.devops.common.vo;

import com.aspire.devops.common.vo.enums.ResultEnum;

/**
 * 自定义业务异常
 */
public final class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1235L;
    private Integer code;
    private String message;
    private String detailMessage;

    public ServiceException() {
    }

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public ServiceException(ResultEnum resultEnum){
        this.message = resultEnum.getMessage();
        this.code = resultEnum.getCode();
    }

    public String getDetailMessage() {
        return this.detailMessage;
    }

    public String getMessage() {
        return this.message;
    }

    public Integer getCode() {
        return this.code;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    public ServiceException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ServiceException(Integer code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public ServiceException(ResultEnum resultEnum,Throwable cause){
        super(resultEnum.getMessage(),cause);
        this.code = resultEnum.getCode();
    }

    public ServiceException(Integer code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
}
