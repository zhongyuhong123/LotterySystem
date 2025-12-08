package org.example.lotterysystem.service.activitystatus.operator;

import org.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import org.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import org.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import org.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import org.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrizeOperator extends AbstractActivityOperator{

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Override
    public Integer sequence() {
        return 1;
    }

    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        //判断当前奖品状态是否 不是COMPLETE
        Long activityId = convertActivityStatusDTO.getActivityId();
        Long prizeId = convertActivityStatusDTO.getPrizeId();
        ActivityPrizeStatusEnum targetPrizeStatus = convertActivityStatusDTO.getTargetPrizeStatus();
        if(null == prizeId || null == targetPrizeStatus){
            return false;
        }
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(activityId, prizeId);
        if(null == activityPrizeDO){
            return false;
        }

        //判断当前奖品状态和目标状态是否一致
        if(targetPrizeStatus.name().equalsIgnoreCase(activityPrizeDO.getStatus())){
            return false;
        }

        return true;
    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        Long prizeId = convertActivityStatusDTO.getPrizeId();
        ActivityPrizeStatusEnum targetPrizeStatus = convertActivityStatusDTO.getTargetPrizeStatus();
        try{
            activityPrizeMapper.updateStatus(activityId, prizeId, targetPrizeStatus.name());
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
