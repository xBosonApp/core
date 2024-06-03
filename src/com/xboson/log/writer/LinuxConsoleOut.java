////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-27 上午9:17
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/log/writer/LinuxConsoleOut.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.log.writer;

import com.xboson.log.ILogWriter;
import com.xboson.log.Level;
import com.xboson.log.OutBase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class LinuxConsoleOut extends OutBase implements ILogWriter {

  private Map<Level, String> colors = new HashMap<>();
  private String END_COLOR;


  public LinuxConsoleOut() {
    colors.put(Level.DEBUG, "\u001b[;37m");
    colors.put(Level.INFO, "\u001b[;39m");
    colors.put(Level.WARN, "\u001b[;33m");
    colors.put(Level.ERR, "\u001b[;31m");
    colors.put(Level.FATAL, "\u001b[;34m");
    END_COLOR = "\u001b[m";
  }


  @Override
  public void output(Date d, Level l, String name, Object[] msg) {
    StringBuilder buf = new StringBuilder();
    format(buf, d, l, name, msg);

    String color = colors.get(l);
    if (color == null) {
      System.out.println(buf.toString());
    } else {
      System.out.println(color + buf.toString() + END_COLOR);
    }
  }


  @Override
  public void destroy(ILogWriter replace) {
  }
}
