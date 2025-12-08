package org.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class ShowWinningRecordsParam implements Serializable {

    @NotNull(message = "活动id不能为空！")
    private Long activityId;

    private Long prizeId;
}
