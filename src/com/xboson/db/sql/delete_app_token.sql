DELETE FROM `sys_pl_app_token`
WHERE
  token = ?
  AND client_id = ?