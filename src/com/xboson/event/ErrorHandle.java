////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-12 上午11:37
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/event/ErrorHandle.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.event;

import javax.naming.Binding;
import javax.naming.event.NamingEvent;

/**
 * 注册错误消息监听器
 */
public abstract class ErrorHandle extends GLHandle implements GlobalListener {

  /**
   * 默认构造器注册到全局
   */
  public ErrorHandle() {
    GlobalEventBus.me().on(Names.inner_error, this);
  }


  @Override
  public void objectChanged(NamingEvent namingEvent) {
    try {
      Binding b = namingEvent.getNewBinding();
      if (b.getName().equals(Names.inner_error)) {
        onError((Exception) b.getObject(), namingEvent.getChangeInfo().toString());
      } else {
        getLog().warn("ErrorHandle recive Non-error message", b);
      }
    } catch(Exception e) {
      getLog().error(e);
    }
  }


  /**
   * 当收到错误时被调用
   */
  abstract void onError(Exception err, String source);

}
