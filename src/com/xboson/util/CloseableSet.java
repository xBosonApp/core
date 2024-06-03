////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-13 下午5:49
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/CloseableSet.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.log.Log;
import com.xboson.log.LogFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * 维护多个可关闭对象, 在 close() 时全部被关闭,
 * 即使有部分对象抛出异常, 也尽可能的调用全部对象的 close().
 */
public class CloseableSet implements AutoCloseable {

  private List<AutoCloseable> list;
  private Log log;


  public CloseableSet() {
    list = new ArrayList<>();
    log = LogFactory.create();
  }


  public<T extends AutoCloseable> T add(T c) {
    list.add(c);
    return c;
  }


  @Override
  public void close() {
    for (int i=list.size()-1; i>=0; --i) {
      AutoCloseable c = list.get(i);
      try {
        c.close();
      } catch (Throwable t) {
        log.error("Close object", c, t.toString());
      }
    }
    list = null;
  }
}
