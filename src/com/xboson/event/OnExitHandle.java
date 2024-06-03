////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-12 上午9:35
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/event/OnExitHandle.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.event;

import com.xboson.log.Log;
import com.xboson.log.LogFactory;

import javax.naming.event.NamingEvent;

/**
 * 监听系统退出的方便实现, 自动将自身注册到全局事件上.
 */
public abstract class OnExitHandle extends GLHandle {

  private static boolean orderInit = false;


  public OnExitHandle() {
    GlobalEventBus.me().on(Names.exit, this);

    if (!orderInit) {
      //
      // 只在第一个使用 OnExitHandle 对象的地方设置一次消息顺序.
      //
      orderInit = true;
      GlobalEventBus.me().setEmitOrder(Names.exit, false);
    }
  }


  public void objectChanged(NamingEvent namingEvent) {
    String name = namingEvent.getNewBinding().getName();
    Log log = getLog();

    switch (name) {
      case Names.exit:
        exit();
        log.info("destory on exit");
        return;
    }
  }


  /**
   * 从全局事件中, 移除自身.
   */
  public void removeExitListener() {
    boolean rm = GlobalEventBus.me().off(Names.exit, this);
    assert rm : "must removed";
  }


  /**
   * 系统退出时被调用
   */
  protected abstract void exit();
}
