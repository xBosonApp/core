////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 下午6:07
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/j2ee/container/ResponseTypes.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.resp;

import com.xboson.been.XBosonException;
import com.xboson.j2ee.container.IXResponse;

import java.util.HashMap;
import java.util.Map;


/**
 * 所有应答方式
 */
public final class ResponseTypes {

  private ResponseTypes() {}

  private static final Map<String, IXResponse> types = new HashMap<>();
  private static final String default_type = "json";

  /**
   * 支持列表
   */
  static {
    types.put("json",  new JsonResponse());
    types.put("xml",   new XmlResponse());
    types.put("jsonp", new JsonPaddingResp());
  }


  /**
   * 返回指定的应答方式, 无效的名称会抛出异常
   */
  public static IXResponse get(String name) {
    IXResponse xr = types.get(name);
    if (xr == null) {
      throw new XBosonException("response type not exist " + name);
    }
    return xr;
  }


  /**
   * 返回默认应答方式
   */
  public static IXResponse get() {
    return get(default_type);
  }
}
