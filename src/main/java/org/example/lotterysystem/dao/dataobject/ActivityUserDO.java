package org.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityUserDO extends BaseDO{
    private Long userId;
    private Long activityId;
    private String status;
    private String userName;
}
