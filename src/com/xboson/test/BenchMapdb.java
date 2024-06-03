////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-10-10 上午9:49
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/BenchMapdb.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.chain.BlockBasic;
import com.xboson.util.Tool;
import org.mapdb.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class BenchMapdb {

  static final int COUNT = 100000000;
  static final String FILE = "d:/mapdb.test";


  /**
   * 写入 2ms/数据块, 读取 <1ms/数据块, 15万数据 100MB.
   */
  public static void main(String[] av) throws Exception {
    Test.msg("模拟区块链数据库插入区块, 长度", COUNT);

    DB db = DBMaker.fileDB(FILE)
            .transactionEnable()
            .make();

    HTreeMap map = db.hashMap("t1")
            .keySerializer(Serializer.STRING)
            .valueSerializer(Serializer.JAVA)
            .createOrOpen();

    fileinfo(map);
    long t = Test.beginTime();
    String pkey = "";

    for (int i=0; i<COUNT; ++i) {
      String key = Tool.uuid.v4();
      if (System.currentTimeMillis() - t > 5e3) {
        t = System.currentTimeMillis();

        long ct = t;
        db.commit();
        ct = System.currentTimeMillis() - ct;
        long u = Test.endTime("Write", i, "Commit use", ct, "ms");

        long gett = System.currentTimeMillis();
        map.get(pkey);
        gett = System.currentTimeMillis() - gett;
        Test.msg("Get", pkey, "use", gett, "ms");
        Test.msg(u/i, "ms/block");
        pkey = key;
      }

      BlockBasic bb = new BlockBasic(
              Tool.randomBytes(32),
              Tool.randomString(10),
              Tool.randomBytes(32));
      map.put(key, bb.createBlock());
    }

    fileinfo(map);
    db.close();
    Test.endTime("Over");
  }


  private static void fileinfo(HTreeMap map) throws IOException {
    long size = Files.size(Paths.get(FILE));
    int total = map.size();
    Test.msg("Total", total, "block, File size",
            size/(1024*1024), "MB", size/total, "byte/block");
  }
}
