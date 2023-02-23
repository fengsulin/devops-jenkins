package com.aspire.devops.common.service.impl;


import com.aspire.devops.common.service.IAuth;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

/**
 * Basic 认证
 */
public class BasicAuth implements IAuth {
    private String username;
    private String password;

    public BasicAuth(String username,String password){
        this.username = username;
        this.password = password;
    }
    @Override
    public String getAuth() {
        String auth = String.format("%s:%s",this.username,this.password);
        byte[] bytes = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        return "Basic " + new String(bytes);
    }
}
