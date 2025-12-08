package org.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityPrizeDO extends BaseDO {
    //关联的活动id
    private Long activityId;
    //关联的奖品id
    private Long prizeId;
    //奖品数量
    private Long prizeAmount;
    //奖品状态
    private String status;
    //奖品等级
    private String prizeTiers;
}
