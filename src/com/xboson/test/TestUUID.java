////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-12 下午5:28
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestUUID.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.script.lib.Uuid;

import java.util.UUID;


public class TestUUID extends Test {

  public void test() throws Throwable {
    check();
    generate1vs4();
    show();
  }


  public void check() {
    Uuid uid = new Uuid();
    final int count = 10000;

    beginTime();
    for (int i=0; i<count; ++i) {
      UUID src  = uid.v4obj();
      String ds = uid.ds(src);
      UUID pds  = uid.parseDS(ds);
      String z  = uid.zip(src);
      UUID uz   = uid.unzip(z);

      eq(ds.length(), 32, "ds length 32");
      eq(pds, src, "parse ds");
      eq(uz, src, "zip uuid");
    }
    endTime("Generate ds/parse ds/zip/unzip UUID", count, "counts");
  }


  public void show() {
    Uuid uid = new Uuid();

    sub("v1()");
    for (int i=0; i<5; ++i) {
      msg("v1: " + uid.v1());
    }

    sub("v4()");
    for (int i=0; i<5; ++i) {
      msg("v4: " + uid.v4());
    }


    UUID id = uid.v4obj();
    String ds = uid.ds(id);
    UUID pds = uid.parseDS(ds);
    String z = uid.zip(id);
    UUID uz = uid.unzip(z);

    sub("strings:");
    msg("UUID : ", id, "[ version:", id.version(),
            "length:", id.toString().length(), "]");
    msg("DS   : ", ds, "[ length:", ds.length(), "]");
    msg("PDS  : ", pds);
    msg("ZIP  : ", z, "[ length:", z.length(), "]");
    msg("UNZIP: ", uz);

//    msg(id.timestamp(), id.variant(), id.version());
  }


  public void generate1vs4() {
    Uuid uid = new Uuid();
    final int count = 100000;

    beginTime();
    for (int i=count; i>=0; --i) {
      uid.v1();
    }
    endTime("Generate v1 UUID", count, "counts");

    beginTime();
    for (int i=count; i>=0; --i) {
      uid.v4();
    }
    endTime("Generate v4 UUID", count, "counts");
  }

  public static void main(String[] a) {
    new TestUUID();
  }
}
