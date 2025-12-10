/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50727 (5.7.27-log)
 Source Host           : localhost:3306
 Source Schema         : lottery_system

 Target Server Type    : MySQL
 Target Server Version : 50727 (5.7.27-log)
 File Encoding         : 65001

 Date: 11/11/2025 12:32:09
*/

CREATE DATABASE IF NOT EXISTS `lottery_system`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE `lottery_system`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `activity_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '活动名称',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '活动描述',
  `status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '活动状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_id`(`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity
-- ----------------------------

-- ----------------------------
-- Table structure for activity_prize
-- ----------------------------
DROP TABLE IF EXISTS `activity_prize`;
CREATE TABLE `activity_prize`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `activity_id` bigint(20) NOT NULL COMMENT '活动id',
  `prize_id` bigint(20) NOT NULL COMMENT '活动关联的奖品id',
  `prize_amount` bigint(20) NOT NULL DEFAULT 1 COMMENT '关联奖品的数量',
  `prize_tiers` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '奖品等级',
  `status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '活动奖品状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_id`(`id`) USING BTREE,
  UNIQUE INDEX `uk_a_p_id`(`activity_id`, `prize_id`) USING BTREE,
  INDEX `idx_activity_id`(`activity_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity_prize
-- ----------------------------

-- ----------------------------
-- Table structure for activity_user
-- ----------------------------
DROP TABLE IF EXISTS `activity_user`;
CREATE TABLE `activity_user`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `activity_id` bigint(20) NOT NULL COMMENT '活动时间',
  `user_id` bigint(20) NOT NULL COMMENT '圈选的用户id',
  `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_id`(`id`) USING BTREE,
  UNIQUE INDEX `uk_a_u_id`(`activity_id`, `user_id`) USING BTREE,
  INDEX `idx_activity_id`(`activity_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity_user
-- ----------------------------

-- ----------------------------
-- Table structure for prize
-- ----------------------------
DROP TABLE IF EXISTS `prize`;
CREATE TABLE `prize`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '奖品名称',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '奖品描述',
  `price` decimal(10, 2) NOT NULL COMMENT '奖品价值',
  `image_url` varchar(2048) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '奖品展示图',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_id`(`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prize
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户姓名',
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '邮箱',
  `phone_number` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '手机号',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录密码',
  `identity` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户身份',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_id`(`id`) USING BTREE,
  UNIQUE INDEX `uk_email`(`email`(30)) USING BTREE,
  UNIQUE INDEX `uk_phone_number`(`phone_number`(11)) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------

-- ----------------------------
-- Table structure for winning_record
-- ----------------------------
DROP TABLE IF EXISTS `winning_record`;
CREATE TABLE `winning_record`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `activity_id` bigint(20) NOT NULL COMMENT '活动id',
  `activity_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '活动名称',
  `prize_id` bigint(20) NOT NULL COMMENT '奖品id',
  `prize_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '奖品名称',
  `prize_tier` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '奖品等级',
  `winner_id` bigint(20) NOT NULL COMMENT '中奖人id',
  `winner_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '中奖人姓名',
  `winner_email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '中奖人邮箱',
  `winner_phone_number` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '中奖人电话',
  `winning_time` datetime NOT NULL COMMENT '中奖时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_id`(`id`) USING BTREE,
  UNIQUE INDEX `uk_w_a_p_id`(`winner_id`, `activity_id`, `prize_id`) USING BTREE,
  INDEX `idx_activity_id`(`activity_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 69 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of winning_record
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for mq_error_msg (MQ异常消息表)
-- ----------------------------
DROP TABLE IF EXISTS `mq_error_msg`;
CREATE TABLE `mq_error_msg` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `message_id` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'MQ消息唯一标识（对应发送的messageId）',
  `message_content` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'MQ消息完整内容（JSON格式）',
  `status` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'UNPROCESSED' COMMENT '消息状态：UNPROCESSED(未处理)/PROCESSING(处理中)/SUCCESS(处理成功)/FAILED(处理失败)',
  `error_reason` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '失败原因（异常堆栈/描述）',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '已重试次数',
  `last_retry_time` datetime NULL DEFAULT NULL COMMENT '最后重试时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC COMMENT = 'MQ死信/异常消息记录表';

SET FOREIGN_KEY_CHECKS = 1;

