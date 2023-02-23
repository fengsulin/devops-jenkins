package com.aspire.devops.common.beans;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: FSL
 * @date: 2023/2/22
 * @description: Jenkins构建等待队列
 */
@Data
@Accessors(chain = true)
public class QueueItemDTO implements Serializable {
    private static final long serialVersionUID = 7L;

    private String id;
    private boolean blocked;
    private boolean buildable;
    private LocalDateTime inQueueTime;
    private boolean stuck;
    private LocalDateTime startBuildTime;
    private boolean pending;
}
