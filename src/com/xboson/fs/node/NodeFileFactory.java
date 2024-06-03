////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-22 下午12:25
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/node/NodeFileFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.node;

import com.xboson.been.Config;
import com.xboson.event.EventLoop;
import com.xboson.event.timer.EarlyMorning;
import com.xboson.fs.redis.*;
import com.xboson.script.IConfigurableModuleProvider;
import com.xboson.script.IModuleProvider;


public final class NodeFileFactory extends AbsFactory {

  private static NodeFileFactory instance;


  /**
   * 打开 node 文件系统
   */
  public static IRedisFileSystemProvider open() {
    return me().__open();
  }


  private synchronized static NodeFileFactory me() {
    if (instance == null) {
      instance = new NodeFileFactory();
    }
    return instance;
  }


  /**
   * 使用当前 node 文件系统创建模块读取器
   * @param parent 只当父级模块返回 null 时才从文件系统中创建.
   * @return
   */
  public synchronized static IConfigurableModuleProvider
        openNodeModuleProvider(IModuleProvider parent)
  {
    IRedisFileSystemProvider fs = open();
    IFileSystemConfig config = me().getConfig();
    NodeModuleProvider node = new NodeModuleProvider(fs, config, parent);
    return node;
  }


  @Override
  protected IRedisFileSystemProvider createLocal(
          Config cf, IFileSystemConfig config) {
    RedisBase rb              = new RedisBase(config);
    RedisFileMapping rfm      = new NodeRedisFileMapping(rb);
    LocalFileMapping local    = new NodeLocalFileMapping(rfm, rb);

    SynchronizeFiles sf = new SynchronizeFiles(rb, rfm);
    EventLoop.me().add(sf);
    if (cf.enableNodeFileSync) {
      EarlyMorning.add(sf);
    }
    return local;
  }


  @Override
  protected IRedisFileSystemProvider createOnline(
          Config cf, IFileSystemConfig config) {
    RedisBase rb              = new RedisBase(config);
    RedisFileMapping rfm      = new NodeRedisFileMapping(rb);
    return rfm;
  }


  @Override
  protected IFileSystemConfig createConfig(Config cf) {
    return new NodeFileSystemConfig(cf.nodeUrl);
  }


  private NodeFileFactory() {}
}
