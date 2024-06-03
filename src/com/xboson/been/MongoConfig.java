////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-3 下午2:28
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/MongoConfig.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.util.Collections;
import java.util.List;


/**
 * 创建客户端需要的配置
 */
public class MongoConfig {

  public static final int TIMEOUT = 3000;

  public String host;
  public int port;
  public String database;
  public String username;
  public String password;
  public boolean enable;


  public ServerAddress address() {
    return new ServerAddress(host, port);
  }


  public List<MongoCredential> credential() {
    char[] ps;
    if (password != null) {
      ps = password.toCharArray();
    } else if (username == null) {
      return Collections.emptyList();
    } else {
      ps = null;
    }
    return Collections.singletonList(
            MongoCredential.createCredential(username, database, ps));
  }


  public MongoClientOptions options() {
    MongoClientOptions.Builder opt = MongoClientOptions.builder();
    opt.connectTimeout(TIMEOUT);
    opt.serverSelectionTimeout(TIMEOUT);
    return opt.build();
  }
}
