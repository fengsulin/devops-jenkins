package com.aspire.devops.common.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aspire.devops.common.beans.BuildDetailDTO;
import com.aspire.devops.common.service.IBuildState;
import com.aspire.devops.common.vo.enums.BuildPhaseEnum;
import org.springframework.stereotype.Component;

@Component
public class QueuedPhase implements IBuildState {
    @Override
    public String getType() {
        return BuildPhaseEnum.QUEUED.value();
    }

    @Override
    public void handle(JSONObject build, BuildDetailDTO detail) {
        // TODO 存储或更新本地库
        // TODO 通知前台构建状态变化
        System.out.println(detail);
    }

}
