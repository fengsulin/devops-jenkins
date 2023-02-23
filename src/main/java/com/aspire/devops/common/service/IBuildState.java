package com.aspire.devops.common.service;

import com.alibaba.fastjson.JSONObject;
import com.aspire.devops.common.beans.BuildDetailDTO;

public interface IBuildState {
    /**获取策略类对应的类型*/
    String getType();
    /**策略方法*/
    void handle(JSONObject build, BuildDetailDTO detail);
}
