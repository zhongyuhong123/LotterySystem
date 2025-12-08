package org.example.lotterysystem.service;

import org.example.lotterysystem.controller.param.CreateActivityParam;
import org.example.lotterysystem.controller.param.PageParam;
import org.example.lotterysystem.service.dto.ActivityDTO;
import org.example.lotterysystem.service.dto.ActivityDetailDTO;
import org.example.lotterysystem.service.dto.CreateActivityDTO;
import org.example.lotterysystem.service.dto.PageListDTO;

public interface ActivityService {

    //创建活动
    CreateActivityDTO createActivity(CreateActivityParam param);

    //翻页查询活动(摘要)列表
    PageListDTO<ActivityDTO> findActivityList(PageParam param);

    //获取活动详细属性
    ActivityDetailDTO getActivityDetail(Long activityId);

    //缓存活动详细信息（读取表数据 在缓存）
    void cacheActivity(Long activityId);
}
