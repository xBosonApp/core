/* 配置连接 */

Insert into
  sys_pl_drm_ds001
  (
    did,   owner,  dbtype,    en,
    dhost, dport,  user_name, pass,
    flg,   status, createdt,  updatedt,
    dn,    cn,     mark
  )
Values
  (
    ?, ?, ?, ?,
    ?, ?, ?, ?,
    '9', '1', now(), now(),
    'xBonson API 数据源', '脚本数据源',
    '由 xBonson 平台生成, 每个机构一个数据源配置, 勿手动修改'
  )