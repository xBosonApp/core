////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-20 下午4:24
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/log/writer/DbOut.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.log.writer;

import com.xboson.been.Config;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.event.GLHandle;
import com.xboson.event.GlobalEventBus;
import com.xboson.event.Names;
import com.xboson.log.*;
import com.xboson.log.slow.AbsSlowLog;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;

import javax.naming.event.NamingEvent;
import java.util.Arrays;
import java.util.Date;


/**
 * 将日志写出到数据库 `sys_pl_log_system` 表中.
 */
public class DbOut extends OutBase implements ILogWriter {

  private final static String ALERT       = "alert_log_system.sql";
  private final static String SQL         = "log_system.sql";
  private final static int    MAX_LOG_LEN = 2000;

  private ILogWriter log;
  private final Config cfg;


  public DbOut() {
    //
    // 在启动时先将日志数据写入内存缓冲区, 在 jdbc 连接池模块初始化完成后再转存入 db 中.
    //
    cfg = SysConfig.me().readConfig();
    log = new SavedOut();

    GlobalEventBus.me().on(Names.already_started, new GLHandle() {
      public void objectChanged(NamingEvent namingEvent) {
        init();
      }
    });
  }


  private void init() {
    try {
      try (SqlResult _ = SqlReader.query(ALERT, cfg.db)) {
      }

      ILogWriter sys_log = new SystemLog();
      log.destroy(sys_log);
      log = sys_log;
    } catch(Exception e) {
      OutBase.nolog(this +"FAIL "+ e.getMessage());
    }
  }


  @Override
  public void output(Date d, Level l, String name, Object[] msg) {
    log.output(d, l, name, msg);
  }


  @Override
  public void destroy(ILogWriter replace) {
    log.destroy(replace);
  }


  private class SystemLog extends AbsSlowLog implements IConstant, ILogWriter {

    private String nodeID;


    private SystemLog() {
      nodeID = cfg.clusterNodeID +"";
    }


    @Override
    protected String getSql() {
      return SqlReader.read(SQL);
    }


    public void output(Date d, Level l, String name, Object[] msg) {
      try {
        StringBuilder buf = new StringBuilder(2000);
        for (int i = 0; i < msg.length; ++i) {
          buf.append(' ');
          buf.append(msg[i]);
          if (buf.length() > MAX_LOG_LEN) break;
        }
        if (buf.length() > MAX_LOG_LEN) {
          buf.setLength(2000-3);
          buf.append("...");
        }

        insert(
          /* 1 */ Tool.nextId() +"",
          /* 2 */ d,
          /* 3 */ l.toString(),
          /* 4 */ name,
          /* 5 */ NULL_STR,
          /* 6 */ nodeID,
          /* 7 */ buf.toString(),
          /* 8 */ new Date()
        );
      } catch(Exception e) {
        OutBase.nolog(this +" FAIL "+ e.getMessage());
      }
    }


    @Override
    public void destroy(ILogWriter replace) {
      destroy();
    }


    /**
     * 重写默认行为, 防止自身的日志输出嵌套自身死循环.
     */
    @Override
    protected Log createLog() {
      return new NulLog("system-log");
    }
  }

}
