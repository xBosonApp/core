////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-19 上午8:46
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/init/install/HttpData.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.init.install;

import com.xboson.been.Config;
import com.xboson.util.Tool;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;


public class HttpData {

  public final HttpServletRequest req;
  public final HttpServletResponse resp;
  public final ServletContext sc;
  public final Config cf;
  public boolean reset = false;
  public boolean ajax = false;

  /**
   * 设定返回给页面的消息
   */
  public String msg;


  public HttpData(HttpServletRequest req, HttpServletResponse resp, Config c) {
    this.req = req;
    this.resp = resp;
    this.sc = req.getServletContext();
    this.cf = c;
  }


  /**
   * 如果参数是空字符串或 null, 则返回 null.
   */
  public String getStr(String name) {
    String n = req.getParameter(name);
    if (Tool.isNulStr(n)) return null;
    return n;
  }


  /**
   * 获取 http 参数, 如果参数为空字符串返回 0
   */
  public int getInt(String name) {
    String n = req.getParameter(name);
    if (Tool.isNulStr(n)) return 0;
    return Integer.parseInt(n);
  }


  public boolean getBool(String name) {
    String b = req.getParameter(name);
    if (Tool.isNulStr(b)) return false;
    if ("1".equals(b)) return true;
    return Boolean.parseBoolean(b);
  }


  /**
   * 是目录返回 true, 否则返回 false 并在 msg 上绑定错误消息.
   */
  public boolean isDirectory(String path) {
    File f = new File(path);
    if (! f.exists()) {
      msg = "目录不存在: " + path;
      return false;
    }
    if (! f.isDirectory()) {
      msg = "不是目录: " + path;
      return false;
    }
    return true;
  }
}
