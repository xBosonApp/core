////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-11 上午11:18
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestRedis.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.been.SessionData;
import com.xboson.sleep.RedisMesmerizer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;

import static javax.swing.UIManager.get;


public class TestRedis extends Test {

  public void test() throws Exception {
    test_pool();
    test_remove_all();
  }


  public void test_remove_all() throws Exception {
    sub("RedisMesmerizer removeAll");
    SessionData sd = new SessionData();
    RedisMesmerizer.me().removeAll(sd);
  }


  public void test_pool() throws Exception {
    sub("Test Pool");

    JedisPoolConfig config = new JedisPoolConfig();
    JedisPool pool = new JedisPool(config, "localhost");

    try (Jedis client = pool.getResource()) {
      String random = randomString(100);
      client.set("test", random);

      String b = client.get("test");
      msg("String: " + random);
      ok(random.equals(b), "get / set");

      byte[] c1 = randomBytes(100);
      byte[] n = "bin".getBytes();
      client.set(n, c1);
      byte[] c2 = client.get(n);
      ok(Arrays.equals(c1, c2), "bin data");
    }

    pool.destroy();
  }


  public static void main(String[] a) throws Exception {
    new TestRedis();
  }
}
