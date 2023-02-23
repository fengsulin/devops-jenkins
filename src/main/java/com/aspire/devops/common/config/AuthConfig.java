package com.aspire.devops.common.config;

import com.aspire.devops.common.service.IAuth;
import com.aspire.devops.common.service.impl.BasicAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {
    private final JenkinsProperty jenkinsProperty;

    public AuthConfig(JenkinsProperty jenkinsProperty) {
        this.jenkinsProperty = jenkinsProperty;
    }

    @Bean
    public IAuth auth(){
        return new BasicAuth(jenkinsProperty.getUsername(),jenkinsProperty.getToken());
    }
}
