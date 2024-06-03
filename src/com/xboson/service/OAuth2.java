////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-3-13 上午11:55
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/service/OAuth2.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.service;

import com.xboson.been.*;
import com.xboson.db.ConnectConfig;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.j2ee.container.XPath;
import com.xboson.j2ee.container.XService;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IOAuth2;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


@XPath("/oauth2")
public class OAuth2 extends XService implements IOAuth2 {


  private Config cf;


  public OAuth2() {
    cf = SysConfig.me().readConfig();
  }


  @Override
  public void service(CallData data) throws Exception {
    subService(data, URL_FAIL_MSG);
  }


  @Override
  public boolean needLogin() {
    return false;
  }


  private boolean isLogin(CallData data) {
    return data.sess.login_user != null
            && data.sess.login_user.userid != null;
  }


  /**
   * 获取授权码
   */
  public void authorize(CallData data) throws Exception {
    try {
      if (! isLogin(data)) {
        goPage(data, PAGE_LOGIN);
        return;
      }
      String type = data.getString(PARM_GTYPE, 1, 30);
      String cid  = data.getString(PARM_CLI_ID, 1, 99);
      String stat = data.req.getParameter(PARM_STATE);

      if (! GTYPE_AUTH_CODE.equals(type)) {
        goPage(data, PAGE_BAD_TYPE);
        return;
      }

      AppInfo app = searchApp(cid);
      if (app == null || app.uri == null) {
        goPage(data, PAGE_BLOCK);
        return;
      }

      String code = Tool.randomString2(CODE_LENGTH);
      OAuth2Code ocode = new OAuth2Code(code);
      ocode.clientid   = cid;
      ocode.begin      = System.currentTimeMillis();
      ocode.userid     = data.sess.login_user.userid;
      RedisMesmerizer.me().sleep(ocode);

      goPage(data, PAGE_ACCESS,
              "code", code, "uri", app.uri, "state", stat,
              "appnm", app.name, "usernm", data.sess.login_user.userid);

    } catch(XBosonException.BadParameter e) {
      goPage(data, PAGE_BAD_PARM);
    } catch(Exception e) {
      data.resp.getWriter().write("Error: " + e.getMessage());
    }
  }


  /**
   * 用授权码交换令牌
   */
  public void access_token(CallData data) throws Exception {
    try {
      String type = data.getString(PARM_GTYPE, 1, 30);
      String cid  = data.getString(PARM_CLI_ID, 1, 99);
      String ps   = data.getString(PARM_CLI_PS, 1, 99);
      String code = data.getString(PARM_CODE, 1, CODE_LENGTH+1);

      if (! GTYPE_AUTH_CODE.equals(type)) {
        error(data, 21328, ERR_UNSUPPORT_GTYPE);
        return;
      }

      OAuth2Code ocode = (OAuth2Code) 
              RedisMesmerizer.me().wake(OAuth2Code.class, code);
      if (ocode == null) {
        error(data, 21325, ERR_INV_GRANT);
        return;
      } else {
        RedisMesmerizer.me().remove(ocode);
      }

      if (! cid.equals(ocode.clientid)) {
        error(data, 21324, ERR_INV_CLIENT);
        return;
      }

      if (null == searchApp(cid, ps)) {
        error(data, 21324, ERR_INV_CLIENT);
        return;
      }

      Timestamp birth = new Timestamp(System.currentTimeMillis());
      AppToken at  = new AppToken(birth, TOKEN_LIFE);
      at.clientid  = ocode.clientid;
      at.token     = Tool.randomString2(TOKEN_LENGTH);
      at.userid    = ocode.userid;

      if (! saveTokenToDB(at, birth)) {
        error(data, 21324, ERR_INV_CLIENT);
      }
      RedisMesmerizer.me().sleep(at);

      data.xres.bindResponse("access_token", at.token);
      data.xres.bindResponse("expires_in", TOKEN_LIFE);
      data.xres.bindResponse("userid", ocode.userid);
      data.xres.responseMsg("ok", 0);

    } catch(XBosonException.BadParameter e) {
      error(data, 21323, ERR_INV_REQ_PARM, e.getMessage());
    }
  }


