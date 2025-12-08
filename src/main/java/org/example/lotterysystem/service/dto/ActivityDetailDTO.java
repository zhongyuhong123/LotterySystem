package org.example.lotterysystem.service.dto;

import lombok.Data;
import org.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import org.example.lotterysystem.service.enums.ActivityPrizeTierEnum;
import org.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.example.lotterysystem.service.enums.ActivityUserStatusEnum;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ActivityDetailDTO {

    //活动信息
    private Long activityId;//活动id
    private String activityName;//活动名
    private String description;//活动描述
    private ActivityStatusEnum status;//活动状态

    public Boolean valid(){
        return status.equals(ActivityStatusEnum.RUNNING);
    }

    //奖品信息（列表）
    private List<ActivityDetailDTO.PrizeDTO> prizeDTOList;

    //人员信息（列表）
    private List<ActivityDetailDTO.UserDTO> userDTOList;

    @Data
    public static class PrizeDTO{
        //奖品id
        private Long prizeId;
        //奖品名
        private String name;
        //图片索引
        private String imageUrl;
        //价格
        private BigDecimal price;
        //描述
        private String description;
        //奖品等级
        private ActivityPrizeTierEnum tier;
        //奖品数量
        private Long prizeAmount;
        //奖品状态
        private ActivityPrizeStatusEnum status;

        public Boolean valid(){
            return status.equals(ActivityPrizeStatusEnum.INIT);
        }
    }

    @Data
    public static class UserDTO{
        //用户id
        private Long userId;
        //姓名
        private String userName;
        //状态
        private ActivityUserStatusEnum status;

        public Boolean valid(){
            return status.equals(ActivityUserStatusEnum.INIT);
        }
    }
}
