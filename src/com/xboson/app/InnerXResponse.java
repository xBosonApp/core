////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-11 下午3:54
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/InnerXResponse.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app;

import com.xboson.j2ee.container.XResponse;

import java.io.IOException;
import java.util.Map;


/**
 * 内部应答对象, 对应答的调用不做任何动作, 不会真的给客户端应答,
 * 仅保存应答数据, 并通过方法返回它们.
 */
public class InnerXResponse extends XResponse {

  private boolean isResponsed;
  private Map<String, Object> root;


  public InnerXResponse(Map<String, Object> root) {
    super(root);
    this.root = root;
  }


  @Override
  public void response() throws IOException {
    isResponsed = true;
  }


  @Override
  public boolean isResponsed() {
    return isResponsed;
  }


  public Map<String, Object> getResponseRoot() {
    return root;
  }


  @Override
  public void setCode(int code) {
    super.setCode(code);
    root.put("ret", Integer.toString(code));
  }
}