  /**
   * 撤销令牌
   */
  public void revoke_token(CallData data) throws Exception {
    try {
      String cid   = data.getString(PARM_CLI_ID, 1, 99);
      String ps    = data.getString(PARM_CLI_PS, 1, 99);
      String token = data.getString(PARM_TOKEN, 1, TOKEN_LENGTH+1);

      if (null == searchApp(cid, ps)) {
        error(data, 21324, ERR_INV_CLIENT);
        return;
      }

      if (! deleteTokenDB(cid, token)) {
        error(data, 21327, ERR_TOKEN_EXP);
        return;
      }

      AppToken at = new AppToken();
      at.token = token;
      RedisMesmerizer.me().remove(at);

      data.xres.responseMsg("ok", 0);
    } catch(XBosonException.BadParameter e) {
      error(data, 21323, ERR_INV_REQ_PARM, e.getMessage());
    }
  }


  private boolean deleteTokenDB(String cid, String token) {
    try (SqlResult sr = SqlReader.query(SQL_DEL_TOKEN, cf.db, token, cid)) {
      return sr.getUpdateCount() == 1;
    }
  }


  /**
   * 该方法尝试从缓存中获取 token 对象, 或从数据库中获取 token 对象 (成功后缓存).
   * token 不存在会返回 null.
   * @throws XBosonException.TokenTimeout 令牌超时
   */
  public static AppToken openToken(String token, ConnectConfig db)
          throws SQLException
  {
    if (Tool.isNulStr(token))
      throw new NullPointerException("token cannot be null");

    AppToken at = (AppToken) RedisMesmerizer.me().wake(AppToken.class, token);
    if (at == null) {
      try (SqlResult sr = SqlReader.query(SQL_GET_TOKEN, db, token)) {
        ResultSet rs = sr.getResult();
        if (! rs.next()) return null;

        at = new AppToken(rs.getTimestamp("birth_time"),
                          rs.getInt("expires_in"));
        at.clientid  = rs.getString("client_id");
        at.userid    = rs.getString("userid");
        at.token     = token;

        if (at.isTimeout()) {
          throw new XBosonException.TokenTimeout();
        }
        RedisMesmerizer.me().sleep(at);
      }
    }
    return at;
  }


  private AppInfo searchApp(String clientId, String clientPs)
          throws SQLException {
    return _searchApp(SQL_GET_APP_PS, clientId, clientPs);
  }


  private AppInfo searchApp(String clientId)
          throws SQLException {
    return _searchApp(SQL_GET_APP, clientId);
  }


  private AppInfo _searchApp(String sqlFile, Object... parm)
          throws SQLException {
    try (SqlResult sr = SqlReader.query(sqlFile, cf.db, parm)) {
      ResultSet rs = sr.getResult();
      if (rs.next()) {
        if (rs.getInt("status") > 0) {
          return new AppInfo(
                  rs.getString("tp_appnm"), 
                  rs.getString("uri"));
        }
      }
    }
    return null;
  }


  private boolean saveTokenToDB(AppToken at, Timestamp birth) {
    try (SqlResult sr = SqlReader.query(SQL_NEW_TOKEN, cf.db,
            at.clientid, at.token, at.userid, birth, TOKEN_LIFE, 1)) {
      return sr.getUpdateCount() == 1;
    }
  }


  /**
   * 跳转到页面并绑定请求参数
   * @param data
   * @param page 页面名(以 PageBase 为基础路径)
   * @param p 绑定参数 [参数名1, 参数值1, 参数名2, ....]
   * @throws Exception
   */
  private void goPage(CallData data, String page, Object...p) throws Exception {
    StringBuilder uri = new StringBuilder();
    uri.append(PageBase);
    uri.append(page);

    if (p != null && p.length > 0) {
      uri.append("?");
      for (int i=0; i<p.length; i+=2) {
        uri.append(p[i]);
        uri.append('=');
        if (p[i+1] != null) {
          uri.append(java.net.URLEncoder.encode(
                  String.valueOf(p[i+1]), CHARSET_NAME));
        }
        uri.append('&');
      }
    }
    data.resp.sendRedirect(uri.toString());
  }


  private void error(CallData data, int code, String err) throws IOException {
    error(data, code, err, err);
  }


  /**
   * 返回 json 格式的 oauth2 应答.
   * @param data
   * @param code oauth2 代码
   * @param err oauth2 错误字符串
   * @param msg 自定义消息
   * @throws IOException
   */
  private void error(CallData data, int code, String err, String msg)
          throws IOException {
    data.xres.bindResponse("error", err);
    data.xres.bindResponse("error_code", code);
    data.xres.responseMsg(msg, code);
  }
  
  
  private class AppInfo {
    String uri;
    String name;

    AppInfo(String name, String uri) {
      this.name = name;
      this.uri = uri;
    }
  }
}
