////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-30 上午11:21
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/rpc/RpcFactory.java
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
import com.xboson.util.SysConfig;

import javax.rmi.PortableRemoteObject;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;


 /**
 * 默认会绑定 Ping 对象, 可以在远程进行测试.
 * 连接需要安全认证.
 * 大部分方法都会抛出 XBosonException.Remote 异常, 必要时需要检查.
 *
 * @see XBosonException.Remote
 */
public final class RpcFactory extends OnExitHandle {

  private static RpcFactory instance;

  private final String myselfNodeID;
  private SafeClientFactory clientf;
  private SafeServerFactory serverf;
  private Registry server;
  private Map<String, RegistryData> registryCache;
  private Log log;
  private NodeUpdate nu;

  /** RMI 创建的存根对象对本地对象是弱引用, 必须将实例强引用, 否则被 GC */
  private Map<String, IXRemote> ref;


  private RpcFactory() {
    try {
      Config cf = SysConfig.me().readConfig();
      String ps = cf.sessionPassword;

      this.clientf        = new SafeClientFactory(ps);
      this.serverf        = new SafeServerFactory(ps);
      this.server = LocateRegistry.createRegistry(0, clientf, serverf);
      this.registryCache  = new HashMap<>();
      this.ref            = new HashMap<>();
      this.log            = LogFactory.create();

      myselfNodeID = ClusterManager.me().localNodeID();
      bind(new Ping());
      attentionNodeUpdateEvent();

    } catch (RemoteException e) {
      throw new XBosonException.Remote(e);
    }
  }


  public static RpcFactory me() {
    if (instance == null) {
      synchronized (RpcFactory.class) {
        if (instance == null) {
          instance = new RpcFactory();
        }
      }
    }
    return instance;
  }


  private void attentionNodeUpdateEvent() {
    nu = new NodeUpdate() {
      @Override
      protected void onChange(String nodeID) {
        updateNodeRegistryCache(nodeID);
      }
    };
  }


  /**
   * 获取远程对象
   *
   * @param nodeID 远程节点 id
   * @param clazz 使用类型名称在注册表中寻找对象
   * @return 远程对象
   * @see #lookup(String, String)
   */
  public <E extends IXRemote> E lookup(String nodeID, Class<E> clazz) {
    XBosonException.NullParamException.check(clazz, "Class clazz");
    String name = clazz.getName();
    return (E) lookup(nodeID, name);
  }


  /**
   * 获取远程对象(或本地对象), 如果远程对象会在某时刻从注册表删除, 在该方法返回的对象
   * 上调用远程方法会抛出 NotBoundException 异常, 此时远程对象应该实现 IPing 接口
   * 并可以继承默认实现 Ping, 该方法返回前会处理对象同步, 并且正确的抛出异常.
   *
   * @param nodeID 远程节点 id
   * @param name 注册表中的对象名称
   * @return 远程对象
   * @see IPing
   * @see Ping IPing 的默认实现
   */
  public synchronized Remote lookup(String nodeID, String name) {
    try {
      XBosonException.NullParamException.check(nodeID, "String node");
      XBosonException.NullParamException.check(name, "String name");

      if (myselfNodeID.equals(nodeID)) {
        return server.lookup(name);
      }

      RegistryData data = findRegistryWithCache(nodeID);
      Remote remote = data.getOrCreate(name);

      if (remote instanceof IPing) {
        ((IPing) remote).ping();
      }
      return remote;

    } catch (ConnectException | NotBoundException e) {
      updateNodeRegistryCache(nodeID);
      throw new XBosonException.Remote(e);

    } catch (RemoteException e) {
      throw new XBosonException.Remote(e);
    }
  }


  /**
   * 使用 remote 的类型名作为注册表名
   * @see #bind(IXRemote, String)
   */
  public void bind(IXRemote remote) {
    String name = getName(remote);
    bind(remote, name);
  }


   /**
    * 同一个名称的服务只绑定一次.
    * @param remote 服务
    * @param name 名称
    * @return 初次绑定返回 true
    */
  public synchronized boolean bindOnce(IXRemote remote, String name) {
    if (! isBind(name)) {
      bind(remote, name);
      return true;
    }
    return false;
  }


  /**
   * 检查注册表上是否已经有命名对象
   *
   * @param name 注册表对象名
   * @return 如果注册表已经有该名称的对象返回 true
   */
  public boolean isBind(String name) {
    return ref.containsKey(name);
  }


  /**
   * 使用 remote 的类型名作为注册表名
   * @see #isBind(String)
   */
  public boolean isBind(IXRemote remote) {
    return isBind(getName(remote));
  }


  /**
   * 将本地对象绑定在注册表上, 远程节点既可以调用这个绑定的方法.
   *
   * @param remote 本地对象实现了 IXRemote 接口即可导出
   * @param name 绑定到注册表的名字
   */
  public synchronized void bind(IXRemote remote, String name) {
    Remote stub = null;
    try {
      stub = export(remote);
      server.bind(name, stub);
      ref.put(name, remote);
    } catch (RemoteException | AlreadyBoundException e) {
      unexport(stub);
      throw new XBosonException.Remote(e);
    }
  }


  /**
   * 使用 remote 的类型名作为注册表名
   * @see #unbind(String)
   */
  public Remote unbind(IXRemote remote) {
    return unbind(getName(remote));
  }


