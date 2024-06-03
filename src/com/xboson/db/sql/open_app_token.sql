SELECT `client_id`,
    `userid`,
    `birth_time`,
    `expires_in`,
    `enable`
FROM `sys_pl_app_token`
WHERE token = ?
  AND enable = 1
