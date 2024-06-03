////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-11 下午2:41
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/init/Touch.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.init;

import com.xboson.app.SystemStartupScript;
import com.xboson.auth.AuthFactory;
import com.xboson.chain.PeerFactory;
import com.xboson.db.DbmsFactory;
import com.xboson.db.sql.SqlReader;
import com.xboson.distributed.XLock;
import com.xboson.event.GlobalEventBus;
import com.xboson.event.Names;
import com.xboson.fs.script.FileSystemFactory;
import com.xboson.j2ee.container.Processes;
import com.xboson.j2ee.container.UrlMapping;
import com.xboson.log.LogFactory;
import com.xboson.rpc.ClusterManager;
import com.xboson.rpc.RpcFactory;
import com.xboson.script.SandboxFactory;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.SysConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Date;


/**
 * 只要触摸过, 系统就能初始化 !
 */
public final class Touch {


/****************************************************************************
 * 初始化对象列表
 ***************************************************************************/
  private static void __init__process() {
    GlobalEventBus.me();
    SysConfig.me();
    LogFactory.me();
    DbmsFactory.me().registeringDefaultDriver();
    Processes.me().init(Startup.getServletContext());
    UrlMapping.me();
    RedisMesmerizer.me();
    SandboxFactory.version();
    FileSystemFactory.me();
    AuthFactory.me();
    SqlReader.me();
    SystemStartupScript.me();
    ClusterManager.me();
    RpcFactory.me();
    PeerFactory.me();
    XLock.me();
  }



  private static final int S_ZERO   = 0;
  private static final int S_INITED = 0;
  private static final int S_EXIT   = 0;
  private static int state = S_ZERO;


  public synchronized static void me() {
    if (state != S_ZERO)
      throw new RuntimeException("cannot start system");

    //
    // 日志子系统尚未初始化
    //
    System.out.println("[" + new Date()
            + "] [Touch.me] ---------- xBoson system boot -----------\n");

    GlobalEventBus.me().emit(Names.initialization, Touch.class);
    __init__process();
    GlobalEventBus.me().emit(Names.already_started, Touch.class);
    state = S_INITED;
  }


  public synchronized static void exit() {
    if (state != S_INITED)
      throw new RuntimeException("cannot exit system");

    LogFactory.create().info(
            "---------- xBoson system leaving -----------");

    GlobalEventBus.me().emit(Names.exit, Touch.class);
    state = S_EXIT;
  }


  static public class Init implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
      me();
    }
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
      exit();
    }
  }
}
