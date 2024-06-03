/* 打开机构数据库配置 */

Select
  did, dn, owner, dbtype, cn, flg, mark, status,
  dhost, dport, url, user_name, pass, en
FROM
  sys_pl_drm_ds001
Where
  owner = ? and flg = 9