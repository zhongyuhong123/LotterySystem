package org.example.lotterysystem.service.dto;

import lombok.Data;
import org.example.lotterysystem.service.enums.ActivityStatusEnum;

@Data
public class ActivityDTO {

    //活动id
    private Long activityId;
    //活动名称
    private String activityName;
    //活动描述
    private String description;
    //活动状态
    private ActivityStatusEnum status;

    //活动是否有效
    public Boolean valid(){
        return status.equals(ActivityStatusEnum.RUNNING);
    }
}
