////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-30 上午11:50
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestRPC.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.app.lib.PmImpl;
import com.xboson.auth.PermissionException;
import com.xboson.been.ComputeNodeInfo;
import com.xboson.been.PublicProcessData;
import com.xboson.rpc.*;
import com.xboson.util.Network;
import com.xboson.util.Tool;

import java.net.InetAddress;
import java.util.List;


public class TestRPC extends Test {

  static final String nodeID = "0";


  @Override
  public void test() throws Throwable {
    net();
    cluster();
    safe();
    rpc();
//    connectFail();
    rpcApp();
  }


  // 需要模拟一个 servlet 上下文, 否则测试错误
  private void safe() throws InterruptedException {
    sub("Bad password throws PermissionException");
    SafeServerFactory server = new SafeServerFactory("123");
    SafeClientFactory client = new SafeClientFactory("321");
    final int port = 100;

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        new Throws(PermissionException.class) {
          public void run() throws Throwable {
            server.createServerSocket(port).accept();
          }
        };
      }
    });
    t.start();

    new Throws(PermissionException.class) {
      @Override
      public void run() throws Throwable {
        client.createSocket("localhost", port);
      }
    };

    t.join();
  }


  /**
   * 启动 '0' 号节点, 成功连接后终止 '0' 节点进程,
   * 此时连接被中断, 重启 '0' 好节点, 连接可以恢复.
   * 这个测试必须人工介入.
   */
  public void connectFail() {
    sub("TCP connect broker");

    RpcFactory rpc = RpcFactory.me();

    for (int i=0; i<1000; ++i) {
      Tool.sleep(1000);
      try {
        IPing p = rpc.lookup("0", Ping.class);
        String r = p.ping("test");
        msg("Call ping()", r);
      } catch(Exception e) {
        msg("Call ping()", Tool.allStack(e));
      }
    }
  }


  public void rpc() {
    try {
      RpcFactory rpc = RpcFactory.me();

      sub("----- Register Objects ------");
      for (String s : rpc.list(nodeID)) {
        msg(s);
      }

      sub("----- Ping -----");
      Ping lp = new Ping();
      rpc.rebind(lp);

      Tool.sleep(500);

      IPing p = rpc.lookup(nodeID, Ping.class);
      msg("Local", lp);
      msg("Remote", p);

      msg("Call ping()", p.ping("test 1"));
      msg("Call ping()", p.ping("test 2"));
      msg("Call ping()", p.ping("test 3"));

      ok(lp != p, "Remote Object");

    } catch(Exception e) {
      warn(e, "\n该测试的正确运行依赖一个 ID=0 的节点");
    }
  }


  /**
   * 需要有一个 tomcat 节点在运行
   */
  public void rpcApp() throws Exception {
    try {
      RpcFactory rpc = RpcFactory.me();

      sub("------- RPC Process List ---------");

      //
      // 服务器上运行过 PM 模块这个对象才注册到全局
      //
      PmImpl.IPM pm = (PmImpl.IPM) rpc.lookup(nodeID, PmImpl.RPC_NAME);
      for (PublicProcessData pd : pm.list()) {
        msg("Procdss:", pd.toJSON());
      }
      pm.equals(pm);

    } catch(Exception e) {
      warn(e, "\n该测试的正确运行依赖一个 ID=0 的节点");
    }
  }


  public void cluster() {
    sub("Cluster");
    ComputeNodeInfo node = new ComputeNodeInfo("test", 0);
    msg(node.toJSON());
  }


  public void net() throws Throwable {
    sub("Network Interfaces");
    List<InetAddress> a = Network.netWorkerInterfaces();
    for (InetAddress aa : a) {
      msg(aa.getHostAddress(), aa.isSiteLocalAddress(), aa.isLinkLocalAddress());
    }
  }


  public static void main(String[] a) {
    new TestRPC();
  }


}
