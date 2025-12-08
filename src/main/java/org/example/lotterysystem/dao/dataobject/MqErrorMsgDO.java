package org.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * MQ异常消息DO
 *
 * @author lottery-system
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MqErrorMsgDO extends BaseDO{
    /**
     * 主键
     */
    private Long id;
    /**
     * MQ消息唯一标识（对应发送的messageId）
     */
    private String messageId;

    /**
     * MQ消息完整内容（JSON格式）
     */
    private String messageContent;

    /**
     * 消息状态：UNPROCESSED(未处理)/PROCESSING(处理中)/SUCCESS(处理成功)/FAILED(处理失败)
     */
    private String status;

    /**
     * 失败原因（异常堆栈/描述）
     */
    private String errorReason;

    /**
     * 已重试次数
     */
    private Integer retryCount;

    /**
     * 最后重试时间
     */
    private Date lastRetryTime;
}