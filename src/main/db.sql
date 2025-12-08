create database if not exists java_gobang;
use java_gobang;

drop table if exists user;

create table user(
     userId int primary key auto_increment,
     username varchar(50) unique,
     password varchar(50),
     score int, -- 天梯分数
     totalCount int, -- ⽐赛总场次
     winCount int -- 获胜场次
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

insert into user values(null, '张三', '123', 1000, 0, 0);
insert into user values(null, '李四', '123', 1000, 0, 0);
insert into user values(null, '王五', '123', 1000, 0, 0);
