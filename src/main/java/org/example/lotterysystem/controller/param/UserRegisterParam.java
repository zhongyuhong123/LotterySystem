package org.example.lotterysystem.controller.param;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterParam implements Serializable {

    @NotBlank(message = "姓名不能为空哦！")
    private String name;

    @NotBlank(message = "邮箱不能为空哦！")
    private String mail;

    //密码
    private String password;

    @NotBlank(message = "电话不能为空！")
    private String phoneNumber;

    @NotBlank(message = "身份不能为空！")
    private String identity;
}
