select
       admin_flag
  from
       sys_tenant_user, sys_userinfo
 where
       sys_userinfo.pid = sys_tenant_user.pid
   and sys_userinfo.userid = ?
   and sys_tenant_user.orgid = ?