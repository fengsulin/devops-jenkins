package com.aspire.devops.common.beans;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: FSL
 * @date: 2023/2/22
 * @description: Jenkins 凭证实体
 */
@Data
@Accessors(chain = true)
public class CredentialDTO implements Serializable {
    private static final long serialVersionUID = 8L;

    private String displayName;
    private String description;
    private String fingerprint;
    private String id;
    private String typeName;
}
