////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-3 下午2:42
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/mongo/SysMongoFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.xboson.been.MongoConfig;
import com.xboson.been.XBosonException;
import com.xboson.event.OnExitHandle;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;


/**
 * 创建全局唯一平台级 mongodb 连接
 */
public class SysMongoFactory extends OnExitHandle {

  public static final String DEFAULT_DISK = "disk";

  private static SysMongoFactory instance;

  private MongoConfig mc;
  private MongoClient cli;


  public static SysMongoFactory me() {
    if (instance == null) {
      synchronized (SysMongoFactory.class) {
        if (instance == null) {
          instance = new SysMongoFactory();
        }
      }
    }
    return instance;
  }


  private SysMongoFactory() {
    mc = SysConfig.me().readConfig().mongodb;
    if (!mc.enable)
      throw new XBosonException("MongoDB disabled");

    cli = new MongoClient(mc.address(), mc.credential(), mc.options());
  }


  @Override
  protected void exit() {
    synchronized (SysMongoFactory.class) {
      Tool.close(cli);
      cli = null;
      mc = null;
      instance = null;
    }
  }


  /**
   * 用默认磁盘名打开 mongodb 上的文件系统
   */
  public MongoFileSystem openFS() {
    return openFS(DEFAULT_DISK);
  }


  /**
   * 打开 mongodb 上的文件系统
   */
  public MongoFileSystem openFS(String diskName) {
    MongoDatabase db = cli.getDatabase(mc.database);
    return new MongoFileSystem(db, diskName);
  }
}
