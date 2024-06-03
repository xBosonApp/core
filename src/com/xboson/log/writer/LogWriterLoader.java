////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-5-26 下午8:31
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/log/writer/LogWriterLoader.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.log.writer;

import com.xboson.been.Config;
import com.xboson.log.ILogWriter;
import com.xboson.log.Level;
import com.xboson.log.OutBase;
import com.xboson.util.Tool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 该输出类会解析配置文件, 并设置输出,
 */
public class LogWriterLoader extends OutBase implements ILogWriter {

  private ILogWriter[] outArr;


  /**
   * 解析配置文件设置日志输出, 支持将多个输出器组合.
   */
  public static ILogWriter getLogWriter(Config cfg) {
    List<ILogWriter> out = new ArrayList<>();
    String types = cfg.loggerWriterType;

    if (Tool.isNulStr(types)) {
      return new ConsoleOut();
    }

    String[] ts = types.split(",");
    for (String t : ts) {
      if (! Tool.isNulStr(t)) {
        try {
          Class<?> cl = Class.forName("com.xboson.log.writer." + t.trim());
          ILogWriter lw = (ILogWriter) cl.newInstance();
          out.add(lw);
          nolog("Load log writer: "+ t);
        } catch (Exception e) {
          nolog("Load log writer '"+ t +"' Get fail: "+ e.getMessage());
        }
      }
    }

    if (out.size() < 1) {
      return new ConsoleOut();
    }

    return new LogWriterLoader(out);
  }


  private LogWriterLoader(List<ILogWriter> out) {
    this.outArr = out.toArray(new ILogWriter[out.size()]);
  }


  @Override
  public void output(Date d, Level l, String name, Object[] msg) {
    for (int i=0; i<outArr.length; ++i) {
      outArr[i].output(d, l, name, msg);
    }
  }


  @Override
  public void destroy(ILogWriter replace) {
    for (int i=0; i<outArr.length; ++i) {
      outArr[i].destroy(replace);
    }
  }
}
