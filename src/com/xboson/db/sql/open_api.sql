
Select
       a.apinm,
       a.status,
       b.content,
       a.createdt,
       a.updatedt
  From
       sys_apis a,
       sys_api_content b
 Where
       a.contentid = b.contentid
   and a.appid = ?
   and a.moduleid = ?
   and a.apiid = ?
