SELECT
  uri, status, tp_appnm
FROM
  sys_pl_tp_app
Where
  tp_appid =?
  and app_secret =?