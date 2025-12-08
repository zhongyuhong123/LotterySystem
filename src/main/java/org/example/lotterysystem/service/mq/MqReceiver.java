package org.example.lotterysystem.service.mq;

import cn.hutool.core.date.DateUtil;
import org.example.lotterysystem.common.Util.JacksonUtil;
import org.example.lotterysystem.common.Util.MailUtil;
import org.example.lotterysystem.common.exception.ServiceException;
import org.example.lotterysystem.controller.param.DrawPrizeParam;
import org.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import org.example.lotterysystem.dao.dataobject.WinningRecordDO;
import org.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import org.example.lotterysystem.dao.mapper.WinningRecordMapper;
import org.example.lotterysystem.service.DrawPrizeService;
import org.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import org.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import org.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import org.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.example.lotterysystem.service.enums.ActivityPrizeTierEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.example.lotterysystem.common.config.DirectRabbitConfig.QUEUE_NAME;

@Component
@RabbitListener(queues = QUEUE_NAME)
public class MqReceiver {

    private static final Logger logger = LoggerFactory.getLogger(MqReceiver.class);

    @Autowired
    private DrawPrizeService drawPrizeService;
    @Autowired
    private ActivityStatusManager activityStatusManager;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private WinningRecordMapper winningRecordMapper;

    @RabbitHandler
    public void process(Map<String, String> message){
        //成功接收到队列中的消息
        logger.info("MQ成功接收到消息，message: {}",
                JacksonUtil.writeValueAsString(message));

        String paramString = message.get("messageData");
        DrawPrizeParam param = JacksonUtil.readValue(paramString, DrawPrizeParam.class);
        //处理抽奖的流程

        try{
            //校验抽奖请求是否有效
            /**
             * 1、有可能前端发起两个一样的抽奖请求，对于param来说也是一样的两个请求
             * 2、param：最后一个奖项-》
             *      处理param1：活动完成、奖品完成
             *      吃力param2：回滚活动、奖品状态
             */
            if(!drawPrizeService.checkPrizeParam(param)){
                return;
            }
            //状态扭转处理 (*****important*******)
            statusConvert(param);

            //保存中奖者名单
            List<WinningRecordDO> winningRecordDOList = drawPrizeService.saveWinnerRecords(param);

            //通知中奖者（邮箱、短信）
            //抽奖之后后续流程，异步（并发处理）
            syncExecute(winningRecordDOList);

        }catch (ServiceException e){
            logger.error("处理 MQ 消息异常！{}:{}",e.getCode(),e.getMessage());
            //出现异常，需要保证实务一致性（回滚），抛出异常
            rollback(param);
            //抛出异常：消息重试（解决异常：代码bug、网络问题、服务器问题）
            throw e;
        }catch (Exception e){
            rollback(param);
            logger.error("处理 MQ 消息异常！",e);
        }

    }

    //处理抽奖异常的回滚行为
    private void rollback(DrawPrizeParam param) {
        //1.回滚状态：活动、奖品、人员
        if(!statusNeedRollback(param)){
            return;
        }
        rollbackStatus(param);

        //2.中奖者名单
        if(!winnerNeedRollback(param)){
            return;
        }
        rollbackWinner(param);
    }

    private void rollbackWinner(DrawPrizeParam param) {
        drawPrizeService.deleteWinnerRecords(param.getActivityId(), param.getPrizeId());
    }

    private boolean winnerNeedRollback(DrawPrizeParam param) {
        //判断活动中奖品是否存在中奖者
        int count = winningRecordMapper.countByAPId(param.getActivityId(), param.getPrizeId());
        return count > 0;
    }

