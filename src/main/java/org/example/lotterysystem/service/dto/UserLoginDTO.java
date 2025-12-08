package org.example.lotterysystem.service.dto;

import lombok.Data;
import org.example.lotterysystem.service.enums.UserIdentityEnum;

@Data
public class UserLoginDTO {
    //JWT令牌
    private String token;
    //登录人员身份
    private UserIdentityEnum identity;
}