  /**
   * 取消注册在注册表上的对象, (这回关闭底层 tcp 连接)
   *
   * @param name 对象名称
   * @return 返回解除绑定的对象
   */
  public synchronized Remote unbind(String name) {
    try {
      server.unbind(name);
      return unexport(ref.remove(name));
    } catch (RemoteException | NotBoundException e) {
      throw new XBosonException.Remote(e);
    }
  }


  /**
   * 使用 remote 的类型名作为注册表名
   * @see #rebind(IXRemote, String)
   */
  public Remote rebind(IXRemote remote) {
    return rebind(remote, getName(remote));
  }


  /**
   * 重新绑定对象, 如果重绑定对象与原先对象相同则什么都不做并返回 null,
   * 否则返回被覆盖的原始对象.
   *
   * @param remote 绑定对象
   * @param name 注册表名称
   * @return 被覆盖的对象
   */
  public synchronized Remote rebind(IXRemote remote, String name) {
    Remote old = ref.remove(name);
    if (old == remote) return null;
    Remote stub = null;
    try {
      unexport(old);
      stub = export(remote);
      server.rebind(name, stub);
      ref.put(name, remote);
      return old;
    } catch (RemoteException e) {
      unexport(stub);
      throw new XBosonException.Remote(e);
    }
  }


  /**
   * 返回本地注册表绑定的对象名称列表
   */
  public synchronized String[] list() {
    try {
      return server.list();
    } catch (RemoteException e) {
      throw new XBosonException.Remote(e);
    }
  }


  /**
   * 返回远程注册表上绑定的对象名称列表
   * @param nodeID 远程节点 ID
   */
  public synchronized String[] list(String nodeID) {
    try {
      RegistryData data = findRegistryWithCache(nodeID);
      return data.reg.list();
    } catch (RemoteException e) {
      throw new XBosonException.Remote(e);
    }
  }


  private String getName(Object o) {
    return o.getClass().getName();
  }


  private Remote export(Remote r) throws RemoteException {
    return UnicastRemoteObject.exportObject(r, 0, clientf, serverf);
  }


  private Remote unexport(Remote r) {
    if (r != null) {
      freeRemote(r);
    }
    return r;
  }


  @Override
  protected synchronized void exit() {
    for (Map.Entry<String, RegistryData> entry : registryCache.entrySet()) {
      entry.getValue().free();
    }

    for (Map.Entry<String, IXRemote> entry : ref.entrySet()) {
      freeRemote(entry.getValue());
    }

    freeRegistry(server);

    registryCache.clear();
    ref.clear();
    registryCache = null;
    ref = null;
    server = null;
    nu.removeFileListener();
  }


  public void freeRegistry(Registry reg) {
    try {
      for (String name : reg.list()) {
        try {
          reg.unbind(name);
        } catch (Exception e) {
          log.warn("Free Registry bind", name, e);
        }
      }
    } catch (Exception e) {
      log.warn("Free Registry list", reg, e);
    }

    try {
      PortableRemoteObject.unexportObject(reg);
    } catch (Exception e) {
      log.warn("Free Registry", reg, e);
    }

    log.debug("freeRegistry", reg);
  }


  public void freeRemote(Remote remote) {
    try {
      UnicastRemoteObject.unexportObject(remote, true);
    } catch (NoSuchObjectException e) {
      log.warn("Free Remote", remote, e);
    }

    log.debug("freeRemote", remote);
  }


  /**
   * 查询运算节点上的注册表, 找不到返回 null.
   */
  private Registry findRegistry(String nodeID) {
    ComputeNodeInfo info = ClusterManager.me().info(nodeID);
    if (info == null)
      throw new XBosonException("Not Found Node: "+ nodeID);

    Registry reg = null;

    for (String host : info.ip) {
      try {
        reg = LocateRegistry.getRegistry(host, info.rpcPort, clientf);
        break;
      } catch (RemoteException e) { /* skip */ }
    }
    return reg;
  }


  /**
   * 创建远程注册表对象缓存
   */
  private RegistryData createRemoteCache(String nodeID, Registry reg) {
    RegistryData data = new RegistryData(reg);
    registryCache.put(nodeID, data);
    return data;
  }


  private RegistryData findRegistryWithCache(String nodeID)
          throws ConnectException
  {
    RegistryData data = registryCache.get(nodeID);

    if (data == null) {
      Registry reg = findRegistry(nodeID);

      if (reg == null)
        throw new ConnectException("Cannot connect to NODE: "+ nodeID);

      data = createRemoteCache(nodeID, reg);
    }
    return data;
  }


  private void updateNodeRegistryCache(String nodeID) {
    RegistryData data = registryCache.remove(nodeID);
    if (data != null) data.free();
  }


  /**
   * 保存远程注册表, 和注册表中的对象.
   */
  private class RegistryData {
    private final Registry reg;
    private final Map<String, Remote> objCache;

    private RegistryData(Registry reg) {
      this.reg = reg;
      this.objCache = new WeakHashMap<>();
    }

    private void free() {
      // freeRegistry(reg); // 这将改变服务端注册表
      objCache.clear();
    }

    /**
     * 获取注册表中的对象, 对象可以来自缓存, 或创建新的远程引用.
     *
     * @param objectName 对象在注册表中的名字
     * @return 远程对象
     * @throws RemoteException
     * @throws NotBoundException
     */
    Remote getOrCreate(String objectName)
            throws RemoteException, NotBoundException
    {
      Remote remote = objCache.get(objectName);
      if (remote == null) {
        remote = reg.lookup(objectName);
        objCache.put(objectName, remote);
      }
      return remote;
    }
  }
}
