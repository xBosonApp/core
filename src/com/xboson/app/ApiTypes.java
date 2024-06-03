////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-15 下午6:45
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/ApiTypes.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app;


import com.xboson.been.ApiCall;


public enum ApiTypes {

  Production("/API/prod", "r"),
  Development("/API/dev", "d");


  /**
   * 与 full 相同 (!拼写错误)
   */
  public final String eventPrifix;

  /**
   * 一个字符的短描述
   */
  public final String flag;

  /**
   * 应用类型完整描述
   */
  public final String full;


  ApiTypes(String eventPrefix, String flag) {
    this.full = eventPrefix;
    this.eventPrifix = eventPrefix;
    this.flag = flag;
  }


  /**
   * 's' 调试状态标记
   *    d：执行最新代码并返回调试信息，
   *    r：执行已发布代码
   */
  public static ApiTypes of(ApiCall ac) {
    if (Development.flag.equalsIgnoreCase(ac.call.req.getParameter("s"))) {
      return Development;
    }
    return Production;
  }


  public String toString() {
    return full;
  }
}
