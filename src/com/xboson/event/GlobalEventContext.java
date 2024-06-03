////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-12 上午9:35
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/event/GlobalEventContext.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.event;

import com.xboson.been.XBosonException;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.ReverseIterator;
import com.xboson.util.Tool;
import redis.clients.jedis.Jedis;

import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingListener;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class GlobalEventContext extends InitialContext implements EventContext {

  private Set<GlobalListener> listeners;
  private String name;
  private Binding oldbind;
  private Log log;
  private boolean skip_error;
  private final long myselfid;
  private boolean isLiLo;

  private String channel_name;


  public GlobalEventContext(String name, long myselfid) throws NamingException {
    super(true);
    this.listeners = Collections.synchronizedSet(new LinkedHashSet<>());
    this.name      = name;
    this.log       = LogFactory.create("global-event."+ name);
    this.myselfid  = myselfid;
    this.isLiLo    = true;

    //
    // 带有 sys 开头的消息不会在集群中路由
    //
    if (name.indexOf("sys.") != 0) {
      this.channel_name = Names.CHANNEL_PREFIX + name;
    }

    if (name.equals(Names.inner_error)) {
      skip_error = true;
    }
  }


  /**
   * 创建实例并返回, 将创建的实例以 name 为键插入 map 中.
   */
  public static GlobalEventContext create(String name,
                                          Map<String, GlobalEventContext> map,
                                          long myselfid) {
    try {
      GlobalEventContext gec = new GlobalEventContext(name, myselfid);
      map.put(name, gec);
      return gec;
    } catch (NamingException e) {
      throw new XBosonException(e);
    }
  }


  Set<GlobalListener> getListeners() {
    return listeners;
  }


  String getChannelName() {
    return channel_name;
  }


  void destory() {
    listeners = null;
    oldbind = null;
    name = null;
  }


  void on(GlobalListener listener) {
    if (listener == null)
      throw new XBosonException.NullParamException("GlobalListener listener");

    listeners.add(listener);
  }


  /**
   * 自动判断消息是否需要发布到集群
   */
  void emit(Object data, int type, String info) {
    emitWithoutCluster(data, type, info);

    if (channel_name != null) {
      try (Jedis client = RedisMesmerizer.me().open()) {
        EventPackage ep = new EventPackage(data, type, info, myselfid);
        client.publish(channel_name, ep.tojson());
      }
    }
  }


  /**
   * 只在系统内部发出消息
   */
  void emitWithoutCluster(Object data, int type, String info) {
    if (listeners.size() < 1)
      return;

    Binding newbind = new Binding(name, data);
    NamingEvent event = new NamingEvent(
            this, type, newbind, oldbind, info);
    oldbind = newbind;

    Iterator<GlobalListener> its = isLiLo
            ? listeners.iterator() : new ReverseIterator(listeners);

    while (its.hasNext()) {
      EmitWithoutCluster.emit(its.next(), event, skip_error);
    }
  }


  /**
   * 设置接收消息的顺序.
   *    true: (默认) 先注册的监听器先接受到消息
   *    false: 后注册的监听器, 先接受到消息
   * @param isLilo
   */
  void setEmitOrder(boolean isLilo) {
    this.isLiLo = isLilo;
  }


  @Override
  public void addNamingListener(Name name, int i, NamingListener namingListener)
          throws NamingException {
    throw new NamingException("not support");
  }


  @Override
  public void addNamingListener(String s, int i, NamingListener namingListener)
          throws NamingException {
    throw new NamingException("not support");
  }


  @Override
  public void removeNamingListener(NamingListener namingListener)
          throws NamingException {
    throw new NamingException("not support");
  }


  @Override
  public boolean targetMustExist() throws NamingException {
    return false;
  }

}
