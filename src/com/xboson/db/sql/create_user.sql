/* 该语句仅适用于创建 root 帐号 */
Insert into
  sys_userinfo
  ( pid, userid, password, password_dt,
    multiflag, status, createdt, updatedt
  ) values (
    ?, ?, ?, ?,
    "1", "1", now(), now()
  );

