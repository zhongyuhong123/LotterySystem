package org.example.lotterysystem.controller.param;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class DrawPrizeParam {

    @NotNull(message = "活动id不能为空")
    private Long activityId;

    @NotNull(message = "奖品id不能为空")
    private Long prizeId;

//    @NotBlank(message = "奖品等级不能为空")
//    private String prizeTiers;

    //获奖时间
    @NotNull(message = "奖品时间不能为空")
    private Date winningTime;

    //中奖者列表
    @NotEmpty(message = "中奖者列表不能为空")
    @Valid
    private List<Winner> winnerList;

    @Data
    public static class Winner {
        @NotNull(message = "中奖者id不能为空")
        private Long userId;

        @NotBlank(message = "奖品名不能为空")
        private String userName;
    }
}
