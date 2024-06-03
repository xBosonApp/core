////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午5:19:09
// 原始文件路径: xBoson/src/com/xboson/log/LogFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.log;

import com.xboson.been.Config;
import com.xboson.been.XBosonException;
import com.xboson.event.OnExitHandle;
import com.xboson.log.writer.ConsoleOut;
import com.xboson.log.writer.LogWriterLoader;
import com.xboson.log.writer.SavedOut;
import com.xboson.util.SysConfig;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


public class LogFactory extends OnExitHandle {

  public static final String FILE = "log.level.properties";

  private static LogFactory instance;
  private static Level level;
  private static ILogWriter writer;
  private static Properties config = new Properties();
  private String filepath;


  static {
    setLevel(Level.ALL);
    /** 仅在启动期间保持日志, 启动后立即切换 */
    writer = new SavedOut();
  }


  public synchronized static LogFactory me() {
    if (instance == null) {
      synchronized (LogFactory.class) {
        if (instance == null) {
          instance = new LogFactory();
        }
      }
    }
    return instance;
  }


  private LogFactory() {
    try {
      Config cfg = SysConfig.me().readConfig();
      setWriter(LogWriterLoader.getLogWriter(cfg));
      setLevel(Level.find(cfg.logLevel));

      filepath = cfg.configPath + "/" + FILE;
      try (InputStream in = new FileInputStream(filepath)) {
        config.load(in);
      }

      create().info("Initialization Success, Log level", level);
    } catch(Exception e) {
      System.err.println("LogFactory::INIT " + e);
    } finally {
      if (writer == null) {
        writer = new ConsoleOut();
        nolog("::Use Console Log Writer");
      }
    }
  }


  @Override
  protected void exit() {
    if (config != null) {
      try (OutputStream out = new FileOutputStream(filepath)) {
        config.store(out, "LogFactory Config From xBoson.");
      } catch(Exception e) {
        nolog("::LogFactory::EXIT " + e);
      }
    }

    if (writer != null) {
      writer.destroy(null);
      // 保证在退出后仍然可用
      writer = new ConsoleOut();
    }
  }


  /**
   * 调用该方法, 通知工厂 Log 的级别被改变, 工厂会对改变做记录
   * @param log 级别被改变的实例
   */
  static void changeLevel(Log log) {
    synchronized (config) {
      config.setProperty(log.getName(), log.getLevel().getName());
    }
  }


  /**
   * 创建日志实例, 用于输出日志.
   */
  public static Log create(String name) {
    if (name == null)
      throw new XBosonException.NullParamException("String name");

    Log log = new Log(name);
    synchronized (config) {
      String levelname = config.getProperty(name);
      if (levelname != null) {
        log.setLevel(Level.find(levelname));
      } else {
        changeLevel(log);
      }
    }
    return log;
  }


  public static Log create(Class c, String name) {
    return create(c.getName() + "::" + name);
  }


  /**
   * 创建日志实例, 用于输出日志, 类名作为日志名.
   * @see #create(String)
   */
  public static Log create(Class<?> c) {
    return create(c.getName());
  }


  /**
   * 使用调用该方法的类名作为日志名, 在集成系统中, 始终使用父类的名称.
   * @see #create(String)
   */
  public static Log create() {
    Exception e = new Exception();
    StackTraceElement[] t = e.getStackTrace();
    return create(t[1].getClassName());
  }


  public static Log create(ILogName ln) {
    return create(ln.logName());
  }


  /**
   * 设置当前日志级别
   */
  public static void setLevel(Level l) {
    if (l == Level.INHERIT) {
      l = Level.ALL;
      nolog("::Global level can not be set to inherit");
    }
    l.checknull();
    level = l;
  }


  static ILogWriter getLogWriter() {
    return writer;
  }


  static boolean blocking(Level l) {
    return level.blocking(l);
  }


  /**
   * 设置全局日志输出器
   */
  public synchronized void setWriter(ILogWriter new_writer) {
    ILogWriter older = writer;
    writer = new_writer;
    older.destroy(writer);
    nolog("::Change Log Writer " + new_writer.getClass());
  }


  public static void nolog(String msg) {
    OutBase.nolog(msg);
  }
}
