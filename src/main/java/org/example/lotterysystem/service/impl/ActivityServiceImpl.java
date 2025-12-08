package org.example.lotterysystem.service.impl;

import org.example.lotterysystem.common.Util.JacksonUtil;
import org.example.lotterysystem.common.Util.RedisUtil;
import org.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import org.example.lotterysystem.common.exception.ServiceException;
import org.example.lotterysystem.controller.param.CreateActivityParam;
import org.example.lotterysystem.controller.param.CreatePrizeByActivityParam;
import org.example.lotterysystem.controller.param.CreateUserByActivityParam;
import org.example.lotterysystem.controller.param.PageParam;
import org.example.lotterysystem.dao.dataobject.ActivityDO;
import org.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import org.example.lotterysystem.dao.dataobject.ActivityUserDO;
import org.example.lotterysystem.dao.dataobject.PrizeDO;
import org.example.lotterysystem.dao.mapper.*;
import org.example.lotterysystem.service.ActivityService;
import org.example.lotterysystem.service.dto.ActivityDTO;
import org.example.lotterysystem.service.dto.ActivityDetailDTO;
import org.example.lotterysystem.service.dto.CreateActivityDTO;
import org.example.lotterysystem.service.dto.PageListDTO;
import org.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import org.example.lotterysystem.service.enums.ActivityPrizeTierEnum;
import org.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);
    private final String ACTIVITY_PREFIX = "ACTIVITY_";
    private final Long ACTIVITY_TIMEOUT = 60 * 60 * 24 * 3L;


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private ActivityUserMapper activityUserMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateActivityDTO createActivity(CreateActivityParam param) {
        //校验活动信息是否正确
        checkActivityInfo(param);
        
        //保存活动信息
        ActivityDO activityDO = new ActivityDO();
        activityDO.setActivityName(param.getActivityName());
        activityDO.setDescription(param.getDescription());
        activityDO.setStatus(ActivityStatusEnum.RUNNING.name());
        activityMapper.insert(activityDO);

        //保存活动关联的奖品信息
        List<CreatePrizeByActivityParam> prizeParams = param.getActivityPrizeList();
        List<ActivityPrizeDO> activityPrizeDOList = prizeParams.stream()
                .map(prizeParam ->{
                    ActivityPrizeDO activityPrizeDO = new ActivityPrizeDO();
                    activityPrizeDO.setActivityId(activityDO.getId());
                    activityPrizeDO.setPrizeId(prizeParam.getPrizeId());
                    activityPrizeDO.setPrizeAmount(prizeParam.getPrizeAmount());
                    activityPrizeDO.setStatus(ActivityPrizeStatusEnum.INIT.name());
                    activityPrizeDO.setPrizeTiers(prizeParam.getPrizeTiers());
                    return activityPrizeDO;
                }).collect(Collectors.toList());
        activityPrizeMapper.batchInsert(activityPrizeDOList);

        //保存活动关联的人员信息
        List<CreateUserByActivityParam> userParams = param.getActivityUserList();
        List<ActivityUserDO> activityUserDOList = userParams.stream()
                .map(userParam ->{
                    ActivityUserDO activityUserDO = new ActivityUserDO();
                    activityUserDO.setUserId(userParam.getUserId());
                    activityUserDO.setActivityId(activityDO.getId());
                    activityUserDO.setStatus(ActivityUserStatusEnum.INIT.name());
                    activityUserDO.setUserName(userParam.getUserName());
                    return activityUserDO;
                }).collect(Collectors.toList());
        activityUserMapper.batchInsert(activityUserDOList);

        //整合完整的活动信息，存放到redis
            //activityId: ActivityDetailDTO: 活动+奖品+人员
            //先获取奖品基本属性列表
        List<Long> prizeIds = param.getActivityPrizeList().stream().map(CreatePrizeByActivityParam::getPrizeId)
                .distinct()
                .collect(Collectors.toList());
        List<PrizeDO> prizeDOList = prizeMapper.batchSelectByIds(prizeIds);

        ActivityDetailDTO detailDTO = convertToActivityDetailDTO(activityDO,activityUserDOList,
                prizeDOList, activityPrizeDOList);

        cacheActivity(detailDTO);

        //构造返回
        CreateActivityDTO createActivityDTO = new CreateActivityDTO();
        createActivityDTO.setActivityId(activityDO.getId());
        return createActivityDTO;
    }

    @Override
    public PageListDTO<ActivityDTO> findActivityList(PageParam param) {
        //获取总量
        int total = activityMapper.count();
//        logger.info("total {}", total);

        //获取当前页列表
        List<ActivityDO> activityDOList = activityMapper.selectActivityList(param.offset(), param.getPageSize());
        List<ActivityDTO>  activityDTOList = activityDOList.stream()
                .map(activityDO -> {
                    ActivityDTO activityDTO = new ActivityDTO();
                    activityDTO.setActivityId(activityDO.getId());
                    activityDTO.setActivityName(activityDO.getActivityName());
                    activityDTO.setDescription(activityDO.getDescription());
                    activityDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));
                    return activityDTO;
                }).collect(Collectors.toList());
        return new PageListDTO<>(total, activityDTOList);
    }

    @Override
    public ActivityDetailDTO getActivityDetail(Long activityId) {
        if(null == activityId) {
            logger.warn("查询活动详细信息的activityId is null");
            return null;
        }
        //查询redis
        ActivityDetailDTO detailDTO = getActivityFromCache(activityId);
        if(null != detailDTO) {
            logger.info("查询活动详细信息成功！detailDTO={}",
                    JacksonUtil.writeValueAsString(detailDTO));
        }

        //如果redis不存在，查表
        //活动表
        ActivityDO aDO = activityMapper.selectById(activityId);
        //活动奖品表
        List<ActivityPrizeDO> apDOList = activityPrizeMapper.selectByActivityId(activityId);
        //活动人员表
        List<ActivityUserDO> auDOList = activityUserMapper.selectByActivityId(activityId);
        //奖品表
        List<Long> prizeIds = apDOList.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());
        List<PrizeDO> pDOList = prizeMapper.batchSelectByIds(prizeIds);

        //整合活动详细信息，存放redis
        detailDTO = convertToActivityDetailDTO(aDO,auDOList,pDOList,apDOList);
        cacheActivity(detailDTO);

        //返回
        return detailDTO;
    }

    @Override
    public void cacheActivity(Long activityId) {
        if(null == activityId) {
            logger.warn("要缓存的活动id为空!");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_IS_EMPTY);
        }

        //查询表数据：活动表、关联奖品、关联人员、奖品信息表
        ActivityDO aDO = activityMapper.selectById(activityId);
        if(null == aDO) {
            logger.error("要缓存的活动id有误!");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_ERROR);
        }
        //活动奖品表
        List<ActivityPrizeDO> apDOList = activityPrizeMapper.selectByActivityId(activityId);
        //活动人员表
        List<ActivityUserDO> auDOList = activityUserMapper.selectByActivityId(activityId);
        //奖品表
        List<Long> prizeIds = apDOList.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());
        List<PrizeDO> pDOList = prizeMapper.batchSelectByIds(prizeIds);

        //整合活动详细信息，存放redis
        ActivityDetailDTO detailDTO = convertToActivityDetailDTO(aDO,auDOList,pDOList,apDOList);
        cacheActivity(detailDTO);
    }

    //缓存
    private void cacheActivity(ActivityDetailDTO detailDTO) {
        //key: "ACTIVITY_" + activityId
        //value: ActivityDetailDTO(json)
        if(null == detailDTO || null == detailDTO.getActivityId()){
            logger.warn("要缓存的活动信息不存在！");
            return;
        }
        try{
            redisUtil.set(ACTIVITY_PREFIX+detailDTO.getActivityId(),
                    JacksonUtil.writeValueAsString(detailDTO),
                    ACTIVITY_TIMEOUT);
        } catch(Exception e){
            logger.error("缓存活动异常，ActivityDetailDTO={}",
                    JacksonUtil.writeValueAsString(detailDTO),
                    e);
        }
    }

    private ActivityDetailDTO getActivityFromCache(Long activityId) {
        if(null == activityId){
            logger.warn("获取缓存活动数据的activityId为空！");
            return null;
        }
        try{
            String str = redisUtil.get(ACTIVITY_PREFIX+activityId);
            if(!StringUtils.hasText(str)){
                logger.info("获取的缓存数据为空！key={}",ACTIVITY_PREFIX+activityId);
                return null;
            }
            return JacksonUtil.readValue(str, ActivityDetailDTO.class);
        } catch (Exception e) {
            logger.error("从缓存中获取活动信息异常， activityId={}",
                    activityId,
                    e);
            return null;
        }

    }

    //根据基本DO表，整合成同一张表
    private ActivityDetailDTO convertToActivityDetailDTO(ActivityDO activityDO, List<ActivityUserDO> activityUserDOList,
                                                         List<PrizeDO> prizeDOList, List<ActivityPrizeDO> activityPrizeDOList) {
        ActivityDetailDTO detailDTO = new ActivityDetailDTO();
        detailDTO.setActivityId(activityDO.getId());
        detailDTO.setActivityName(activityDO.getActivityName());
        detailDTO.setDescription(activityDO.getDescription());
        detailDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));

        //apDO:{prizeId amount  status}, {prizeId, amount, status}
        //pDO:{prizeId, name...} {prizeId,name...}
        List<ActivityDetailDTO.PrizeDTO> prizeDTOList = activityPrizeDOList
                .stream()
                .map(apDO ->{
                    ActivityDetailDTO.PrizeDTO prizeDTO = new ActivityDetailDTO.PrizeDTO();
                    prizeDTO.setPrizeId(apDO.getPrizeId());
                    Optional<PrizeDO> optionalPrizeDO = prizeDOList.stream()
                                .filter(prizeDO -> prizeDO.getId().equals(apDO.getPrizeId()))
                                .findFirst();
                    //当PrizeDO不为空，才执行当前方法
                    optionalPrizeDO.ifPresent(prizeDO -> {
                        prizeDTO.setName(prizeDO.getName());
                        prizeDTO.setImageUrl(prizeDO.getImageUrl());
                        prizeDTO.setPrice(prizeDO.getPrice());
                        prizeDTO.setDescription(prizeDO.getDescription());
                    });

                    prizeDTO.setTier(ActivityPrizeTierEnum.forName(apDO.getPrizeTiers()));
                    prizeDTO.setPrizeAmount(apDO.getPrizeAmount());
                    prizeDTO.setStatus(ActivityPrizeStatusEnum.forName(apDO.getStatus()));

                    return prizeDTO;
                }).collect(Collectors.toList());
        detailDTO.setPrizeDTOList(prizeDTOList);

        List<ActivityDetailDTO.UserDTO> userDTOList = activityUserDOList
                .stream()
                .map(auDO ->{
                    ActivityDetailDTO.UserDTO userDTO = new ActivityDetailDTO.UserDTO();
                    userDTO.setUserId(auDO.getUserId());
                    userDTO.setUserName(auDO.getUserName());
                    userDTO.setStatus(ActivityUserStatusEnum.forName(auDO.getStatus()));
                    return userDTO;
                }).collect(Collectors.toList());
        detailDTO.setUserDTOList(userDTOList);
        return detailDTO;
    }

    //校验活动有效性
    private void checkActivityInfo(CreateActivityParam param) {
        if(null == param){
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_INFO_IS_EMPTY);
        }

        //人员id在人员表中存在
        //1 2 3 ->  1 2
        List<Long> userIds = param.getActivityUserList()
                .stream()
                .map(CreateUserByActivityParam::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> existUserIds = userMapper.selectExistByIds(userIds);
        if(CollectionUtils.isEmpty(existUserIds)){
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
        }
        userIds.forEach(id->{
            if(!existUserIds.contains(id)){
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
            }
        });

        //奖品id在奖品表中是否存在
        List<Long> prizeId = param.getActivityPrizeList()
                .stream()
                .map(CreatePrizeByActivityParam::getPrizeId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> exitPrizeIds = prizeMapper.selectExistByIds(prizeId);
        if(CollectionUtils.isEmpty(exitPrizeIds)){
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
        }
        prizeId.forEach(id -> {
            if(!exitPrizeIds.contains(id)){
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
            }
        });

        //人员数量应该大于等于奖品数量
        int userAmount =param.getActivityUserList().size();
        long prizeAmount = param.getActivityPrizeList()
                .stream()
                .mapToLong(CreatePrizeByActivityParam::getPrizeAmount)
                .sum();
        if(userAmount < prizeAmount){
            throw  new ServiceException(ServiceErrorCodeConstants.USER_PRIZE_AMOUNT_ERROR);
        }

        //校验活动奖品等奖有效性
        param.getActivityPrizeList().forEach(prize->{
            if(null == ActivityPrizeTierEnum.forName(prize.getPrizeTiers())){
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_TIERS_ERROR);
            }
        });

    }
}
