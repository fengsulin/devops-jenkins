package com.aspire.devops.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.aspire.devops.common.beans.BuildDetailDTO;
import com.aspire.devops.common.service.IBuildState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工厂类
 */
@Component
@Slf4j
public class TaskStrategyFactory implements ApplicationContextAware {
    private Map<String, IBuildState> stateMap = new ConcurrentHashMap<>(3);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IBuildState> beansOfType = applicationContext.getBeansOfType(IBuildState.class);
        beansOfType.values().forEach(iBuildState -> {
            stateMap.put(iBuildState.getType(),iBuildState);
        });
    }

    /***
     * 工厂方法
     * @param type 子策略类型名
     * @param build 构建JSONObject对象
     * @param detail 构建详情实体
     */
    public void handle(String type, JSONObject build, BuildDetailDTO detail){
        IBuildState buildState = stateMap.get(type);
        if (buildState != null){
            buildState.handle(build,detail);
            log.info("["+type+"]策略执行完毕");
        }else {
            log.info("["+type+"+]策略bean不存在");
        }
    }
}
