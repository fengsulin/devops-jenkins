package com.aspire.devops.common.vo;

import com.aspire.devops.common.vo.enums.ResultEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ResultVo<T> implements Serializable {
    private static final long serialVersionUID = 2384975113L;

    /**
     * 响应码
     */
    private Integer code;
    /**
     * 响应提示信息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;

    /**
     * 无参构造方法
     */
    public ResultVo(){}

    public ResultVo(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public ResultVo(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static <T> ResultVo<T> success(T data){
        return new ResultVo<>(ResultEnum.SUCCESS.getCode(),ResultEnum.SUCCESS.getMessage(),data);
    }
    public static <T> ResultVo<T> success(ResultEnum resultEnum,String message){
        return new ResultVo<>(ResultEnum.SUCCESS.getCode(),message);
    }
    public static <T> ResultVo<T> success(){
        return new ResultVo<>(ResultEnum.SUCCESS.getCode(),ResultEnum.SUCCESS.getMessage());
    }

    public static <T> ResultVo<T> error(){
        return new ResultVo<>(ResultEnum.SYSTEM_ERROR.getCode(),ResultEnum.SYSTEM_ERROR.getMessage());
    }

    public static <T> ResultVo<T> error(String message){
        return new ResultVo<>(ResultEnum.SYSTEM_ERROR.getCode(),message);
    }

    public static <T> ResultVo<T> error(ResultEnum resultEnum){
        return new ResultVo<>(resultEnum.getCode(),resultEnum.getMessage());
    }

}

