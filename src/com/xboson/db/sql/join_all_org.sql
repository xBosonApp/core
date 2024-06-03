/* 将用户加入所有的机构, 用于管理员帐号的建立 */
Insert into
  sys_tenant_user
  (orgid, pid, admin_flag, status, createdt, updatedt)
Select
  sys_tenant.orgid AS orgid,
                 ? AS pid,
               "1" AS admin_flag,
               "1" AS status,
             now() AS createdt,
             now() AS updatedt
From
  sys_tenant;