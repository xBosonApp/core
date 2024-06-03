////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-5-19 下午4:27
// 原始文件路径: E:/xboson/xBoson/src/com/xboson/j2ee/emu/EmuJeeContext.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.emu;

import com.xboson.app.ApiPath;
import com.xboson.app.AppContext;
import com.xboson.been.*;
import com.xboson.init.Startup;
import com.xboson.j2ee.container.Striker;
import com.xboson.j2ee.container.XPath;
import com.xboson.log.slow.RequestApiLog;
import com.xboson.service.App;
import com.xboson.util.SysConfig;
import com.xboson.util.Version;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 模拟 JEE 的完整环境, 使 AppContext 和相关组件可以正确运行
 */
public class EmuJeeContext {

  private final String appPrefix;
  private final String userAgent;
  private LoginUser user;


  /**
   * 默认使用超级管理员运行 appContext
   */
  public EmuJeeContext() {
    XPath x = App.class.getAnnotation(XPath.class);
    this.appPrefix = x.value();
    this.user      = new Admin();
    this.userAgent = "xBoson v"+ Version.xBoson +" internal call";
  }


  /**
   * 调用指定的 api, 完成后返回.
   */
  public void callApi(String org, String app, String mod, String api) {
    jeeContext((req0, resp0) -> {
      HttpServletRequest req = (HttpServletRequest) req0;
      HttpServletResponse resp = (HttpServletResponse) resp0;

      ApiCall ac = new ApiCall(org, app, mod, api);
      ac.call = new CallData(req, resp);
      AppContext appCtx = AppContext.me();

      if (req0 instanceof EmuServletRequest) {
        EmuServletRequest esr = (EmuServletRequest) req0;
        esr.requestUriWithoutContext = appPrefix + ApiPath.getPath(ac);
        esr.setHeader(RequestApiLog.HEADER_USER_AGENT, userAgent);
      }

      appCtx.call(ac);
      resp.flushBuffer();
    });
  }


  /**
   * 进入 AppContext 中执行 inApp->Run 中的代码;
   * 当授权正确, 该环境可以正确运行受限的代码.
   * 继承自 jee 上下文.
   */
  public void appContext(Runnable inApp) {
    jeeContext((req0, resp0) -> {
      HttpServletRequest req = (HttpServletRequest) req0;
      HttpServletResponse resp = (HttpServletResponse) resp0;

      ApiCall ac = new ApiCall("org", "app", "mod", "api");
      ac.call = new CallData(req, resp);
      AppContext appCtx = AppContext.me();

      appCtx.call(ac, inApp);
    });
  }


  /**
   * 在 JEE 模拟环境中运行一个 servlet 链实例,
   * 该上下文已经有一个登录的用户和相关的 servlet 对象.
   */
  public void jeeContext(FilterChain chain) {
    Striker st = new Striker();
    EmuServletRequest req   = new EmuServletRequest();
    EmuServletResponse resp = new EmuServletResponse();
    ServletContext sc       = Startup.getServletContext();
    EmuFilterConfig fconf   = new EmuFilterConfig();

    if (sc == null) {
      sc = new EmuServletContext();
    }

    fconf.servlet_context = sc;
    req.servlet_context   = sc;

    try {
      SessionData sd = new SessionData();
      req.setAttribute(SessionData.ATTRNAME, sd);
      sd.login_user = user;

      st.init(fconf);
      st.doFilter(req, resp, chain);

    } catch(XBosonException e) {
      throw e;
    } catch(Exception e) {
      throw new XBosonException(e);
    }
  }


  /**
   * 指定上下文的登录用户
   */
  public void setUser(LoginUser user) {
    this.user = user;
  }


  private class Admin extends LoginUser {

    public Admin() {
      super.userid = SysConfig.me().readConfig().rootUserName;
    }

    public boolean isRoot() {
      return true;
    }
  }
}
