////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-4 上午8:33
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/ApiCall.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;


/**
 * 封装对 api 的调用, 参数小写化.
 */
public class ApiCall implements IBean {

  /** 必须, HTTP 请求参数 */
  public CallData call;

  /** 必须, 机构 id, 该参数可能在运行时被替换, exparam 始终保存请求值 */
  public String org;

  /** 必须, 应用 id */
  public String app;

  /** 必须, 模块 id */
  public String mod;

  /** 必须, 接口 id */
  public String api;

  /** 扩展请求参数, 优先级高于 http 参数, 可以 null */
  public Map<String, Object> exparam;


  /**
   * 分析 url 参数, 并将请求映射到 api 上, 返回的对象中 call 属性为 null.
   * @param url 该参数是安全的, 不会被改变.
   */
  public ApiCall(UrlSplit url) {
    UrlSplit sp = url.clone();
    sp.withoutSlash(true);

    this.org = sp.next().toLowerCase();
    this.app = sp.next().toLowerCase();
    this.mod = sp.next().toLowerCase();
    this.api = sp.next().toLowerCase();
  }


  /**
   * 参数都被转换为小写.
   */
  public ApiCall(String org, String app, String mod, String api) {
    this.org = toLower(org);
    this.app = toLower(app);
    this.mod = toLower(mod);
    this.api = toLower(api);
  }


  private String toLower(String s) {
    return s == null ? null : s.toLowerCase();
  }


  /**
   * <b>谨慎调用 !!</b><br/>
   * 线程被 kill, 不能正常应答(抛异常或发送错误消息都不可用), 这里发送最后一条消息,
   * 防止浏览器不停的请求这个没有应答的 api.
   */
  public void makeLastMessage(String msg) {
    try {
      PrintWriter out = call.resp.getWriter();
      out.write('"');
      out.write(msg);
      out.write('"');
      out.flush();
    } catch (IOException e) {
      throw new XBosonException.IOError(e);
    }
  }
}
