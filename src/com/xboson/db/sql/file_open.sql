
SELECT 
    content, `update-time`, `content-type`
FROM
    sys_upload_files
WHERE
    dir = ? AND filename = ?