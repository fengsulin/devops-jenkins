package com.aspire.devops.common.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aspire.devops.common.beans.BuildDetailDTO;
import com.aspire.devops.common.service.IBuildState;
import com.aspire.devops.common.utils.JenkinsUtils;
import com.aspire.devops.common.vo.ServiceException;
import com.aspire.devops.common.vo.enums.BuildPhaseEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class FinalizedPhase implements IBuildState {
    @Resource
    private JenkinsUtils jenkinsUtils;

    @Override
    public String getType() {
        return BuildPhaseEnum.FINALIZED.value();
    }

    @Override
    public void handle(JSONObject build, BuildDetailDTO detail) {
        String timestamp = build.getString("timestamp");
        String status = build.getString("status");
        String log = build.getString("log");
        String duration = build.getString("duration");
        Instant instant = Instant.ofEpochMilli(Long.valueOf(timestamp));
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime buildTime = LocalDateTime.ofInstant(instant, zoneId);
        detail.setDuration(Double.valueOf(duration)/1000).
                setBuildTime(buildTime).setResult(status).setLogs(log);
        // TODO 存储或更新本地库
        // TODO 通知前台构建状态变化
//        System.out.println("["+detail.getJobId()+"]构建信息为："+detail);
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            throw new ServiceException(e.getMessage());
        }
        jenkinsUtils.deleteJob(null,detail.getJobId().toString());
    }
}
