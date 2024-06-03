////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-17 上午8:49
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/Peer.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.been.ChainEvent;
import com.xboson.been.JsonHelper;
import com.xboson.event.EventLoop;
import com.xboson.event.GLHandle;
import com.xboson.event.GlobalEventBus;
import com.xboson.event.Names;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.rpc.RpcFactory;
import com.xboson.util.Hex;
import com.xboson.util.LocalLock;

import javax.naming.event.NamingEvent;
import java.rmi.RemoteException;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;


/**
 * 非 0 号节点都是账本节点, 可以查询, 数据同步依赖排序服务
 */
public class Peer extends AbsPeer {

  private IPeerProvider orderRpc;
  private final Log log;


  /**
   * 使用工厂来创建该类的实例, 而不是使用构造函数
   * @see PeerFactory#peer() 创建该类的实例
   */
  public Peer(IPeerProvider orderRpc) {
    this.log = LogFactory.create("chain-peer");
    this.orderRpc = orderRpc;
  }


  @Override
  public void startSynchronizeThread() {
    new ChainSyncListener();
  }


  private IPeer getOrder() {
    return orderRpc.getLeader();
  }


  public byte[] sendBlock(String chainName, String channelName, BlockBasic b)
          throws RemoteException {
    try (LocalLock _ = new LocalLock(lock.writeLock())) {
      return getOrder().sendBlock(chainName, channelName, b);
    }
  }


  @Override
  public void createChannel(String chainName, String channelName,
                            String uid, String exp, KeyPair[] kp)
          throws RemoteException {
    try (LocalLock _ = new LocalLock(lock.writeLock())) {
      getOrder().createChannel(chainName, channelName, uid, exp, kp);
    }
  }


  private void syncAllChannels() {
    try {
      log.info("start synchronized all chain/channel");

      for (ChainEvent ce : getOrder().allChainSetting()) {
        if (! channelExists(ce.chain, ce.channel)) {
          syncChannel(ce);
        }
        syncBlock(ce);
      }
    } catch(Exception e) {
      log.error("synchronized all channels", e);
    }
  }


  /**
   * 同步: 拉取主节点的链到本地链
   */
  private void syncBlock(ChainEvent e) {
    try (LocalLock _ = new LocalLock(lock.writeLock())) {
      log.debug("synchronized block", e.chain, "/", e.channel);

      byte[] lkey = getOrder().lastBlockKey(e.chain, e.channel);
      Deque<Block> syncBlockStack = new LinkedList<>();

      BlockFileSystem.InnerChain ca = getChain(e.chain);
      BlockFileSystem.InnerChannel ch = getChannel(e.chain, e.channel);

      while (ch.search(lkey) == null) {
        Block b = getOrder().search(e.chain, e.channel, lkey);
        if (b == null && !ch.verify(b)) {
          log.error("synchronized block", JsonHelper.toJSON(b));
          break;
        }
        syncBlockStack.push(b);
        lkey = b.key;
      }

      if (! Arrays.equals(ch.lastBlockKey(), lkey)) {
        log.error("synchronized block, key not find in last block:",
                Hex.upperHex(lkey));
        return;
      }

      while (syncBlockStack.size() > 0) {
        Block b = syncBlockStack.pop();
        ch.pushOriginal(b);
      }
      ca.commit();
    } catch (Exception err) {
      log.error("synchronized block", err);
    }
  }


  private void syncChannel(ChainEvent e) {
    try (LocalLock _ = new LocalLock(lock.writeLock())) {
      log.debug("synchronized channel setting", e.chain, "/", e.channel);
      byte[] gkey = getOrder().genesisKey(e.chain, e.channel);
      Block gb = getOrder().search(e.chain, e.channel, gkey);

      ISigner si = getSigner(e.chain, e.channel, e.exp, e.kp);
      if (! si.verify(gb)) {
        log.error("synchronized genesis block verify fail",
                JsonHelper.toJSON(gb));
        return;
      }

      BlockFileSystem.InnerChain ca = getChain(e.chain);
      ca.createChannel(e.channel, si, gb);
      ca.commit();
    } catch (RemoteException re) {
      log.error("synchronized channel", re);
    }
  }


  /**
   * 接受 Order 发送的同步消息并同步本地数据
   */
  public class ChainSyncListener extends GLHandle {

    private ChainSyncListener() {
      GlobalEventBus.me().on(Names.chain_sync, this);
      EventLoop.me().add(() -> syncAllChannels());
    }

    @Override
    public void objectChanged(NamingEvent e) {
      ChainEvent ce = (ChainEvent) e.getNewBinding().getObject();

      switch (e.getType()) {
        case NEW_CHANNEL:
          syncChannel(ce);
          break;

        case NEW_BLOCK:
          syncBlock(ce);
          break;

        default:
          log.error("bad chain synchronized type");
          break;
      }
    }
  }
}
