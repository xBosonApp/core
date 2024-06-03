////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月1日 上午11:57:34
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/j2ee/container/SessionCluster.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.container;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimerTask;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xboson.been.*;
import com.xboson.db.ConnectConfig;
import com.xboson.event.timer.EarlyMorning;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.service.OAuth2;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.AES;
import com.xboson.util.SessionID;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IOAuth2;


public class SessionCluster extends HttpFilter {

  private static final long serialVersionUID = -6654306025872001022L;
  private static final String COOKIE_NAME = "xBoson";

  private static byte[] sessionPassword = null;
  private static int sessionTimeout = 0; // 分钟

  private final Log log = LogFactory.create("session");
  private String contextPath;
  private ConnectConfig db;


  protected void doFilter(HttpServletRequest request,
                          HttpServletResponse response,
                          FilterChain chain)
          throws IOException, ServletException {
    SessionData sd = null;

    try {
      sd = fromToken(request, response);
      if (sd == null) {
        sd = fromCookie(request, response);
      }

      request.setAttribute(SessionData.ATTRNAME, sd);
      chain.doFilter(request, response);

    } catch (SQLException e) {
      throw new XBosonException.XSqlException(e);
    } finally {
      if (sd != null) {
        RedisMesmerizer.me().sleep(sd);
      }
    }
  }


  /**
   * 如果没有 token 参数则返回 null, 如果 token 无效会抛出异常.
   */
  private SessionData fromToken(HttpServletRequest request,
                                HttpServletResponse response)
          throws IOException, ServletException, SQLException
  {
    String token = request.getParameter(IOAuth2.PARM_TOKEN);
    if (Tool.isNulStr(token) || token.length() != IOAuth2.TOKEN_LENGTH) {
      return null;
    }

    SessionData sess = (SessionData)
            RedisMesmerizer.me().wake(SessionData.class, token);
    if (sess != null) {
      return sess;
    }

    AppToken at = OAuth2.openToken(token, db);
    if (at == null) {
      throw new XBosonException("invalid Token: "+ token, 21323);
    }

    LoginUser user = LoginUser.fromDb(at.userid, db);
    if (user == null) {
      throw new XBosonException("Token 对应的用户无法访问: "+ at.userid);
    }
    user.bindUserRoles(db);
    user.password = null;

    sess = new SessionData();
    sess.login_user  = user;
    sess.id          = token;
    sess.loginTime   = user.loginTime;
    log.debug("Token ID:", token);
    return sess;
  }


  /**
   * 总是会尽可能返回一个 SessionData, 在必要时会创建空的 SessionData
   */
  private SessionData fromCookie(HttpServletRequest request,
                                 HttpServletResponse response)
          throws IOException, ServletException
  {
    Cookie ck = SessionID.getCookie(COOKIE_NAME, request);
    SessionData sd = null;

    if (ck == null) {
      //
      // 第一次访问, 创建 cookie
      //
      ck = createCookie(response);
    } else {
      //
      // 验证 cookie 的值是否是平台生成
      //
      if (!SessionID.checkSessionId(sessionPassword, ck.getValue()) ) {
        //
        // 错误的 cookie 加密, 则生成全新的 cookie
        //
        log.debug("Bad Session id:", ck.getValue());
        ck = createCookie(response);
      } else {
        //
        // 尝试从 redis 还原数据
        //
        sd = resurrectionSession(ck);
        //
        // 超时则重建数据
        //
        if (sd != null && sd.isTimeout()) {
          ck = createCookie(response);
          sd = null;
        }
      }
    }

    if (sd == null) {
      sd = new SessionData(ck, sessionTimeout);
    }
    log.debug("ID:", ck.getValue());
    return sd;
  }


  /**
   * 从请求中还原 session
   */
  public static SessionData resurrectionSession(HttpServletRequest request) {
    Cookie ck = SessionID.getCookie(COOKIE_NAME, request);
    return resurrectionSession(ck);
  }


  /**
   * 从 cookie 中还原 session 数据
   */
  public static SessionData resurrectionSession(Cookie ck) {
    return (SessionData) RedisMesmerizer.me()
            .wake(SessionData.class, ck.getValue());
  }


  private Cookie createCookie(HttpServletResponse response)
          throws ServletException {
    Cookie ck = new Cookie(COOKIE_NAME,
            SessionID.generateSessionId(sessionPassword));
    ck.setHttpOnly(true);
    ck.setMaxAge(sessionTimeout * 60);
    ck.setPath(contextPath);
    response.addCookie(ck);
    return ck;
  }


  public void init(FilterConfig filterConfig) throws ServletException {
    Config cfg = SysConfig.me().readConfig();
    sessionTimeout  = cfg.sessionTimeout;
    sessionPassword = AES.aesKey( cfg.sessionPassword );
    contextPath     = filterConfig.getServletContext().getContextPath();
    db              = cfg.db;

    if (cfg.enableSessionClear) {
      SessionData sd = new SessionData();
      TimerTask clean = RedisMesmerizer.me().createCleanTask(sd);
      EarlyMorning.add(clean);
    }
  }
}
