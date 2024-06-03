////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-3 下午1:22
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestMongo.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.squareup.moshi.JsonAdapter;
import com.xboson.app.lib.ModuleHandleContext;
import com.xboson.fs.mongo.MongoFileAttr;
import com.xboson.fs.mongo.MongoFileSystem;
import com.xboson.fs.mongo.SysMongoFactory;
import com.xboson.fs.script.IScriptFileSystem;
import com.xboson.script.Application;
import com.xboson.util.CloseableSet;
import com.xboson.util.Tool;

import java.io.InputStream;
import java.io.OutputStream;


public class TestMongo extends Test {


  @Override
  public void test() throws Throwable {
    basic();
    fs();
    script();
  }


  // 需要模拟一个 servlet 上下文, 否则测试错误
  public void script() throws Throwable {
    sub("Script Module");
    new ModuleHandleContext().init();
    ModuleHandleContext.register(ModuleHandleContext.CLOSE, new CloseableSet());

    IScriptFileSystem vfs = TestScript.createVFS();
    Application app = TestScript.createBasicApplication(vfs);
    app.run("/mongo-db.js");
  }


  public void basic() throws Throwable {
    sub("Basic");
    //
    // 该MongoClient实例表示一个到数据库的连接池;
    // MongoClient即使有多个线程，您也只需要一个类的实例。
    //
    try (MongoClient cli = new MongoClient()) {
      MongoDatabase db = cli.getDatabase("test");
      MongoCollection cl = db.getCollection("c1");
      JsonAdapter a = Tool.getAdapter(Object.class);
      Object list = cli.listDatabaseNames();
      msg(list);
      cl.count();
    }
  }


  public void fs() throws Throwable {
    sub("File System");

    MongoFileSystem fs = SysMongoFactory.me().openFS();

    sub("Make dir");
    fs.makeDir("/a/b/c");

    sub("Write file");
    String filename = "/a/test.txt";
    String txt = writeRandomFile(fs, filename);

    sub("Write file 2");
    writeRandomFile(fs, "/c/1.txt");
    writeRandomFile(fs, "/2.txt");
    writeRandomFile(fs, "/3.txt");

    sub("Read dir");
    msg("/a", fs.readDir("/a"));
    msg("/", fs.readDir("/"));

    sub("Read file");
    InputStream i = fs.openInputStream(filename);
    byte[] buf = new byte[1000];
    int len = i.read(buf);
    String rtext = new String(buf, 0, len);
    eq(txt, rtext, "Read file from mongo");
    msg("OK", rtext);

    MongoFileAttr attr = fs.readAttribute("/test.txt");
    msg("File Attribute:", attr);
  }


  public String writeRandomFile(MongoFileSystem fs, String file)
          throws Exception {
    OutputStream out = fs.openOutputStream(file);
    String txt = Tool.randomString(100);
    out.write(txt.getBytes());
    out.close();
    return txt;
  }


  public static void main(String[] a) {
    new TestMongo();
  }
}
