////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-30 上午11:21
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/rpc/ClusterManager.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.rpc;

import com.xboson.been.ComputeNodeInfo;
import com.xboson.been.Config;
import com.xboson.been.XBosonException;
import com.xboson.event.OnExitHandle;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Set;


/**
 * 集群管理器
 */
public final class ClusterManager extends OnExitHandle {

  public static final String HNAME = "XB.Cluster.ComputeNodes";

  private static ClusterManager instance;
  private final Log log;
  private final String nodeID;
  private ComputeNodeInfo info;


  private ClusterManager() {
    Config cf   = SysConfig.me().readConfig();
    this.log    = LogFactory.create();
    this.nodeID = Short.toString(cf.clusterNodeID);
    this.info   = new ComputeNodeInfo(nodeID, 0);
    registerSelf();
    NodeUpdate.emit(nodeID);
  }


  @Override
  protected void exit() {
    try (Jedis client = RedisMesmerizer.me().open()) {
      client.hdel(HNAME, nodeID);
    }
  }


  public void updateRpcPort(int rpcPort) {
    info.rpcPort = rpcPort;
    registerSelf();
  }


  private void registerSelf() {
    try (Jedis client = RedisMesmerizer.me().open()) {
      String str = info.toJSON();
      client.hset(HNAME, nodeID, str);
      log.debug("Cluster Node registered:", str);
    }
  }


  public String localNodeID() {
    return nodeID;
  }


  /**
   * 返回集群中所有节点 id.
   */
  public Set<String> list() {
    try (Jedis client = RedisMesmerizer.me().open()) {
      return client.hkeys(HNAME);
    }
  }


  /**
   * 获取集群中节点的信息, 节点不存在返回 null.
   */
  public ComputeNodeInfo info(String id) {
    try (Jedis client = RedisMesmerizer.me().open()) {
      String str = client.hget(HNAME, id);
      if (str == null) return null;
      return Tool.getAdapter(ComputeNodeInfo.class).fromJson(str);
    } catch (IOException e) {
      throw new XBosonException.IOError(e);
    }
  }


  public static ClusterManager me() {
    if (instance == null) {
      synchronized (ClusterManager.class) {
        if (instance == null) {
          instance = new ClusterManager();
        }
      }
    }
    return instance;
  }
}
