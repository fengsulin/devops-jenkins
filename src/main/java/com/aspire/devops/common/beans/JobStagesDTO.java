package com.aspire.devops.common.beans;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: FSL
 * @date: 2023/2/21
 * @description: job构建stage阶段信息
 */
@Data
@Accessors(chain = true)
public class JobStagesDTO implements Serializable {
    private static final long serialVersionUID = 6L;

    private Long id;
    /**构建id，这里是代码生成的唯一id*/
    private Long buildId;
    /**stage名称*/
    private String name;
    /**耗时*/
    private String duration;
    /**阶段开始时间*/
    private LocalDateTime startTime;
    /**阶段状态*/
    private String status;
}
