////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月11日 上午07:16:48
// 原始文件路径: xBoson/src/com/xboson/util/GlobalEventBus.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.event;

import com.xboson.been.XBosonException;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.Tool;

import javax.naming.event.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 全局事件总线, 线程安全的.
 *
 * 事件是实时的, 一个节点发送的事件将被所有节点接收, 一旦事件发送完成,
 * 在线的所有节点一定会受到事件的副本, 离线节点在上线后一定无法接受该事件.
 *
 * 该对象是系统第一个初始化的对象, 也是最后一个有效对象(销毁的对象).
 */
public class GlobalEventBus {

  private static GlobalEventBus instance;


  public static final GlobalEventBus me() {
    if (instance == null) {
      synchronized (GlobalEventBus.class) {
        if (instance == null) {
          instance = new GlobalEventBus();
          instance.init();
        }
      }
    }
    return instance;
  }


  private Map<String, GlobalEventContext> contexts;
  private SubscribeThread sub_thread;
  private Log log;
  private long myselfid;
  private boolean destoryed;


  /**
   * 构造时什么也不做, 初始化都在 init() 中做.
   */
  private GlobalEventBus() {
    contexts = new ConcurrentHashMap<>();
  }


  private void init() {
    log = LogFactory.create();
    myselfid = Tool.nextId();
    sub_thread = new SubscribeThread(this, myselfid);
    sub_thread.start();
    log.info("Initialization Success");
  }


  private void destory() {
    destoryed = true;
    Iterator<GlobalEventContext> it = contexts.values().iterator();
    while (it.hasNext()) {
      try {
        it.next().destory();
      } catch(Exception e) {
        log.error("Destory context", e);
      }
    }
    sub_thread.destory();
    EventLoop.me().destory();
    log.info("destoryed");
    log.info("---------- xBoson system Gone ----------");

    sub_thread = null;
    contexts = null;
  }


  protected GlobalEventContext getContext(boolean createIfNeed, String name) {
    GlobalEventContext context = contexts.get(name);
    if (context == null && createIfNeed) {
      context = GlobalEventContext.create(name, contexts, myselfid);
    }
    return context;
  }


  /**
   * 监听事件
   *
   * @param name 事件名称
   * @param listener 监听器对象
   */
  public void on(String name, GlobalListener listener) {
    if (destoryed) {
      throw new XBosonException.Shutdown();
    }

    if (name == null)
      throw new NullPointerException("name");
    if (listener == null)
      throw new NullPointerException("listener");

    GlobalEventContext context = getContext(true, name);
    context.on(listener);
  }


  /**
   * 设置接收消息的顺序.
   *
   * @param name 事件名称
   * @param isLilo true: (默认) 先注册的监听器先接受到消息;
   *               false: 后注册的监听器, 先接受到消息
   */
  public void setEmitOrder(String name, boolean isLilo) {
    GlobalEventContext context = getContext(true, name);
    context.setEmitOrder(isLilo);
  }


  /**
   * 删除事件监听器
   *
   * @param name 事件名称
   * @param listener 监听器对象, 如果 null 则删除所有在 name 上的监听器
   * @return 如果删除了监听器返回 true, 如果监听器不存在返回 false
   */
  public boolean off(String name, GlobalListener listener) {
    if (destoryed) { return false; }

    if (name != null) {
      synchronized (this) {
        GlobalEventContext context = contexts.get(name);
        if (context == null)
          return false;

        if (listener == null) {
          context.destory();
          contexts.remove(name);
          return true;
        } else {
          Set<GlobalListener> ls = context.getListeners();
          boolean ret = ls.remove(listener);
          if (ls.isEmpty()) {
            context.destory();
            contexts.remove(name);
          }
          return ret;
        }
      }
    }
    return false;
  }


  /**
   * 触发事件. (节点自身也会接受到)
   *
   * @param name 事件名称
   * @param data 数据, NamingEvent.getNewBinding().getObject() 返回
   * @param type 数据类型, NamingEvent.getType() 返回
   * @param info 扩展描述, NamingEvent.getChangeInfo() 返回
   * @return 忽略返回值
   */
  public boolean emit(String name, Object data, int type, String info) {
    if (destoryed) {
      throw new XBosonException.Shutdown();
    }

    GlobalEventContext context = getContext(true, name);
    context.emit(data, type, info);

    // 这是个特殊的事件, 当检查到退出系统的消息后, 等待所有处理器退出
    // 然后 GlobalEventBus 执行自身的退出操作
    if (Names.exit.equals(name)) {
      destory();
    }
    return true;
  }


  /**
   * [ info = null ]
   * @see #emit(String, Object, int, String)
   */
  public boolean emit(String name, Object data, int type) {
    return emit(name, data, type, null);
  }


  /**
   * [ type = NamingEvent.OBJECT_CHANGED, info = null; ]
   * @see #emit(String, Object, int, String)
   */
  public boolean emit(String name, Object data) {
    return emit(name, data, NamingEvent.OBJECT_CHANGED, null);
  }


  /**
   * [ data = null, type = NamingEvent.OBJECT_CHANGED, info = null; ]
   * @see #emit(String, Object, int, String)
   */
  public boolean emit(String name) {
    return emit(name, null, NamingEvent.OBJECT_CHANGED, null);
  }

}
