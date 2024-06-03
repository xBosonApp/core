Select distinct
       sys_pl_chain.physical_chain,
       sys_pl_chain.physical_channel,
       sys_pl_chain.chain_id
  From
	     sys_pl_chain,
	     sys_user_role
 Where
		(sys_pl_chain.chain_id = ? OR sys_pl_chain.name = ?)
    AND (   (   sys_pl_chain.roleid is null )
            OR
            (   sys_pl_chain.create_userid = ? )
            OR
            (   sys_user_role.roleid = sys_pl_chain.roleid
                AND
                sys_user_role.pid = ?
            )

        )
		AND sys_pl_chain.status  = '1'
		AND sys_user_role.status = '1'