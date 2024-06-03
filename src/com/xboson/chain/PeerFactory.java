////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-17 下午12:08
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/PeerFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.been.XBosonException;
import com.xboson.event.OnExitHandle;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.rpc.ClusterManager;
import com.xboson.rpc.RpcFactory;
import com.xboson.util.SysConfig;
import com.xboson.util.c0nst.IConstant;


public class PeerFactory extends OnExitHandle implements IConstant {

  private static final String RPC_NAME   = "XB.rpc.blockchain.peer";
  private static final String ORDER_NODE = "0";

  private static PeerFactory instance;

  private final Log log;
  private final boolean enable;
  private IPeer peer;


  private PeerFactory() {
    this.log    = LogFactory.create("chain-peer-factory");
    this.enable = SysConfig.me().readConfig().chainEnable;

    if (!enable)
      return;

    modeFixOrderNode();
    // modeRaft();
  }


  /**
   * Raft 算法, 无单点失效问题.
   * <a href="https://raft.github.io/">Raft 可视化</a>
   */
  private void modeRaft() {
    // TODO
  }


  /**
   * 固定 0 号节点为主节点, 如果该节点失效, 区块链服务不可用
   */
  private void modeFixOrderNode() {
    String nodeid = ClusterManager.me().localNodeID();
    boolean is_order = ORDER_NODE.equals(nodeid);
    if (is_order) {
      peer = new Order();
      RpcFactory.me().bind(peer, RPC_NAME);
    } else {
      peer = new Peer(() -> {
        return (IPeer) RpcFactory.me().lookup(ORDER_NODE, RPC_NAME);
      });
    }

    if (peer instanceof IPeerLocal) {
      IPeerLocal pl = (IPeerLocal) peer;
      installSigner(pl);
      pl.startSynchronizeThread();
    }
  }


  @Override
  protected void exit() {
    if (peer instanceof IPeerLocal) {
      ((IPeerLocal) peer).waitOver();
    }
    peer = null;
  }


  public static PeerFactory me() {
    if (instance == null) {
      synchronized (PeerFactory.class) {
        if (instance == null) {
          instance = new PeerFactory();
        }
      }
    }
    return instance;
  }


  public IPeer peer() {
    if (peer == null) {
      throw new XBosonException.Closed("Blockchain server");
    }
    return peer;
  }


  /**
   * 安装签名提供商
   * @see IConstant#CHAIN_SIGNER_PROVIDER 签名提供商来源
   */
  private void installSigner(IPeerLocal pl) {
    try {
      Class<? extends ISignerProvider> ps = CHAIN_SIGNER_PROVIDER;
      ISignerProvider sp = ps.newInstance();
      pl.registerSignerProvider(sp);
    } catch(Exception e) {
      log.error("install signer", e);
    }
  }
}
