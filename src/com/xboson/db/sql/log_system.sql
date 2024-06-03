

INSERT INTO `sys_pl_log_system`
(
  `logid`,
  `log_time`,
  `log_level`,
  `log_error_type`,
  `requestid`,
  `serverid`,
  `log`,
  `createdt`
) VALUES (
  ?, -- 1 <{logid: }>,
  ?, -- 2 <{log_time: }>,
  ?, -- 3 <{log_level: }>,
  ?, -- 4 <{log_error_type: }>,
  ?, -- 5 <{requestid: }>,
  ?, -- 6 <{serverid: }>,
  ?, -- 7 <{log: }>,
  ?  -- 8 <{createdt: }>
);
