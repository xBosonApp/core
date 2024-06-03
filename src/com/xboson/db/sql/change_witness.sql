UPDATE sys_pl_chain_witness
SET
  host = ?,
  port = ?,
  urlperfix = IFNULL(?, urlperfix),
  updatedt = now()
WHERE wnid = ?
