////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午2:43:15
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/SessionData.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import com.xboson.sleep.IBinData;
import com.xboson.sleep.ITimeout;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


/**
 * Session 不是动态数据, 属性都是固定的,
 * 这与 servlet 中的 session 是分离的两套系统, xBoson 不使用 servlet.session.
 */
public class SessionData implements IBean, IBinData, ITimeout {

  public static final String ATTRNAME = "xBoson-session-data";

  public LoginUser login_user;
  public String id;
  public String captchaCode;

  public long loginTime;
  public long endTime;


  public SessionData() {
  }


  /**
   * 使用 token 创建 session
   */
  public SessionData(AppToken token) {
    this.id = token.token;
    this.loginTime = System.currentTimeMillis();
    this.endTime = token.over;
  }


  /**
   * 使用 cookie 创建 session
   */
  public SessionData(Cookie ck, int sessionTimeoutMinute) {
    this.id = ck.getValue();
    this.loginTime = System.currentTimeMillis();
    this.endTime = this.loginTime + sessionTimeoutMinute * 60 * 1000;
  }


  public boolean isTimeout() {
    return endTime - System.currentTimeMillis() < 0;
  }


  public static SessionData get(HttpServletRequest request) throws ServletException {
    SessionData sd = (SessionData) request.getAttribute(ATTRNAME);
    if (sd == null) {
      throw new ServletException("SessionData not init");
    }
    return sd;
  }


  @Override
  public String getid() {
    return id;
  }


  /**
   * 标记为销毁状态
   */
  public void destoryFlag() {
    endTime = 0;
  }
}
