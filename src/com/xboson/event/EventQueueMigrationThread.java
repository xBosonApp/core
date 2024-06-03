////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-20 上午10:35
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/j2ee/ui/EventQueueMigrationThread.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.event;

import com.xboson.fs.redis.IFileSystemConfig;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.c0nst.IConstant;
import com.xboson.util.Tool;
import redis.clients.jedis.Jedis;

import javax.naming.event.NamingEvent;
import java.util.List;


/**
 * 事件队列迁移线程, 该线程将队列中的事件取出后发布到集群订阅通知中.
 * 允许有多个该线程的实例来分担任务, 但每个一条消息只被一个线程处理一次.
 * 在系统进入销毁程序, 该线程自动终止.
 */
public class EventQueueMigrationThread extends OnExitHandle implements Runnable {

  public static final int QUEUE_TIMEOUT   = 5; // 秒
  public static final int EVENT_TYPE = NamingEvent.OBJECT_CHANGED;

  private boolean running;
  private Thread myself;
  private Log log;
  private String queue_name;
  private String event_name;


  /**
   * 创建线程并启动
   */
  public EventQueueMigrationThread(IFileSystemConfig config) {
    this.event_name = config.configFileChangeEventName();
    this.queue_name = config.configQueueName();
    this.running = true;
    this.log = LogFactory.create(EventQueueMigrationThread.class, queue_name);

    myself = new Thread(this);
    myself.start();
    log.info(IConstant.INITIALIZATION);
  }


  public void run() {
    myself = Thread.currentThread();
    log.info("Start", myself);
    GlobalEventBus ge = GlobalEventBus.me();

    try (Jedis client = RedisMesmerizer.me().open()) {
      while (running) {
        //
        // 在队列上等待 QUEUE_TIMEOUT 秒后返回
        //
        List<String> ret = client.brpop(QUEUE_TIMEOUT, queue_name);
        if (ret.size() == 2) {
          ge.emit(event_name, ret.get(1), EVENT_TYPE, ret.get(0));
        }
      }
    } finally {
      running = false;
    }
    log.info("Stop", myself);
  }


  /**
   * 该方法会尝试让线程终止
   */
  public void stop() {
    exit();
    removeExitListener();
  }


  public boolean isRunning() {
    return running;
  }


  @Override
  protected void exit() {
    if (!running) return;
    running = false;
    log.debug("Wait...", QUEUE_TIMEOUT +"s");
    Tool.waitOver(myself);
  }
}
