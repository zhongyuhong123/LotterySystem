package org.example.lotterysystem.service.activitystatus;

import org.example.lotterysystem.service.dto.ConvertActivityStatusDTO;

public interface ActivityStatusManager {

    //处理活动相关状态转换
    void handlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO);
    //回滚活动相关状态
    void rollbackHandlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO);
}