    private void rollbackStatus(DrawPrizeParam param) {
        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(param.getActivityId());
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.RUNNING);
        convertActivityStatusDTO.setPrizeId(param.getPrizeId());
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.INIT);
        convertActivityStatusDTO.setUserIds(
                param.getWinnerList().stream()
                    .map(DrawPrizeParam.Winner::getUserId)
                    .collect(Collectors.toList())
        );
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.INIT);

        activityStatusManager.rollbackHandlerEvent(convertActivityStatusDTO);
    }

    private boolean statusNeedRollback(DrawPrizeParam param) {
        //扭转状态是看，使用了 @Transactional 包郑实务一致性，要么都扭转，要么没扭转
        //所以只需要判断人员/奖品是否扭转过，就能判断出状态是否全部扭转（活动不行，设置了顺序）
        //结论：判断奖品表状态是否扭转就行
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(param.getActivityId(),param.getPrizeId());
        return !(activityPrizeDO == null || activityPrizeDO.getStatus().equalsIgnoreCase(ActivityPrizeStatusEnum.INIT.name()));
    }

    //并发处理抽奖后续
    private void syncExecute(List<WinningRecordDO> winningRecordDOList) {
        //通过线程池 threadPoolTaskExecutor
        //拓展：加入一些策略模式或者其他设计模式来完成后续的异步操作

        //短信通知
            // todo 由于阿里云短信服务无法开通，这里使用网易邮箱代替
        threadPoolTaskExecutor.execute(()->sendMessage(winningRecordDOList));

        //邮件通知
        threadPoolTaskExecutor.execute(()->sendMail(winningRecordDOList));
    }

    private void sendMail(List<WinningRecordDO> winningRecordDOList) {
        if(CollectionUtils.isEmpty(winningRecordDOList)){
            logger.info("中奖列表为空，不用发送邮件！");
            return;
        }
        for(WinningRecordDO winningRecordDO : winningRecordDOList){
            String context = "Hi，" + winningRecordDO.getWinnerName() + "。恭喜你在"
                    + winningRecordDO.getActivityName() + "活动中获得" +
                    ActivityPrizeTierEnum.forName(winningRecordDO.getPrizeTier()).getMessage()
                    + "：" + winningRecordDO.getPrizeName() + "。获奖时间为"
                    + DateUtil.formatTime(winningRecordDO.getWinningTime()) + "，请尽快领取您的奖励！";

            mailUtil.sendSampleMail(winningRecordDO.getWinnerEmail(),"中奖通知",context);
        }
    }

    /**   发送短信 由于阿里云短信服务无法开通，这里使用网易邮箱代替    **/
    private void sendMessage(List<WinningRecordDO> winningRecordDOList) {
        if(CollectionUtils.isEmpty(winningRecordDOList)){
            logger.info("中奖列表为空，不用发送短信！");
            return;
        }

        for(WinningRecordDO winningRecordDO : winningRecordDOList){
            String mail163 = winningRecordDO.getWinnerPhoneNumber() +"@163.com";

            String context = "Hi，" + winningRecordDO.getWinnerName() + "。恭喜你在"
                    + winningRecordDO.getActivityName() + "活动中获得" +
                    ActivityPrizeTierEnum.forName(winningRecordDO.getPrizeTier()).getMessage()
                    + "：" + winningRecordDO.getPrizeName() + "。获奖时间为"
                    + DateUtil.formatTime(winningRecordDO.getWinningTime()) + "，请尽快领取您的奖励！";

            mailUtil.sendSampleMail(mail163,"中奖通知",context);
        }
    }

    //状态扭转
    private void statusConvert(DrawPrizeParam param) {

        //传统写法问题：
        // 1、活动状态扭转有依赖性，导致代码维护性差
        // 2、状态扭转条件可能会扩展，当前写法。扩展性差，维护性差。
        // 3、代码的灵活性、维护性极差
        // todo 解决方案： 设计模式（责任链设计模式、策略模式）


        //活动：RUNNING 全部抽完后-> COMPLETED
        //奖品：INIT --> COMPLETED
        //人员列表：INIT --> COMPLETED

        //1.扭转奖品状态
        //查询活动关联的奖品信息
        //条件判断是否符合扭转奖品状态：判断当前状态是否不是COMPLETED, 如果不是，才要扭转


        //2.扭转人员状态
        //查询活动关联的人员信息
        //条件判断是否符合扭转人员状态：判断当前状态是否不是COMPLETED, 如果不是，才要扭转

        //3.扭转活动状态（必须再扭转奖品状态之后完成）
        //查询活动信息
        //条件判断是否符合扭转活动状态：判断当前状态是否不是COMPLETED,
        // 如果不是，且**全部奖品抽完之后**，才要扭转

        //更新缓存

        statusConvert0(param);
    }

    /*****************----------------------新方法---------------------------*******************/

    //状态扭转
    private void statusConvert0(DrawPrizeParam param) {

        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(param.getActivityId());
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.COMPLETED);
        convertActivityStatusDTO.setPrizeId(param.getPrizeId());
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.COMPLETED);
        convertActivityStatusDTO.setUserIds(
                param.getWinnerList().stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .collect(Collectors.toList())
        );
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.COMPLETED);


        activityStatusManager.handlerEvent(convertActivityStatusDTO);

    }

}












