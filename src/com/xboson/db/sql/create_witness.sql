INSERT INTO sys_pl_chain_witness
(
  wnid,
  host,
  port,
  publickey,
  urlperfix,
  algorithm,
  createdt,
  updatedt
) VALUES (
  ?, ?, ?, ?, ?, ?, now(), now()
);
