DELETE FROM
    `sys_upload_files`
WHERE
    `update-time` < DATE_SUB(CURDATE(), interval 2 day)