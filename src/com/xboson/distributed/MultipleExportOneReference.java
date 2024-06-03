////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-12-9 下午12:03
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/distributed/MultipleExportOneReference.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.distributed;

import com.xboson.rpc.ClusterManager;
import com.xboson.rpc.IXRemote;
import com.xboson.rpc.RpcFactory;

import java.rmi.RemoteException;


/**
 * 集群服务模式, 所有节点将服务导出到集群中, 使用时遍历所有节点上的服务
 */
public class MultipleExportOneReference<T extends IXRemote> {

  private ClusterManager cm;
  private RpcFactory rpc;
  private String name;


  public interface For<E extends IXRemote> {

    /**
     * 每个节点上的服务调用该方法
     * @param index 索引
     * @param nodeid 节点索引
     * @param service 节点上的服务
     * @return 如果返回 false, 则不再迭代更多节点
     */
    boolean node(int index, String nodeid, E service) throws RemoteException;
  }


  /**
   * 指定服务名
   */
  public MultipleExportOneReference(String name) {
    this.rpc  = RpcFactory.me();
    this.cm   = ClusterManager.me();
    this.name = name;
  }


  /**
   * 只绑定服务一次
   */
  public boolean bindOnce(IXRemote remote) {
    return rpc.bindOnce(remote, name);
  }


  /**
   * 遍历所有服务, 异常将会终止遍历
   */
  public void each(For<T> getter) throws RemoteException {
    int i = 0;

    for (String node : cm.list()) {
      T service = (T) rpc.lookup(node, name);
      if (! getter.node(i, node, service)) {
        break;
      }
      ++i;
    }
  }


  public RpcFactory getRpc() {
    return rpc;
  }
}
