////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-5-22 上午9:34
// 原始文件路径: E:/xboson/xBoson/src/com/xboson/service/OlderApi.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.service;

import com.xboson.been.CallData;
import com.xboson.j2ee.container.XPath;
import com.xboson.j2ee.container.XService;


@XPath("/api")
public class OlderApi extends XService {

  @Override
  public void service(CallData data) throws Exception {
    throw new UnsupportedOperationException(
            "Service '/api' Already obsolete; Use "+ App.PATH_FOTMAT);
  }


  @Override
  public boolean needLogin() {
    return false;
  }
}
