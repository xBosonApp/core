/* 将用户加入所有的用户组, 用于管理员帐号的建立 */
Insert into
  sys_ug_user
  (ugid, pid, status, createdt, updatedt)
Select
  sys_ug.ugid AS ugid,
            ? AS pid,
          "1" AS status,
        now() AS createdt,
        now() AS updatedt
From
  sys_ug;