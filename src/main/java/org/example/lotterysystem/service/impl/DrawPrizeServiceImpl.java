package org.example.lotterysystem.service.impl;

import org.example.lotterysystem.common.Util.JacksonUtil;
import org.example.lotterysystem.common.Util.RedisUtil;
import org.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import org.example.lotterysystem.common.exception.ServiceException;
import org.example.lotterysystem.controller.param.DrawPrizeParam;
import org.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import org.example.lotterysystem.dao.dataobject.*;
import org.example.lotterysystem.dao.mapper.*;
import org.example.lotterysystem.service.DrawPrizeService;
import org.example.lotterysystem.service.dto.WinningRecordDTO;
import org.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import org.example.lotterysystem.service.enums.ActivityPrizeTierEnum;
import org.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.lotterysystem.common.config.DirectRabbitConfig.EXCHANGE_NAME;
import static org.example.lotterysystem.common.config.DirectRabbitConfig.ROUTING;

@Service
public class DrawPrizeServiceImpl implements DrawPrizeService {

    private static final Logger logger = LoggerFactory.getLogger(DrawPrizeServiceImpl.class);
    private static final String WINNING_RECORDS_PREFIX = "WINNING_RECORDS_";
    private static final Long WINNING_RECORDS_TIMEOUT = 60*60*24*2L;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private WinningRecordMapper winningRecordMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void drawPrize(DrawPrizeParam param) {

        Map<String, String> map = new HashMap<>();
        map.put("messageId", String.valueOf(UUID.randomUUID()));
        map.put("messageData", JacksonUtil.writeValueAsString(param));
        //发消息: 交换机、绑定的key、消息体
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING, map);
        logger.info("mq消息发送成功：map={}", JacksonUtil.writeValueAsString(map));
    }

    @Override
    public Boolean checkPrizeParam(DrawPrizeParam param) {
        //奖品是否存在可以从 activity_prize 中取，原因是保存activity做了本地事务，保证一致性
        //活动是否有效
        ActivityDO activityDO = activityMapper.selectById(param.getActivityId());
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(
                param.getActivityId(), param.getPrizeId());
        if(activityDO == null || activityPrizeDO == null) {
//            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_OR_PRIZE_IS_EMPTY);
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.ACTIVITY_OR_PRIZE_IS_EMPTY.getMsg());
            return false;
        }
        //活动是否有效
        if(activityDO.getStatus()
                .equalsIgnoreCase(ActivityStatusEnum.COMPLETED.getMessage())){
//            throw  new ServiceException(ServiceErrorCodeConstants.ACTIVITY_IS_COMPLETED);
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.ACTIVITY_IS_COMPLETED.getMsg());
            return false;
        }

        //奖品是否有效
        if(activityPrizeDO.getStatus()
                .equalsIgnoreCase(ActivityPrizeStatusEnum.COMPLETED.getMessage())){
//            throw new ServiceException(ServiceErrorCodeConstants.PRIZE_IS_COMPLETED);
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.PRIZE_IS_COMPLETED.getMsg());
            return false;
        }

        //中奖者人数是否和设置奖品数量一致
        if(activityPrizeDO.getPrizeAmount() != param.getWinnerList().size()){
//            throw new ServiceException(ServiceErrorCodeConstants.WINNER_PRIZE_AMOUNT_NOT_EQUAL);
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.WINNER_PRIZE_AMOUNT_NOT_EQUAL.getMsg());
            return false;
        }
        return true;
    }

    @Override
    public List<WinningRecordDO> saveWinnerRecords(DrawPrizeParam param) {
        //查询相关信息：活动、人员、奖品、活动奖品关联表
        ActivityDO activityDO = activityMapper.selectById(param.getActivityId());
        List<UserDO> userDOList = userMapper.batchSelectByIds(
                param.getWinnerList().stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .collect(Collectors.toList())
        );
        PrizeDO prizeDO = prizeMapper.selectById(param.getPrizeId());
        ActivityPrizeDO activityPrizeDO =
                activityPrizeMapper.selectByAPId(param.getActivityId(), param.getPrizeId());

        //构造中奖者记录，保存
        List<WinningRecordDO> winningRecordDOList = userDOList.stream()
                .map(userDO -> {
                    WinningRecordDO winningRecordDO = new WinningRecordDO();
                    winningRecordDO.setActivityId(activityDO.getId());
                    winningRecordDO.setActivityName(activityDO.getActivityName());
                    winningRecordDO.setPrizeId(prizeDO.getId());
                    winningRecordDO.setPrizeName(prizeDO.getName());
                    winningRecordDO.setPrizeTier(activityPrizeDO.getPrizeTiers());
                    winningRecordDO.setWinnerId(userDO.getId());
                    winningRecordDO.setWinnerName(userDO.getUserName());
                    winningRecordDO.setWinnerEmail(userDO.getEmail());
                    winningRecordDO.setWinnerPhoneNumber(userDO.getPhoneNumber());
                    winningRecordDO.setWinningTime(param.getWinningTime());
                    return winningRecordDO;
                }).collect(Collectors.toList());

        winningRecordMapper.batchInsert(winningRecordDOList);

        //缓存中奖者记录
            //1.缓存奖品维度中奖记录(WinningRecord_activityId_prizeId, winningRecordDOList)
        cacheWinningRecords(param.getActivityId()+"_"+param.getPrizeId(),
                            winningRecordDOList,
                            WINNING_RECORDS_TIMEOUT);
            //2.缓存活动维度中奖记录(WinningRecord_activityId, winningRecordDOList)
            //当活动已完成再去存放活动维度的中奖记录
        if(activityDO.getStatus()
                .equalsIgnoreCase(ActivityStatusEnum.COMPLETED.name())){
            //查询活动维度的全量中奖记录
            List<WinningRecordDO> alllist = winningRecordMapper.selectByActivityId(param.getActivityId());
            cacheWinningRecords(String.valueOf(param.getActivityId()),
                    alllist,
                    WINNING_RECORDS_TIMEOUT);
        }

        return winningRecordDOList;
    }

    @Override
    public void deleteWinnerRecords(Long activityId, Long prizeId) {
        if(activityId == null) {
            logger.warn("要删除的中奖记录相关的活动id为空！");
            return;
        }
        //删除数据表
        winningRecordMapper.deleteRecords(activityId,prizeId);

        //删除缓存（奖品维度、活动维度）
        if(null != prizeId){
            deleteWinningRecords(activityId + "_" + prizeId);
        }
        //无论是否传递了prizeId，都需要删除活动维度的中奖记录缓存：
        //如果传递了prizeId，证明奖品未抽奖，必须删除活动维度的缓存记录。
        //如果没有传递prizeId，就知识删除活动维度的信息
        deleteWinningRecords(String.valueOf(activityId));
    }

    @Override
    public List<WinningRecordDTO> getRecords(ShowWinningRecordsParam param) {
        //查询redis
            //如果prizeId存在，则构造ActivityId+prizeId，否则构造ActivityId
        String key = null == param.getPrizeId()
                ? String.valueOf(param.getActivityId())
                : param.getActivityId() + "_" + param.getPrizeId();
        List<WinningRecordDO> winningRecordDOList = getWinningRecords(key);
        if(!CollectionUtils.isEmpty(winningRecordDOList)){
            return convertToWinningDTOList(winningRecordDOList);
        }

        //如果redis不存在，查库
        winningRecordDOList = winningRecordMapper.selectByActivityIdOrPrizeId(param.getActivityId(),param.getPrizeId());

        //存放记录到redis
        if(CollectionUtils.isEmpty(winningRecordDOList)){
            logger.info("查询的中奖记录为空！param:{}",JacksonUtil.writeValueAsString(param));
            return Arrays.asList();
        }
        cacheWinningRecords(key, winningRecordDOList, WINNING_RECORDS_TIMEOUT);
        return convertToWinningDTOList(winningRecordDOList);
    }

    private List<WinningRecordDTO> convertToWinningDTOList(List<WinningRecordDO> winningRecordDOList) {
        if(CollectionUtils.isEmpty(winningRecordDOList)){
            return Arrays.asList();
        }
        return winningRecordDOList.stream()
                .map(winningRecordDO ->{
                    WinningRecordDTO winningRecordDTO = new WinningRecordDTO();
                    winningRecordDTO.setWinnerId(winningRecordDO.getWinnerId());
                    winningRecordDTO.setWinnerName(winningRecordDO.getWinnerName());
                    winningRecordDTO.setPrizeName(winningRecordDO.getPrizeName());
                    winningRecordDTO.setPrizeTier(ActivityPrizeTierEnum.forName(winningRecordDO.getPrizeTier()));
                    winningRecordDTO.setWinningTime(winningRecordDO.getWinningTime());
                    return winningRecordDTO;
                }).collect(Collectors.toList());
    }

    private void deleteWinningRecords(String key) {
        try{
            if(redisUtil.hasKey(WINNING_RECORDS_PREFIX + key)){
                redisUtil.del(WINNING_RECORDS_PREFIX + key);
            }
        }catch (Exception e){
            logger.error("删除中奖记录缓存异常，key:{}", key);
        }
    }

    //缓存中奖记录
    private void cacheWinningRecords(String key,
                                     List<WinningRecordDO> winningRecordDOList,
                                     Long time) {
        String str = JacksonUtil.writeValueAsString(winningRecordDOList);
        try{
            if(!StringUtils.hasText(key)
                || CollectionUtils.isEmpty(winningRecordDOList)){
                logger.warn("要缓存的内容为空！key:{}, value:{}",key,str);
                return;
            }
            redisUtil.set(WINNING_RECORDS_PREFIX + key, str, time);
        }catch (Exception e){
            logger.error("缓存中奖记录异常！key:{}, value:{}",WINNING_RECORDS_PREFIX + key,str);
        }

    }

    //从缓存中获取中奖记录
    private List<WinningRecordDO> getWinningRecords(String key) {
        try{
            if(!StringUtils.hasText(key)){
                logger.warn("要从缓存中查询中奖记录的key为空！");
                return Arrays.asList();
            }
            String str = redisUtil.get(WINNING_RECORDS_PREFIX + key);
            if(!StringUtils.hasText(str)){
                return Arrays.asList();
            }
            List<WinningRecordDO> winningRecordDOList =
                    JacksonUtil.readListValue(str, WinningRecordDO.class);
            return winningRecordDOList;
        }catch (Exception e){
            logger.error("从缓存查询中奖记录异常！key:{}",WINNING_RECORDS_PREFIX + key);
            return Arrays.asList();
        }

    }

}
