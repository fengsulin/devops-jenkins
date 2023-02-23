package com.aspire.devops.common.vo;

import lombok.Data;
import org.apache.http.Header;

import java.io.Serializable;

@Data
public class HttpResponseVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**响应码*/
    private Integer code;
    /**响应体*/
    private String content;
    private Header[] Headers;
}
