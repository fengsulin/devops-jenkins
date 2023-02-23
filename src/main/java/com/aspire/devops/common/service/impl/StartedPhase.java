package com.aspire.devops.common.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aspire.devops.common.beans.BuildDetailDTO;
import com.aspire.devops.common.service.IBuildState;
import com.aspire.devops.common.vo.enums.BuildPhaseEnum;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class StartedPhase implements IBuildState {
    @Override
    public String getType() {
        return BuildPhaseEnum.STARTED.value();
    }

    @Override
    public void handle(JSONObject build, BuildDetailDTO detail) {
        String timestamp = build.getString("timestamp");
        Instant instant = Instant.ofEpochMilli(Long.valueOf(timestamp));
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime buildTime = LocalDateTime.ofInstant(instant, zoneId);
        detail.setBuildTime(buildTime);
        // TODO 存储或更新本地库
        // TODO 通知前台构建状态变化
        System.out.println(detail);
    }
}
