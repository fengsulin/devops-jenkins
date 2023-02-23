package com.aspire.devops.common.beans;

import lombok.Data;

@Data
public class JenkinsParamDTO {
    private String name;
    private String desc;
    private String value;
    private String type;
}
