package org.example.lotterysystem.service.activitystatus.operator;

import org.example.lotterysystem.dao.dataobject.ActivityUserDO;
import org.example.lotterysystem.dao.mapper.ActivityUserMapper;
import org.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import org.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.List;

@Component
public class UserOperator extends AbstractActivityOperator{

    @Autowired
    private ActivityUserMapper activityUserMapper;

    @Override
    public Integer sequence() {
        return 1;
    }

    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        //判断当前活动状态是否 不是COMPLETE
        Long activityId = convertActivityStatusDTO.getActivityId();
        List<Long> userIds = convertActivityStatusDTO.getUserIds();
        ActivityUserStatusEnum targetUserStatus = convertActivityStatusDTO.getTargetUserStatus();
        if(activityId == null
            || CollectionUtils.isEmpty(userIds)
            || targetUserStatus == null){
            return false;
        }
        List<ActivityUserDO> activityUserDOList = activityUserMapper.batchSelectByAUId(activityId, userIds);
        if(CollectionUtils.isEmpty(activityUserDOList)){
            return false;
        }

        for(ActivityUserDO auDO : activityUserDOList){
            if(auDO.getStatus()
                    .equalsIgnoreCase(targetUserStatus.name())){
                return false;
            }
        }

        return true;
    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        List<Long> userIds = convertActivityStatusDTO.getUserIds();
        ActivityUserStatusEnum targetUserStatus = convertActivityStatusDTO.getTargetUserStatus();
        try{
            activityUserMapper.batchUpdateStatus(activityId, userIds, targetUserStatus);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
