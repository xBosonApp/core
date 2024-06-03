select
  count(1) as count
from
  sys_userinfo
where
  userid = ? or tel = ? or email = ?