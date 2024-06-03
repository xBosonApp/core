/* 查询 mysql 账户是否存在 */
SELECT
    COUNT(1)
FROM
    mysql.user
WHERE
    User = ?;