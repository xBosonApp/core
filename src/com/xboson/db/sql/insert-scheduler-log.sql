-- 插入一条计划任务日志

INSERT INTO `sys_pl_log_scheduler`
  (
  `scheduleid`,
  `create_date`,
  `content`,
  `api`
  )
VALUES
  (
  ?, ?, ?, ?
  );
