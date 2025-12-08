package org.example.lotterysystem.controller.param;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CreateActivityParam implements Serializable {
    //活动名
    @NotBlank(message = "活动名不能为空！")
    private String activityName;
    //描述
    @NotBlank(message = "描述不能为空！")
    private String description;
    //活动关联奖品列表
    @NotEmpty(message = "活动关联奖品列表不能为空！")
    @Valid  //加上这个注解才可以对List<>里面进行验证
    private List<CreatePrizeByActivityParam> activityPrizeList;
    //活动关联人员列表
    @NotEmpty(message = "活动关联人员列表不能为空！")
    private List<CreateUserByActivityParam> activityUserList;


}
