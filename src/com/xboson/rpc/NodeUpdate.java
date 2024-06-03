////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-31 上午9:02
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/rpc/NodeUpdate.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.rpc;

import com.xboson.event.GLHandle;
import com.xboson.event.GlobalEventBus;
import com.xboson.event.Names;

import javax.naming.event.NamingEvent;


/**
 * 节点更新事件辅助对象
 *
 * @see Names#host_update 事件名称
 */
public abstract class NodeUpdate extends GLHandle {


  public NodeUpdate() {
    GlobalEventBus.me().on(Names.host_update, this);
  }


  public void objectChanged(NamingEvent namingEvent) {
    String host = (String) namingEvent.getNewBinding().getObject();
    onChange(host);
  }


  /**
   * 接受更新消息后该方法被调用
   * @param nodeID 更新的节点 id
   */
  protected abstract void onChange(String nodeID);


  /**
   * 发送全局消息, 通知整个集群节点信息已经更新
   */
  public static void emit(String nodeID) {
    GlobalEventBus.me().emit(Names.host_update, nodeID);
  }


  /**
   * 结束监听消息, 从全局事件移除自身.
   */
  public void removeFileListener() {
    GlobalEventBus.me().off(Names.host_update, this);
  }
}
