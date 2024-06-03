SELECT
  pid, userid, password, password_dt, tel, email, count(1) c, status
FROM
  sys_userinfo
WHERE
  userid = ?
union all
  SELECT
    pid, userid, password, password_dt, tel, email, count(1) c, status
  FROM
    sys_userinfo
  WHERE
    tel = ?
union all
  SELECT
    pid, userid, password, password_dt, tel, email, count(1) c, status
  FROM
    sys_userinfo
  WHERE
    email = ?