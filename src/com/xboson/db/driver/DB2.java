////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 上午10:35
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/driver/DB2.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db.driver;

import com.xboson.db.ConnectConfig;
import com.xboson.db.IDriver;
import com.xboson.db.NullDriver;


public class DB2 extends NullDriver implements IDriver {

  @Override
  public String driverClassName() {
    return "com.ibm.db2.jcc.DB2Driver";
  }


  @Override
  public String name() {
    return "db2";
  }


  @Override
  public int id() {
    return 4;
  }


  @Override
  public String getUrl(ConnectConfig config) {
    return "jdbc:db2://"
            + config.getHost() + ':'
            + config.getPort() + '/'
            + config.getDatabase();
  }


  @Override
  public int port() {
    return 0;
  }


  @Override
  public String nowSql() {
    return "SELECT current timestamp AS _now_ FROM sysibm.sysdummy1";
  }
}
