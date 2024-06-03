////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-5-14 上午8:45
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/ContextHandle.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ContextHandle {

  public final List<String> handle;
  public final List<Thread> related;
  public final Thread main;


  public ContextHandle(Thread main) {
    if (main == null) {
      throw new NullPointerException("Thread main");
    }
    this.main    = main;
    this.handle  = Collections.synchronizedList(new ArrayList<>());
    this.related = Collections.synchronizedList(new ArrayList<>());
  }


  public void clear() {
    handle.clear();
    related.clear();
  }
}
