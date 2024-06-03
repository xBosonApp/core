////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-6-12 上午7:17
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/script/EventLoop.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script;

import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.Tool;
import jdk.nashorn.api.scripting.ScriptObjectMirror;


/**
 * 模拟 nodejs 消息队列.
 * 该对象线程安全.
 *
 *  [ 队列0, 队列1, 队列2, ...... 队列N ]
 *     |     |       |            |
 *    top.next  => task         last.next => null
 */
public class EventLoop implements IVisitByScript {

  private final ScriptObjectMirror process;
  private final Log log;
  private Task top;
  private Task last;


  public EventLoop(ScriptObjectMirror process) {
    this.process = process;
    this.log = LogFactory.create("js:EventLoop");
    this.top = new Task(null);
    this.last = top;

    if (process == null) {
      log.warn("Not set 'process' object");
    }
  }


  /**
   * 将任务函数压入队列
   */
  public void push(ScriptObjectMirror func) {
    if (func == null || (! func.isFunction()))
      throw new IllegalArgumentException("must push Function");

    synchronized(this) {
      Task task = new Task(func);
      last.next = task;
      last = task;
    }
  }


  /**
   * 运行消息队列, 直到队列清空,
   * 如果队列中任务抛出异常不会停止队列而是将错误发送到处理器
   */
  public void runUntilEmpty() {
    if (top == last) return;

    //
    // 这里是否需要完全锁住线程有待测试
    //
    synchronized (this) {
      Task task = pullFirst();
      while (task != null) {
        try {
          task.func.call(this);
        } catch (Exception e) {
          sendError(e);
        }
        task = pullFirst();
      }
    }
  }


  private Task pullFirst() {
    Task ret = null;
    if (top.next != null) {
      ret = top.next;
      top.next = ret.next;
      if (top.next == null) {
        last = top;
      }
    }
    return ret;
  }


  private void sendError(Exception e) {
    log.error(Tool.allStack(e));
    if (process == null) {
    } else {
      process.callMember("emit", "error", e);
    }
  }


  public class Task {
    private final ScriptObjectMirror func;
    private Task next;

    private Task(ScriptObjectMirror func) {
      this.func = func;
    }
  }
}
