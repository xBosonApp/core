////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改, 
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-3-14 上午9:24
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/c0nst/IOAuth2.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util.c0nst;

public interface IOAuth2 extends IConstant {

  String URL_FAIL_MSG = "Provide sub-service name '/oauth2/[sub service]'";

  String PageBase       = "/xboson/face/ui/paas/oauth2/";
  String PAGE_LOGIN     = "login.html";
  String PAGE_BAD_TYPE  = "badtype.html";
  String PAGE_BLOCK     = "block.html";
  String PAGE_ACCESS    = "access.html";
  String PAGE_BAD_PARM  = "badp.html";

  String SQL_GET_APP    = "open_tp_app.sql";
  String SQL_GET_APP_PS = "open_tp_app_ps.sql";
  String SQL_DEL_TOKEN  = "delete_app_token.sql";
  String SQL_NEW_TOKEN  = "create_app_token.sql";
  String SQL_GET_TOKEN  = "open_app_token.sql";

  String GTYPE_AUTH_CODE = "authorization_code";

  String PARM_CLI_ID = "client_id";
  String PARM_CLI_PS = "client_secret";
  String PARM_TOKEN  = "access_token";
  String PARM_CODE   = "code";
  String PARM_GTYPE  = "grant_type";
  String PARM_STATE  = "state";

  String ERR_UNSUPPORT_GTYPE = "unsupported_grant_type";
  String ERR_INV_GRANT       = "invalid_grant";
  String ERR_INV_CLIENT      = "invalid_client";
  String ERR_INV_REQ_PARM    = "invalid_request";
  String ERR_TOKEN_EXP       = "expired_token";

  int CODE_LENGTH  = 40;
  int TOKEN_LENGTH = 90;

  /** 授权码有效期, 10 分钟, 单位毫秒 */
  int CODE_LIFE = 10 * 60 * 1000;

  /** 令牌有效期, 90 天, 单位秒 */
  int TOKEN_LIFE = 90 * 24 * 60 * 60;

}
