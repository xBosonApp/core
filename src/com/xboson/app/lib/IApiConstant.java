////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-8 上午8:56
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/IApiConstant.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.util.c0nst.IConstant;


public interface IApiConstant {

  /** 所有通过 se 对象操作 redis 都会添加这个前缀 */
  String _R_KEY_PREFIX_                 = "/sys";

  String _CACHE_REGION_API_             = "/api";
  String _CACHE_REGION_SYS_SQL_         = "/sql";
  String _CACHE_REGION_SYS_AUTHORITY_   = "/auth";
  String _CACHE_REGION_RBAC_            = "/rbac";
  String _CACHE_REGION_PAGE_            = "/page";
  String _CACHE_REGION_LOGON_           = "/logon";
  String _CACHE_REGION_MDM_             = "/mdm";
  String _CACHE_REGION_TP_APP_          = "/app";
  String _CACHE_REGION_CONFIG_          = "/config";
  String _CACHE_REGION_JDBC_CONNECTION_ = "/jdbc";
  String _CACHE_REGION_TENANT_          = "/tenant";
  String _CACHE_REGION_SYS_CONFIG_      = "/sys-config";
  String _CACHE_REGION_SCHEDULE_        = "/sche";
  String _CACHE_REGION_BIZ_MODEL_       = "/biz-model";
  String _CACHE_REGION_DATASET_         = "/dataset";
  String _CACHE_REGION_SYSTEM_          = "/system";

  String _CACHE_KEY_READY_              = "/ready";
  String _CACHE_KEY_INIT_ORG_V_         = "/org-v";
  String _CACHE_KEY_INIT_ORG_           = "/org";

  String _ORGID_PLATFORM_               = IConstant.SYS_ORG;
  String _COUNT_SUFFIX_                 = "_count";
  String _COUNT_NAME_                   = "count";

}
