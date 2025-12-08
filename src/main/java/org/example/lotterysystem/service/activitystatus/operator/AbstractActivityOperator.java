package org.example.lotterysystem.service.activitystatus.operator;

import org.example.lotterysystem.service.dto.ConvertActivityStatusDTO;

public abstract class AbstractActivityOperator {
    //控制处理顺序
    public abstract  Integer sequence();
    //是否需要转换
    public abstract Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO);
    //转换方法
    public abstract  Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO);
}
