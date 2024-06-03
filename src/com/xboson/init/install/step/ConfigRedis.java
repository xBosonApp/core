////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-19 上午9:29
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/init/install/step/ConfigRedis.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.init.install.step;

import com.xboson.db.ConnectConfig;
import com.xboson.init.install.HttpData;
import com.xboson.init.install.IStep;
import com.xboson.util.Tool;
import redis.clients.jedis.Jedis;


public class ConfigRedis implements IStep {

  @Override
  public int order() {
    return 4;
  }


  @Override
  public boolean gotoNext(HttpData data) {
    ConnectConfig redis = data.cf.redis;
    redis.setHost(data.req.getParameter("rhost"));
    redis.setPort(data.req.getParameter("rport"));
    redis.setPassword(data.req.getParameter("rpassword"));

    final int port = redis.getIntPort(6379);
    final String host = redis.getHost();

    try (Jedis jc = new Jedis(host, port)) {
      if (!Tool.isNulStr(redis.getPassword())) {
        jc.auth(redis.getPassword());
      }
      jc.ping();
      return true;

    } catch(Exception e) {
      data.msg = e.getMessage();
      e.printStackTrace();
    }

    return false;
  }


  @Override
  public String getPage(HttpData data) {
    return "redis.jsp";
  }
}
