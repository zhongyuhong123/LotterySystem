package org.example.lotterysystem.service.mq;


import org.example.lotterysystem.common.Util.JacksonUtil;
import org.example.lotterysystem.common.config.DirectRabbitConfig;
import org.example.lotterysystem.dao.dataobject.MqErrorMsgDO;
import org.example.lotterysystem.dao.mapper.MqErrorMsgMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.example.lotterysystem.common.config.DirectRabbitConfig.*;


//2.存放后，当前异常消息消费完成，死信队列消息处理完成，但异常消息被我们持久化存储到表中了
//3.解决异常
//4.完成脚本任务，判断异常消息表中是否存在数据，如果存在，表示消息未完成，此时处理消息
//5.处理消息：将消息发送给普通队列进行处理


@Component
@RabbitListener(queues = DLX_QUEUE_NAME)
public class DlxReceiver {
    private static final Logger logger = LoggerFactory.getLogger(DlxReceiver.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MqErrorMsgMapper mqErrorMsgMapper;

    @RabbitHandler
    public void process(Map<String, Object> message) {
        try {
            logger.info("开始处理死信队列消息：{}", JacksonUtil.writeValueAsString(message));

            // 1. 提取消息ID（从message中获取发送时的messageId）
            String messageId = (String) message.get("messageId");
            if (messageId == null) {
                messageId = UUID.randomUUID().toString(); // 兜底生成唯一ID
            }

            // 2. 构造异常消息DO
            MqErrorMsgDO errorMsg = new MqErrorMsgDO();
            errorMsg.setMessageId(messageId);
            errorMsg.setMessageContent(JacksonUtil.writeValueAsString(message));
            errorMsg.setStatus("UNPROCESSED"); // 初始状态：未处理
            errorMsg.setRetryCount(0); // 初始重试次数0

            // 3. 插入数据库（避免重复插入）
            MqErrorMsgDO existMsg = mqErrorMsgMapper.selectByMessageId(messageId);
            if (existMsg == null) {
                mqErrorMsgMapper.insert(errorMsg);
                logger.info("异常消息已入库，messageId：{}", messageId);
            } else {
                // 已存在则更新重试次数
                mqErrorMsgMapper.incrementRetryCount(
                        messageId,
                        "PROCESSING",
                        new Date()
                );
                logger.info("异常消息已存在，更新重试次数，messageId：{}", messageId);
            }

            // 4. （可选）手动重试：仅当重试次数<3时重新发送到普通队列
            if (existMsg == null || existMsg.getRetryCount() < 3) {
                rabbitTemplate.convertAndSend(DirectRabbitConfig.EXCHANGE_NAME, DirectRabbitConfig.ROUTING, message);
                logger.info("异常消息已重新发送到普通队列，messageId：{}", messageId);
            } else {
                // 重试次数耗尽，标记为失败
                MqErrorMsgDO failMsg = new MqErrorMsgDO();
                failMsg.setId(existMsg.getId());
                failMsg.setStatus("FAILED");
                failMsg.setErrorReason("重试次数耗尽（最大3次）");
                failMsg.setRetryCount(existMsg.getRetryCount());
                failMsg.setLastRetryTime(new Date());
                mqErrorMsgMapper.updateById(failMsg);
                logger.error("异常消息重试次数耗尽，标记为失败，messageId：{}", messageId);
            }

        } catch (Exception e) {
            logger.error("死信队列消息处理失败", e);
        }
    }
}
