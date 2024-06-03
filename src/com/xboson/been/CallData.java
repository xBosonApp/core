////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午3:42:10
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/CallData.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xboson.j2ee.container.XResponse;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;


/**
 * http 请求数据的包装.
 * 全部属性公共不可变; 提供参数获取的便捷方法并检查格式.
 */
public class CallData implements IBean {

  private final static String remoteIpHeader;

  public final HttpServletRequest req;
  public final HttpServletResponse resp;
  public final UrlSplit url;
  public final XResponse xres;
  public final SessionData sess;

  static {
    String h = SysConfig.me().readConfig().remoteIpHeader;
    if (Tool.isNulStr(h)) {
      remoteIpHeader = null;
    } else {
      remoteIpHeader = h;
    }
  }


  /**
   * 从 http 请求中创建
   */
  public CallData(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException {
    this.req  = req;
    this.resp = resp;
    this.xres = XResponse.get(req);
    this.sess = SessionData.get(req);
    this.url  = new UrlSplit(req);
  }


  /**
   * 复制一个 CallData 并定制 XResponse 对象
   *
   * @param cd 复制来源
   * @param xr 主要目的是定制一个 XResponse 对象
   * @throws ServletException
   */
  public CallData(CallData cd, XResponse xr)
          throws ServletException {
    this.req  = cd.req;
    this.resp = cd.resp;
    this.xres = xr;
    this.sess = cd.sess;
    this.url  = cd.url;
  }


  /**
   * 获取 http 参数的方法, 当参数不在或不符合条件时抛出 msg 指定的异常,
   * 在计算长度之前, 会删除参数的首尾空格.
   *
   * @param name 参数名
   * @param min 最小长度, >= min, 如果 min == 0 则允许参数不存在.
   * @param max 最大长度, < max
   * @return 返回这个参数
   */
  public String getString(String name, int min, int max) {
    String v = req.getParameter(name);
    if (v == null) {
      if (min <= 0) {
        return v;
      } else {
        throw new XBosonException.BadParameter(
                name, "Can not be null");
      }
    }

    v = v.trim();
    final int len = v.length();
    if (len < min) {
      throw new XBosonException.BadParameter(
              name,"最小长度 " + min);
    }
    if (len > max) {
      throw new XBosonException.BadParameter(
              name,"最大长度 " + max);
    }
    return v;
  }


  public Integer getInt(String name, int min, int max) {
    String v = req.getParameter(name);
    if (v == null) {
      if (min == 0)
        return null;

      throw new XBosonException.BadParameter(
              name, "Can not be null");
    }

    Integer i = Integer.parseInt(v);
    if (i < min) {
      throw new XBosonException.BadParameter(
              name,"最小值 " + min);
    }
    if (i > max) {
      throw new XBosonException.BadParameter(
              name,"最大值 " + max);
    }
    return i;
  }


  public String getRemoteAddr() {
    String addr = null;
    if (remoteIpHeader != null) {
      addr = req.getHeader(remoteIpHeader);
    }
    if (addr == null) {
      addr = req.getRemoteAddr();
    }
    return addr;
  }
}
