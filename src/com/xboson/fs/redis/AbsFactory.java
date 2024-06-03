////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-22 下午12:29
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/redis/AbsFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.redis;

import com.xboson.been.Config;
import com.xboson.been.XBosonException;
import com.xboson.util.SysConfig;


/**
 * 方便创建文件系统工厂
 */
public abstract class AbsFactory {

  private IRedisFileSystemProvider current;
  private IFileSystemConfig config;
  private final Config cf;


  public AbsFactory() {
    cf = SysConfig.me().readConfig();
  }


  /**
   * 使用配置文件中定义的参数创建全局唯一 ui 读取器.
   */
  protected synchronized IRedisFileSystemProvider __open() {
    if (current == null) {
      config    = getConfig();
      current   = create();
    }
    return current;
  }


  private IRedisFileSystemProvider create() {
    switch (cf.uiProviderClass) {
      case "local":
        return createLocal(cf, config);

      case "online":
        return createOnline(cf, config);

      default:
        throw new XBosonException.NotImplements(
                "File system type: " + cf.uiProviderClass);
    }
  }


  public IFileSystemConfig getConfig() {
    if (config == null) {
      config = createConfig(cf);
    }
    return config;
  }


  /**
   * 创建本地文件系统
   */
  protected abstract IRedisFileSystemProvider createLocal(
          Config cf, IFileSystemConfig config);


  /**
   * 创建在线文件系统
   */
  protected abstract IRedisFileSystemProvider createOnline(
          Config cf, IFileSystemConfig config);


  /**
   * 创建配置文件, 该方法保证只调用一次
   */
  protected abstract IFileSystemConfig createConfig(Config cf);
}
