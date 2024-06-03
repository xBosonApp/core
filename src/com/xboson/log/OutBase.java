////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月3日 下午4:14:35
// 原始文件路径: xBoson/src/com/xboson/log/OutBase.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.log;

import java.io.IOException;
import java.util.Date;

import com.xboson.been.XBosonException;
import com.xboson.util.Tool;

public abstract class OutBase implements ILogWriter {

  /**
   * 格式化数据到 add 对象, 末尾无换行
   */
  public void format(Appendable add, Date d, Level l, String name, Object[] msg) {
    try {
      add.append(Tool.formatDate(d));
      add.append(" [");
      add.append(l.toString());
      add.append("] [");
      add.append(name);
      add.append("]");

      for (int i = 0; i < msg.length; ++i) {
        add.append(' ');
        add.append("" + msg[i]);
      }
    } catch (IOException e) {
      throw new XBosonException.IOError(e);
    }
  }


  public static void nolog(String msg) {
    System.out.println("::NO-LOG> " + msg);
  }
}
