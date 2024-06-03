////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-12-9 下午2:02
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/LockImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.app.ApiPath;
import com.xboson.app.AppContext;
import com.xboson.been.XBosonException;
import com.xboson.distributed.ILock;
import com.xboson.distributed.XLock;
import com.xboson.rpc.ClusterManager;
import com.xboson.util.Tool;

import java.rmi.RemoteException;


/**
 * 集群锁
 */
public class LockImpl {

  private String nodeId;
  private XLock xl;
  private AppContext app;


  public LockImpl() {
    this.xl     = XLock.me();
    this.app    = AppContext.me();
    this.nodeId = ClusterManager.me().localNodeID();
  }


  public Warp open(String name) throws RemoteException {
    if (Tool.isNulStr(name))
      throw new XBosonException.BadParameter("name", "is null");

    ILock local = xl.getLock().get(name);
    Warp w = new Warp(name, local);
    ModuleHandleContext.autoClose(w);
    return w;
  }


  public Warp api() throws Exception {
    return open("_$API-LOCK:"+ app.getCurrentApiPath());
  }


  public Warp mod() throws Exception {
    return open("_$MODEL-LOCK:"+ ApiPath.getModPath(app.getExtendParameter()));
  }


  public Warp org() throws Exception {
    return open("_$ORG-LOCK:"+ app.originalOrg());
  }


  public Warp app() throws Exception {
    return open("_$APP-LOCK:"+ ApiPath.getAppPath(app.getExtendParameter()));
  }


  public Warp node() throws Exception {
    return open("_$NODE-LOCK:"+ nodeId);
  }


  public class Warp implements ILock, AutoCloseable {
    public final String name;
    private final ILock l;


    private Warp(String name, ILock local) {
      this.name = name;
      this.l = local;
    }


    @Override
    public void lock() throws RemoteException {
      l.lock();
    }


    @Override
    public void lockInterruptibly() throws InterruptedException, RemoteException {
      l.lockInterruptibly();
    }


    @Override
    public boolean tryLock() throws RemoteException {
      return l.tryLock();
    }


    @Override
    public boolean tryLock(long ms) throws InterruptedException, RemoteException {
      return l.tryLock(ms);
    }


    @Override
    public void unlock() throws RemoteException {
      l.unlock();
    }


    @Override
    public void close() throws Exception {
      l.unlock();
    }
  }
}
