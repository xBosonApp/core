////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-12-9 下午1:07
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/distributed/MasterMode.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.distributed;

import com.xboson.been.Config;
import com.xboson.rpc.IXRemote;
import com.xboson.rpc.RpcFactory;
import com.xboson.util.SysConfig;
import com.xboson.util.c0nst.IConstant;


/**
 * 集群服务模式, 服务由主节点提供, 其他节点总是引用主节点上的服务
 */
public class MasterMode<T extends IXRemote> implements IConstant {

  private RpcFactory rpc;
  private boolean isMaster;
  private T local_service;
  private String rpcName;


  /**
   * 该类的实例通常为静态属性并在系统初始化时创建, 初始化顺序很重要,
   * 因为其他节点可以在任何时候访问主节点.
   *
   * @param rpcName 服务名称
   * @param local 服务类构造器, 在非 Master 节点上, 不会实例化本地服务.
   */
  public MasterMode(String rpcName, ILocalServiceCreator<T> local) {
    this.rpcName = rpcName;
    this.rpc = RpcFactory.me();
    Config cf = SysConfig.me().readConfig();
    this.isMaster = cf.clusterNodeID == MASTER_NODE;

    if (isMaster) {
      this.local_service = local.newInstance();
      rpc.bindOnce(local_service, rpcName);
    } else {
      this.local_service = null;
    }
  }


  /**
   * 返回集群上的服务
   */
  public T get() {
    if (isMaster) {
      return local_service;
    } else {
      return (T) rpc.lookup(MASTER_NODE_STR, rpcName);
    }
  }
}
