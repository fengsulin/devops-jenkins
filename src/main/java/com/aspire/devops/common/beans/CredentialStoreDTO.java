package com.aspire.devops.common.beans;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author: FSL
 * @date: 2023/2/22
 * @description: Jenkins凭证存储描述
 */
@Data
@Accessors(chain = true)
public class CredentialStoreDTO implements Serializable {
    private static final long serialVersionUID = 9L;

    private String description;
    private String displayName;
    private String fullName;
    private boolean global;
    List<CredentialDTO> credentialDTOS;
}
