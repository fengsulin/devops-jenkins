package com.aspire.devops.common.vo.enums;

public enum ParamTypeEnum {
    STRING("string"),
    CHOICE_PARAM("choice_string");

    public String getType() {
        return type;
    }

    private final String type;

    ParamTypeEnum(String type) {
        this.type = type;
    }
}
