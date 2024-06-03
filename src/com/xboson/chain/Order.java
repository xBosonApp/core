////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-17 上午8:49
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/Order.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.been.ChainEvent;
import com.xboson.event.GlobalEventBus;
import com.xboson.event.Names;
import com.xboson.rpc.IXRemote;
import com.xboson.rpc.RpcFactory;

import java.security.KeyPair;


/**
 * 只有 0 号节点有排序服务
 */
public class Order extends AbsPeer implements IXRemote, IPeer {

  /**
   * 使用工厂来创建该类的实例, 而不是使用构造函数
   * @see PeerFactory#peer() 创建该类的实例
   */
  public Order() {
  }


  public byte[] sendBlock(String chainName, String channelName, BlockBasic b) {
    byte[] key = sendBlockLocal(chainName, channelName, b);
    sendNewBlock(chainName, channelName);
    return key;
  }


  public void createChannel(String chainName, String channelName,
                            String uid, String exp, KeyPair[] ks) {
    createChannelLocal(chainName, channelName, uid, exp, ks);
    sendNewChannel(chainName, channelName, exp, ks);
  }


  private void sendNewChannel(String chain, String channel, String exp, KeyPair[] ks) {
    ChainEvent e = new ChainEvent(chain, channel, exp, ks);
    GlobalEventBus.me().emit(Names.chain_sync, e, NEW_CHANNEL);
  }


  private void sendNewBlock(String chain, String channel) {
    ChainEvent e = new ChainEvent(chain, channel, null, null);
    GlobalEventBus.me().emit(Names.chain_sync, e, NEW_BLOCK);
  }
}
