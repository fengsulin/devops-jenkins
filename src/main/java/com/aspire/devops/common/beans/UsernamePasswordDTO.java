package com.aspire.devops.common.beans;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: FSL
 * @date: 2023/2/22
 * @description: TODO
 */
@Data
@Accessors(chain = true)
public class UsernamePasswordDTO implements Serializable {
    private static final long serialVersionUID = 8L;

    private String username;
    private String password;
    private String description;
    private String id;
}
