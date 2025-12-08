package org.example.lotterysystem.service.activitystatus.operator;

import org.example.lotterysystem.dao.dataobject.ActivityDO;
import org.example.lotterysystem.dao.mapper.ActivityMapper;
import org.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import org.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import org.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import org.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivityOperator extends AbstractActivityOperator{

    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Override
    public Integer sequence() {
        return 2;
    }

    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        ActivityStatusEnum targetStatus = convertActivityStatusDTO.getTargetActivityStatus();

        if(null == convertActivityStatusDTO.getActivityId()
                || null == targetStatus){
            return false;
        }

        ActivityDO activityDO = activityMapper.selectById(activityId);
        if(null == activityDO){
            return false;
        }

        //当前活动状态与传入的状态一致，不处理
        if(targetStatus.name().equalsIgnoreCase(activityDO.getStatus())){
            return false;
        }

        //需要判读奖品是否全部抽完
            //查询Running状态的奖品个数
        int count = activityPrizeMapper.countPrize(activityId, ActivityPrizeStatusEnum.INIT.name());
        return count == 0;
    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        try{
            activityMapper.updateStatus(convertActivityStatusDTO.getActivityId(),
                    convertActivityStatusDTO.getTargetActivityStatus().name());
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
