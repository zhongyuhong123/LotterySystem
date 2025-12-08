package org.example.lotterysystem.controller.result;

import lombok.Data;

@Data
public class BaseUserInfoResult {
    //人员id
    private Long userId;
    //姓名
    private String userName;
    //身份信息
    private String identity;
}
