ALTER TABLE `sys_pl_log_system`
  CHANGE COLUMN `log_error_type` `log_error_type` VARCHAR(100)
  NOT NULL COMMENT '日志名称'
