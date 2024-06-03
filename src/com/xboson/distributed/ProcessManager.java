////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-12-9 上午11:26
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/distributed/ProcessManager.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.distributed;

import com.xboson.app.AppContext;
import com.xboson.app.IProcessState;
import com.xboson.been.LoginUser;
import com.xboson.been.PublicProcessData;
import com.xboson.event.timer.TimeFactory;
import com.xboson.script.IVisitByScript;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


/**
 * '进程' 管理器
 */
public class ProcessManager implements IProcessState {
  /** 检查 cpu 使用情况间隔, ms */
  private final static long INTERVAL = 30 * 1000;
  private Map<Thread, AppContext.ThreadLocalData> running;
  private Map<Long, Thread> id;


  public ProcessManager() {
    running = new ConcurrentHashMap<>();
    id = new ConcurrentHashMap<>();
    startCpuChecker();
  }


  private void startCpuChecker() {
    TimeFactory.me().schedule(new TimerTask() {
      public void run() {
        cpuSafe();
      }
    }, INTERVAL, INTERVAL);
  }


  /**
   * 请求线程开始进入管理区
   */
  public void start(AppContext.ThreadLocalData data) {
    Thread t = Thread.currentThread();
    running.put(t, data);
    id.put(t.getId(), t);
  }


  /**
   * 请求线程退出
   */
  public void exit() {
    exit(Thread.currentThread());
  }


  public void exit(Thread t) {
    running.remove(t);
    id.remove(t.getId());
  }


  /**
   * 返回当前线程的绑定数据,
   * 不在应用上下文中调用会抛出 IllegalStateException
   */
  public AppContext.ThreadLocalData get() {
    Thread t = Thread.currentThread();
    AppContext.ThreadLocalData ret = running.get(t);
    if (ret == null) {
      throw new IllegalStateException("Not in App Context");
    }
    return ret;
  }


  /**
   * 返回当前线程的绑定数据, 不在应用上下文中调用返回 null
   */
  public AppContext.ThreadLocalData getMaybeNull() {
    Thread t = Thread.currentThread();
    return running.get(t);
  }


  /**
   * 检查所有线程, 一旦发现线程执行时间过长, 则降低运行优先级
   */
  private void cpuSafe() {
    for (Map.Entry<Thread, AppContext.ThreadLocalData> entry : running.entrySet()) {
      AppContext.ThreadLocalData tld = entry.getValue();

      if (tld.notLowPriority() && tld.runningTime() > AppContext.LOW_CPU_TIME) {
        tld.setLowPriority(entry.getKey());
      }
    }
  }


  /**
   * 列出所有运行中的线程
   */
  public PublicProcessData[] list() {
    PublicProcessData[] ppd = new PublicProcessData[running.size()];
    int i = -1;

    for (Map.Entry<Thread, AppContext.ThreadLocalData> entry : running.entrySet()) {
      ppd[++i] = createPD(entry.getKey(), entry.getValue());
    }
    return ppd;
  }


  /**
   * 终止 api 进程.
   * @param processId 进程 id
   * @return 停止了正在运行的进程返回 true, 如果进程不存或已经停止在返回 false
   */
  public int kill(long processId) {
    Thread t = id.get(processId);
    if (t == null)
      return KILL_NO_EXIST;

    if (! t.isAlive())
      return KILL_IS_KILLED;

    AppContext.ThreadLocalData tld = running.get(t);
    if (tld == null)
      return KILL_NO_EXIST;

    if (tld.notReadyForKill())
      return KILL_NO_READY;

    //
    // 必须这样做, 脚本上下文的设计可以保证安全的 stop 线程.
    // [ 除非有 bug :( ]
    //
    t.stop();
    return KILL_OK;
  }


  public int stop(long processId) {
    return kill(processId);
  }


  private PublicProcessData createPD(Thread t, AppContext.ThreadLocalData tld) {
    PublicProcessData pd = new PublicProcessData();
    pd.processId = t.getId();
    tld.copyTo(pd);
    return pd;
  }
}
