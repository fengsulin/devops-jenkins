package com.aspire.devops.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jenkins")
@Data
public class JenkinsProperty {
    /**用户名*/
    private String username;
    /**用户token*/
    private String token;
    /**jenkins访问地址*/
    private String url;
}
