package org.example.lotterysystem.dao.mapper;

import org.apache.ibatis.annotations.*;
import org.example.lotterysystem.dao.dataobject.MqErrorMsgDO;

import java.util.Date;
import java.util.List;

/**
 * MQ异常消息Mapper
 *
 * @author lottery-system
 */
@Mapper
public interface MqErrorMsgMapper {

    /**
     * 插入异常消息
     */
    @Insert({
            "INSERT INTO mq_error_msg (message_id, message_content, status, error_reason, retry_count, last_retry_time)",
            "VALUES (#{messageId}, #{messageContent}, #{status}, #{errorReason}, #{retryCount}, #{lastRetryTime})"
    })
    int insert(MqErrorMsgDO mqErrorMsgDO);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM mq_error_msg WHERE id = #{id}")
    MqErrorMsgDO selectById(@Param("id") Long id);

    /**
     * 根据消息ID查询
     */
    @Select("SELECT * FROM mq_error_msg WHERE message_id = #{messageId}")
    MqErrorMsgDO selectByMessageId(@Param("messageId") String messageId);

    /**
     * 根据状态查询（如查询所有未处理的消息）
     */
    @Select("SELECT * FROM mq_error_msg WHERE status = #{status}")
    List<MqErrorMsgDO> selectByStatus(@Param("status") String status);

    /**
     * 更新消息状态和失败原因
     */
    @Update({
            "UPDATE mq_error_msg SET ",
            "status = #{status}, ",
            "error_reason = #{errorReason}, ",
            "retry_count = #{retryCount}, ",
            "last_retry_time = #{lastRetryTime} ",
            "WHERE id = #{id}"
    })
    int updateById(MqErrorMsgDO mqErrorMsgDO);

    /**
     * 递增重试次数
     */
    @Update({
            "UPDATE mq_error_msg SET ",
            "retry_count = retry_count + 1, ",
            "last_retry_time = #{lastRetryTime}, ",
            "status = #{status} ",
            "WHERE message_id = #{messageId}"
    })
    int incrementRetryCount(
            @Param("messageId") String messageId,
            @Param("status") String status,
            @Param("lastRetryTime") Date lastRetryTime
    );
}