package org.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShortMessageLoginParam extends UserLoginParam{
    //email
    @NotBlank(message = "邮箱不能为空！")
    private String loginEmail;
    //验证码
    @NotBlank(message = "验证码不能为空！")
    private String verificationCode;
}
