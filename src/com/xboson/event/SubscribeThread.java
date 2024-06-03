////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-12 上午10:28
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/event/SubscribeThread.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.event;

import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.Tool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;


/**
 * 在独立的线程中取 redis 消息队列, 并压入另一个线程中去执行.
 */
class SubscribeThread extends JedisPubSub implements Runnable {

  public final static String SUBSCRIBE_PATTERN = Names.CHANNEL_PREFIX + "*";

  private GlobalEventBus bus;
  private Thread thread;
  private Log log;
  private boolean running;
  private final long mySelfid;


  public SubscribeThread(GlobalEventBus bus, long mySelfid) {
    this.mySelfid = mySelfid;
    this.log      = LogFactory.create();
    this.running  = true;
    this.bus      = bus;
  }


  public void start() {
    if (thread != null) {
      throw new RuntimeException("Don't start again");
    }
    running = true;
    thread = new Thread(this);
    thread.setDaemon(true);
    thread.start();
  }


  /**
   * 必须且只能在 GlobalEventBus.destory() 中调用
   * @see GlobalEventBus
   */
  void destory() {
    running = false;
    punsubscribe();
    Tool.waitOver(thread);
    thread = null;
    log.info("destoryed");
  }


  @Override
  public void onPMessage(String pattern, String channel, String message) {
    try {
      EventPackage ep = EventPackage.fromjson(message);
      if (ep.from != mySelfid) {
        ep.parseData();
        GlobalEventContext context = bus.getContext(false,
                channel.substring(Names.CHANNEL_PREFIX.length()));

        if (context != null) {
          context.emitWithoutCluster(ep.data, ep.type, ep.info);
        }
      }
    } catch (Exception e) {
      log.error("onMessage()", e);
    }
  }


  @Override
  public void run() {
    while (running) {
      try (Jedis client = RedisMesmerizer.me().open()) {
        client.psubscribe(this, SUBSCRIBE_PATTERN);

      } catch (Exception e) {
        log.debug("STOP", e.getMessage());
        Tool.sleep(1000);

      } finally {
        running = false;
      }
    }
  }
}
