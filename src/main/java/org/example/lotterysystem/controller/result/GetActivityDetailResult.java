package org.example.lotterysystem.controller.result;

import lombok.Data;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GetActivityDetailResult implements Serializable {

    //活动信息
    private Long activityId;//活动id
    private String activityName;//活动名
    private String description;//活动描述
    private Boolean status;//活动状态

    //奖品信息（列表）
    private List<Prize> prizes;

    //人员信息（列表）
    private List<User> users;

    @Data
    public static class Prize{
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
        private String prizeTierName;
        //奖品数量
        private Long prizeAmount;
        //奖品状态
        public Boolean valid;

    }

    @Data
    public static class User{
        //用户id
        private Long userId;
        //姓名
        private String userName;
        //人员是否被抽取
        public Boolean valid;

    }
}
