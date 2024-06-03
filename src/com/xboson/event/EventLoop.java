////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-19 下午12:31
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/event/EventLoop.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.event;

import com.xboson.been.XBosonException;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.c0nst.IConstant;
import com.xboson.util.Tool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * 一个全局单线程任务管理器, 所有任务都在一个线程中运行,
 * 适合各种初始化操作, 非紧急的任务.
 */
public class EventLoop implements ThreadFactory, IConstant {

  private final static String THREAD_NAME = "EventLoopThread";

  private static EventLoop instance;
  private ExecutorService worker;
  private Log log;


  private EventLoop() {
    this.worker = Executors.newSingleThreadExecutor(this);
    this.log = LogFactory.create("event-loop");
    log.info(INITIALIZATION);
  }


  public static EventLoop me() {
    if (instance == null) {
      synchronized (EventLoop.class) {
        if (instance == null) {
          instance = new EventLoop();
        }
      }
    }
    return instance;
  }


  /**
   * 在线程上添加一个任务, 如果任务队列为空可以立即执行,
   * 否则等到队列中之前的任务执行完成.
   */
  public void add(Runnable task) {
    log.debug("Add task:", task);
    worker.execute(new Wrap(task));
  }


  /**
   * 必须在 GlobalEventBus.destory() 中调用
   * @see GlobalEventBus
   */
  void destory() {
    worker.shutdown();
    Tool.waitOver(worker);
    worker = null;
    log.info(DESTORYED);
  }


  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(r, THREAD_NAME);
    t.setPriority(Thread.MIN_PRIORITY);
    log.info("Create Local Thread Object:", t);
    return t;
  }


  private class Wrap implements Runnable {
    private Runnable r;

    private Wrap(Runnable r) {
      this.r = r;
    }

    public void run() {
      try {
        r.run();
      } catch (XBosonException.Shutdown e) {
        log.warn("Run Task ["+ r +"],", e);
      } catch (Throwable t) {
        log.error("Run Task ["+ r +"],", Tool.allStack(t));
      }
    }
  }
}
