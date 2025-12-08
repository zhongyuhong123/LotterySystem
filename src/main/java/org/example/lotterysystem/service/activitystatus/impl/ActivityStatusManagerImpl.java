package org.example.lotterysystem.service.activitystatus.impl;

import org.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import org.example.lotterysystem.common.exception.ServiceException;
import org.example.lotterysystem.service.ActivityService;
import org.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import org.example.lotterysystem.service.activitystatus.operator.AbstractActivityOperator;
import org.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class ActivityStatusManagerImpl implements ActivityStatusManager {

    private static final Logger logger = LoggerFactory.getLogger(ActivityStatusManagerImpl.class);

    @Autowired //这里的Autowired会将operator中的其它属性进行注入
    private final Map<String, AbstractActivityOperator> operatorMap = new HashMap<>();
    @Autowired
    private ActivityService activityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO) {
        // 传统写法的弊端：
        // 1、活动状态扭转有依赖性，导致代码维护性差
        // 2、状态扭转条件可能会扩展，当前写法。扩展性差，维护性差。

        //map<Sting, AbstractActivityOperator>

        if(CollectionUtils.isEmpty(operatorMap)){
            logger.warn("operatorMap 为空!");
            return;
        }

        Map<String, AbstractActivityOperator> curMap = new HashMap<>(operatorMap);
        Boolean update = false;

        // 先处理：人员、奖品
        update = processConvertStatus(convertActivityStatusDTO,curMap,1);

        //后处理：活动
        update = processConvertStatus(convertActivityStatusDTO,curMap,2) || update;

        //更新缓存
        if(update){
            activityService.cacheActivity(convertActivityStatusDTO.getActivityId());
        }

    }

    @Override
    public void rollbackHandlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO) {
        //operatorMap: 活动、奖品、人员按顺序更新
        for(AbstractActivityOperator operator : operatorMap.values()){
            operator.convert(convertActivityStatusDTO);
        }

        //缓存更新
        activityService.cacheActivity(convertActivityStatusDTO.getActivityId());
    }

    //扭转状态
    private Boolean processConvertStatus(ConvertActivityStatusDTO convertActivityStatusDTO,
                                         Map<String, AbstractActivityOperator> curMap, int sequence) {
        Boolean update = false;
        //遍历curMap
        Iterator<Map.Entry<String, AbstractActivityOperator>> iterator = curMap.entrySet().iterator();
        while(iterator.hasNext()){
            AbstractActivityOperator operator = iterator.next().getValue();
            //Operator是否需要转换：
            if(operator.sequence() != sequence
            || !operator.needConvert(convertActivityStatusDTO)){
                continue;
            }

            //需要转换：转换
            if(!operator.convert(convertActivityStatusDTO)){
                logger.error("{}状态转换失败！",operator.getClass().getName());
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_STATUS_CONVERT_ERROR);
            }

            //curMap 删除当前Operator
            iterator.remove();
            update = true;
        }

        //返回
        return update;
    }
}
