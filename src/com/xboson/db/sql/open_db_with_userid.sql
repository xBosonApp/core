/*
 查询数据库连接, 并判断当前用户是否能操作
 成功返回一行数据库连接信息
 */
select
       CONVERT(s.dbtype, signed) `dbid`,
       s.dhost `host`,
       s.dport `port`,
       s.user_name `username`,
       s.pass  `password`,
       s.en    `database`
  from
       sys_pl_drm_ds001 s
  join
       mdm_org o
	  on o.orgid = s.owner and o.status = '1'
  join
       sys_tenant_user tu
    on tu.orgid = o.orgid and tu.status = '1'
  join
       sys_userinfo u
    on u.pid = tu.pid and u.status = '1'
 where
       s.did = ?
   and u.userid = ?
   and s.status = '1';