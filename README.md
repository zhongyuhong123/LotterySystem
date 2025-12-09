# LotterySystem

#### 介绍
这是一个基于 Spring Boot 3 构建的企业级抽奖系统，支持活动管理、奖品配置、人员管理、公平抽奖及中奖通知等核心功能，适用于营销活动、年会抽奖等场景，具备高可用、可扩展、安全可靠的特点。

#### 软件架构

软件架构说明
采用分层架构设计，清晰划分职责边界：
展示层：HTML + JavaScript + AJAX，负责页面交互与数据渲染
通信层：HTTP/HTTPS 协议实现前后端通信
服务层：Spring Boot 核心业务模块（用户服务、活动服务、奖品服务等）
数据层：MySQL 存储核心数据，Redis 作为缓存层
中间件：RabbitMQ 处理异步任务（如抽奖逻辑、消息通知）
安全与日志：JWT 身份认证、数据加密、SLF4J + logback 日志记录

技术栈
后端：Java 17、Spring Boot 3、MyBatis、JWT、Hutool
前端：JavaScript、jQuery、AJAX
数据库：MySQL
缓存：Redis
消息队列：RabbitMQ
其他：Maven、Git、阿里云短信服务、邮件服务

核心模块
用户模块：管理员注册登录、普通用户管理
奖品模块：奖品创建、图片上传、列表分页展示
活动模块：活动创建（关联奖品与人员）、活动列表分页展示
抽奖模块：多轮抽奖、中奖名单记录、异常处理与事务一致性
通知模块：短信 + 邮件中奖通知
安全模块：身份认证、敏感数据加密、接口访问控制

#### 安装教程

安装 JDK 17（参考《JDK17 安装》文档）
安装 MySQL（参考《MySQL 安装》文档）
安装 Redis（参考《Redis 安装》文档）
安装 RabbitMQ（参考《RabbitMQ 安装》文档）
安装 Maven（用于项目构建）

#### 使用说明

若需外网访问，开放服务器 8080 端口（或配置文件中自定义的端口）。
直接访问链接：http://8.148.20.81:8081/blogin.html

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
