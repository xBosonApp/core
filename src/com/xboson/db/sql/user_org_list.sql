SELECT
    sys_tenant.orgid orgid,
    mdm_org.de0810013j orgnm,
    sys_tenant.org_type org_type,
    sys_tenant.url,
    sys_tenant_user.admin_flag
FROM
    sys_tenant_user,
    sys_tenant,
    mdm_org,
    sys_userinfo
WHERE
        sys_tenant_user.pid = sys_userinfo.pid
		AND sys_userinfo.userid = ?
    AND sys_tenant_user.orgid = sys_tenant.orgid
    AND sys_tenant.orgid = mdm_org.orgid
    AND sys_tenant_user.status = '1'
    AND sys_tenant.status = '1'
    AND mdm_org.status = '1'