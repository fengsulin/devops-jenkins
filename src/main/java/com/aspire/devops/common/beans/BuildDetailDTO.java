package com.aspire.devops.common.beans;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author FSL
 * @since 2023-02-20
 */
@Accessors(chain = true)
@Data
public class BuildDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 所属job id
     */
    private Long jobId;
    /**
     * 构建number
     */
    private Integer buildId;
    /**
     * 构建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime buildTime;

    /**
     * 构建耗时
     */
    private Double duration;

    /**
     * 构建结果
     */
    private String result;

    /**
     * 构建阶段，QUEUED排队中,STARTED开始构建,FINALIZED构建结束
     */
    private String stage;

    /**
     * 执行人
     */
    private String buildUser;

    /**
     * 代码分支
     */
    private String causeBranch;

    /**
     * 代码地址
     */
    private String causeUrl;
    /**构建是否结束*/
    private Boolean endFlag;
    /**
     * 构建日志
     */
    private String logs;

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "BuildDetail{" +
            "id=" + id +
            ", jobId=" + jobId +
            ", buildTime=" + buildTime +
            ", duration=" + duration +
            ", result=" + result +
            ", stage=" + stage +
            ", buildUser=" + buildUser +
            ", causeBranch=" + causeBranch +
            ", causeUrl=" + causeUrl +
            ", logs=" + logs +
        "}";
    }
}
