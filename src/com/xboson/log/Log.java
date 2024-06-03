////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午5:18:45
// 原始文件路径: xBoson/src/com/xboson/log/Log.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.log;

import java.util.Date;

/**
 * 推荐使用非 static 变量存储 Log
 */
public class Log {

  /**
   * 如果是继承的, 则使用全局级别, 否则使用日志自己的级别
   */
  private Level cl = Level.INHERIT;
  private String name;


  Log(String name) {
    this.name = name;
  }


  /**
   * 改变当前实例的日志级别
   */
  public final void setLevel(final Level l) {
    l.checknull();
    cl = l;
    LogFactory.changeLevel(this);
  }


  public void logs(final Level l, Object[] msg) {
    if (blocking(l)) {
      return;
    }

    Date d = new Date();
    //
    // 为保证性能不在这里做同步
    //
    LogFactory.getLogWriter().output(d, l, name, msg);
  }


  Level getLevel() {
    return cl;
  }


  String getName() {
    return name;
  }


  /**
   * 指定的日志级别不需要打印(执行)返回 true;
   * 该方法用于优化复杂的日志参数在无需打印时不再执行初始化操作.
   */
  public final boolean blocking(Level l) {
    if (cl == Level.INHERIT) {
      if (LogFactory.blocking(l))
        return true;
    } else if (cl.blocking(l)) {
      return true;
    }
    return false;
  }


  public final void log(Level l, Object... msg) {
    logs(l, msg);
  }

  public final void info(Object... msg) {
    logs(Level.INFO, msg);
  }

  public final void debug(Object... msg) {
    logs(Level.DEBUG, msg);
  }

  public final void error(Object... msg) {
    logs(Level.ERR, msg);
  }

  public final void warn(Object... msg) {
    logs(Level.WARN, msg);
  }

  public final void fatal(Object... msg) {
    logs(Level.FATAL, msg);
  }
}
