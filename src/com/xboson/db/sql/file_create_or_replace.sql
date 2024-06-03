INSERT INTO `sys_upload_files`
  (
  `id`,
  `filename`,
  `dir`,
  `create-date`,
  `content-type`,
  `content`
  )
VALUES
  (
  ?, ?, ?, now(), ?, ?
  )
ON DUPLICATE KEY UPDATE
  `content-type` = `content-type`,
  `content` = `content`,
  `update-time` = now()