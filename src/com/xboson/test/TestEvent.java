////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月11日 09:05
// 原始文件路径: xBoson/src/com/xboson/test/TestEvent.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.squareup.moshi.JsonAdapter;
import com.xboson.event.GlobalEventBus;
import com.xboson.event.GlobalListener;
import com.xboson.event.Names;
import com.xboson.event.QuickSender;
import com.xboson.event.timer.EarlyMorning;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.Tool;
import redis.clients.jedis.Jedis;

import javax.naming.Binding;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import java.io.IOException;
import java.util.Date;

public class TestEvent extends Test implements GlobalListener {

  private Object recv = null;
  private int count = 0;
  private boolean not_recive_when_off = true;


  public void test() throws Exception {
    GlobalEventBus ge = GlobalEventBus.me();
    ge.on("test", this);
    ge.on(Names.inner_error, this);
    TestFace.waitEventLoopEmpty();

    send(new Date().toString());
    send("hello.2=" + Math.random());
    send(new TestData());

    ok(recv != null, "recive data");
    eq(3, count, "recive count");

    QuickSender.emitError(new Exception("yes"), this);

    not_recive_when_off = true;
    ge.off("test", null);
    ge.emit("test", "not recive");
    ok(not_recive_when_off, "off() remove listener");

    ge.off(Names.inner_error,null);

    test_recv();
    timer();
  }


  public void timer() throws Exception {
    msg(EarlyMorning.class, EarlyMorning.first, EarlyMorning.hour24);
  }


  private static String send = "PUBLISH \"/com.xboson.event.GlobalEventBus/recv\" " +
          "\"{\\\"className\\\":\\\"com.xboson.test.Test$TestData\\\"," +
          "\\\"data\\\":\\\"{\\\\\\\"a\\\\\\\":957,\\\\\\\"b\\\\\\\":17165," +
          "\\\\\\\"name\\\\\\\":\\\\\\\"oEp0fi1XiKEA7w==\\\\\\\"}\\\"," +
          "\\\"from\\\":1,\\\"type\\\":3}\"";

  private static String quit = "PUBLISH \"/com.xboson.event.GlobalEventBus/recv\" " +
          "\"{\\\"data\\\":\\\"quit\\\",\\\"from\\\":1,\\\"type\\\":3}\"";


  void test_recv() throws InterruptedException {
    msg("命令用来模拟另一个节点发送数据:");
    sub(send);
    msg("命令可以让进程退出:");
    sub(quit);
    msg("等待另一个节点或 Redis 客户端发送来数据...");

    WaitRecv _wait = new WaitRecv();
    GlobalEventBus ge = GlobalEventBus.me();
    ge.on("recv", _wait);
    ge.on(Names.inner_error, _wait);

    new SendThread().start();
    Thread t = new Thread(_wait);
    t.start();
    t.join();
  }


  /**
   * 用 redis 客户端发送一条消息, 这里会接收到
   */
  class WaitRecv implements GlobalListener, Runnable {
    boolean over = false;

    @Override
    public void run() {
      while (!over) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    @Override
    public void objectChanged(NamingEvent namingEvent) {
      String name = namingEvent.getNewBinding().getName();
      Object data = namingEvent.getNewBinding().getObject();

      msg("Recv in Thread "+ name + " " + data);
      if (data.equals("quit")) {
        over = true;
        msg("OK bye !!");
      }
    }

    @Override
    public void namingExceptionThrown(NamingExceptionEvent namingExceptionEvent) {
      namingExceptionEvent.getException().printStackTrace();
    }
  }


  class SendThread extends Thread {
    public void run() {
      try (Jedis client = RedisMesmerizer.me().open()) {
        _send_cmd(send, client);
        sub("Wait to Send quit..");
        Tool.sleep(2000);
        _send_cmd(quit, client);
        sub("Send Quit.");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    void _send_cmd(String cmd, Jedis client) throws IOException {
      String[] sp = cmd.split(" ", 3);
      JsonAdapter<String> ja = Tool.getAdapter(String.class);
      String ch = ja.fromJson(sp[1]);
      String data = ja.fromJson(sp[2]);

      client.publish(ch, data);
    }
  }


  void send(Object data) {
    GlobalEventBus ge = GlobalEventBus.me();
    ge.emit("test", data);
    Tool.sleep(1000);
    eq(recv, data, "recive: " + recv);
  }


  @Override
  public void objectChanged(NamingEvent e) {
    Binding b = e.getNewBinding();
    String name = b.getName();
    Object data = b.getObject();
    recv = data;

    msg(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> [get event]"
            + "\n\t\t\tname: "+ name
            + "\n\t\t\tdata: " + data
            + "\n\t\t\tinfo: " + e.getChangeInfo());

    if(name.equals(Names.inner_error) ) {
      msg("LOG print 'error by error'");
      throw (RuntimeException) b.getObject();
    }

    if ("not recive".equals(data)) {
      not_recive_when_off = false;
    }
    ++count;
  }


  @Override
  public void namingExceptionThrown(NamingExceptionEvent namingExceptionEvent) {
  }


  public static void main(String[] a) throws Exception {
    sub("Global Event");
    new TestEvent();
  }
}
