package org.example.lotterysystem.controller.param;

import lombok.Data;
import org.example.lotterysystem.service.enums.UserIdentityEnum;

import java.io.Serializable;

@Data
public class UserLoginParam implements Serializable {

    /**
     * 强制某身份登录。
     * @see UserIdentityEnum#name()
     */
    private String mandatoryIdentity;
}
