-- 通过 contentid(1) 返回 appid/modid/apiid
-- 该文件中含有文本模板标识符, 不能直接用 DB 执行
SELECT
    ap.appid `app`, ap.moduleid `mod`, ap.apiid `api`
FROM
    `%1$s`.sys_api_content ac,
    `%1$s`.sys_apis ap
WHERE
    ac.contentid = ?
        AND ac.contentid = ap.contentid
        AND ac.content IS NOT NULL
