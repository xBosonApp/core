--
-- xBoson 平台附加数据库初始化过程
--

CREATE TABLE `sys_upload_files` (
  `filename` varchar(80) NOT NULL,
  `dir` varchar(200) NOT NULL,
  `content` longblob,
  `create-date` datetime(6) NOT NULL,
  `update-time` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `id` varchar(30) NOT NULL,
  `content-type` varchar(100) DEFAULT NULL,
  `apiid` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`filename`,`dir`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


CREATE TABLE `sys_pl_log_scheduler` (
  `scheduleid` VARCHAR(40) NOT NULL,
  `create_date` DATETIME NOT NULL,
  `content` MEDIUMTEXT NULL,
  `api` VARCHAR(500) NOT NULL
) ENGINE = MyISAM;


CREATE TABLE `sys_pl_log_uimodify` (
  `dt` DATETIME NOT NULL,
  `patch` MEDIUMTEXT NOT NULL,
  `path` VARCHAR(300) NOT NULL,
  `user_id` VARCHAR(45) NOT NULL
) COMMENT = 'UI modify history';


ALTER TABLE `sys_pl_log_access`
CHANGE COLUMN `access_cd` `access_cd` CHAR(6) NULL DEFAULT NULL
COMMENT '登录状态代码' ;


ALTER TABLE `sys_pl_log_uimodify`
ADD INDEX `user_id` (`user_id` ASC),
ADD INDEX `time_dt` (`dt` DESC);


CREATE TABLE `sys_pl_app_token` (
  `client_id` VARCHAR(32) NOT NULL,
  `token` VARCHAR(90) NOT NULL,
  `userid` VARCHAR(45) NOT NULL,
  `birth_time` DATETIME NOT NULL DEFAULT now(),
  `expires_in` INT NOT NULL COMMENT '生命长度, 秒',
  `enable` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`token`),
  UNIQUE INDEX `token_UNIQUE` (`token` ASC))
COMMENT = '第三方应用登录令牌';
