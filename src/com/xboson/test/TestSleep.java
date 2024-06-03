////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-11 下午12:48
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestSleep.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.sleep.*;
import com.xboson.util.Tool;

import java.io.Serializable;


public class TestSleep extends Test {

  IMesmerizer im;


  public void test() {
    config_factory();
    test_bin();
    test_json();
    test_timeout();
  }


  public void test_timeout() {
    sub("Test Timeout interface");
    String id = Test.randomString(10);

    JsonData c = new JsonData();
    c.change();
    c.id = id;
    c.timeout = 100;
    c.savepoint = System.currentTimeMillis();

    im.sleep(c);
    Tool.sleep(500);

    JsonData d = (JsonData) im.wake(c.getClass(), id);
    eq(null, d, "is timeout");

    msg("timeout working");
  }


  public void test_bin() {
    sub("Test sleep bin");
    String id = Test.randomString(10);

    BinData c = new BinData();
    c.change();
    c.id = id;
    im.sleep(c);
    BinData d = (BinData) im.wake(c.getClass(), id);
    eq(c, d, "bin data");
    msg("bin data: " + d);

    im.remove(d);
    Object nul = im.wake(c.getClass(), id);
    eq(null, nul, "data removed");
  }


  public void test_json() {
    sub("Test sleep JSON");
    String id = Test.randomString(10);

    JsonData a = new JsonData();
    a.change();
    a.id = id;
    a.timeout = Long.MAX_VALUE;
    im.sleep(a);
    JsonData b = (JsonData) im.wake(a.getClass(), id);
    eq(a, b, "json data");
    msg("json data: " + b);

    im.remove(b);
    Object nul = im.wake(a.getClass(), id);
    eq(null, nul, "data removed");
  }


  public void config_factory() {
    sub("Config sleep factory");

    RedisMesmerizer rm = RedisMesmerizer.me();
    SleepFactory sf = SleepFactory.me();
    sf.configDefault(rm);
    im = rm;
  }


  static public class JsonData extends TData
          implements IJsonData, Serializable, ITimeout {
    public long timeout;
    public long savepoint;

    @Override
    public String getid() {
      return id;
    }
    @Override
    public boolean isTimeout() {
      return System.currentTimeMillis() - savepoint > timeout;
    }
  }


  static public class BinData extends TData
          implements IBinData, Serializable, ITimeout {
    boolean timeout = false;

    @Override
    public String getid() {
      return id;
    }
    public boolean isTimeout() {
      return timeout;
    }
  }


  public static void main(String[] a) throws Throwable {
    new TestSleep();
  }
}
