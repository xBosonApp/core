////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-22 下午12:57
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/init/install/step/ConfigMongoDB.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.init.install.step;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoIterable;
import com.xboson.been.MongoConfig;
import com.xboson.init.install.HttpData;
import com.xboson.init.install.IStep;


public class ConfigMongoDB implements IStep {

  @Override
  public int order() {
    return 8;
  }


  @Override
  public boolean gotoNext(HttpData data) throws Exception {
    MongoConfig mdb = data.cf.mongodb;

    if (data.getBool("skip")) {
      mdb.enable = false;
      return true;
    }

    mdb.enable = true;
    mdb.host = data.getStr("host");
    mdb.port = data.getInt("port");
    mdb.username = data.getStr("username");
    mdb.password = data.getStr("password");
    mdb.database = data.getStr("database");

    try (MongoClient cli = new MongoClient(
            mdb.address(), mdb.credential(), mdb.options())) {
      MongoIterable<String> mi = cli.listDatabaseNames();
      return mi.first() != null;
    }
  }


  @Override
  public String getPage(HttpData data) {
    return "mangodb.jsp";
  }
}
