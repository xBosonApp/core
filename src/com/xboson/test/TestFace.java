////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-19 上午10:40
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestFace.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.been.Config;
import com.xboson.been.XBosonException;
import com.xboson.event.EventLoop;
import com.xboson.fs.redis.*;
import com.xboson.fs.ui.UIFileSystemConfig;
import com.xboson.fs.ui.UILocalFileMapping;
import com.xboson.fs.ui.UIRedisFileMapping;
import com.xboson.log.Level;
import com.xboson.log.LogFactory;
import com.xboson.sleep.LuaScript;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import redis.clients.jedis.Jedis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;


/**
 * UI-FS 测试
 * 如果有其他的集群节点连接了 redis, 测试会失败.
 * 启动这个测试前, 必须保证本地文件已经同步到 redis.
 */
public class TestFace extends Test {

  private String path;
  private byte[] content;

  private UIFileSystemConfig config;
  private LocalFileMapping local;
  private RedisFileMapping redis;
  private RedisBase rb;


  public void test() throws Throwable {
    sub("Test UI file system");

    Config cf = SysConfig.me().readConfig();
    config = new UIFileSystemConfig(cf.uiUrl);
    rb = new RedisBase(config);

    redis = new UIRedisFileMapping(rb);
    local = new UILocalFileMapping(redis, rb);
    path  = "/ui/paas/login.html";
    LogFactory.setLevel(Level.ALL);

    //
    // 直接在当前线程中执行同步
    //
    SynchronizeFiles sf = new SynchronizeFiles(rb, redis);
    sf.run();

    waitEventLoopEmpty();
    testLocal();
    test_redis_base();
    local_and_redis();
    read_dir();
    test_move();
    test_lua();
    test_find();
    test_find_path();
  }


  public static void waitEventLoopEmpty() {
    sub("Wait event loop empty...");
    boolean[] check = new boolean[1];
    EventLoop.me().add(() -> check[0] = true);
    while (check[0] != true) {
      waitDelayed();
    }
    msg("OK, Event loop empty.");
  }


  public void test_find_path() {
    sub("Test find path");

    beginTime();
    FinderResult fr = redis.findPath("/t/paas/md");
    endTime(fr);
    msg(fr.files);
  }


  public void test_lua() {
    sub("Test lua base");
    LuaScript find = LuaScript.compile("return 'ok';");
    msg("LUA1:", find.eval());

    //
    // 删除脚本之后也能保证可以调用
    //
    try (Jedis client = RedisMesmerizer.me().open()) {
      client.scriptFlush();
    }

    msg("LUA2:", find.eval());
  }


  public void test_find() {
    sub("Test find function in lua.");
    FindContentInRedisWithLua find = new FindContentInRedisWithLua(config);

    finds(find, true, "Register", "register",
            "请输入验证码", "background", "系统信息");

    finds(find, false, "register", "系统信息");

    // 带缓存
    finds(find, false, "register", "系统信息");
  }


  public void finds(FindContentInRedisWithLua find,
          boolean caseSensitive, String ...what) {

    String basedir = "/t";

    for (int i=0; i<what.length; ++i) {
      String str = what[i];
      beginTime();
      FinderResult r =
              find.find(basedir, str, caseSensitive);
      endTime(r);
      eq(caseSensitive, r.caseSensitive, "case sensitive");
      eq(basedir, r.baseDir, "base dir");
      int len = Math.min(5, r.files.size());
      msg("Find", r.files.subList(0, len), "...");
    }
  }


  public void test_move() {
    sub("Test move");
    final byte[] s1 = randomString(100).getBytes(CHARSET);
    final byte[] s2 = randomString(200).getBytes(CHARSET);

    del("/t2/s1.txt", "/t2/t3/t4/s2.txt", "/t2/t3/t4",
            "/t2/t3", "/t2");

    del("/m4/s1.txt", "/m4/t3/t4/s2.txt",
            "/m4/t3/t4", "/m4/t3", "/m4");

    redis.makeDir("/t2");
    redis.makeDir("/t2/t3/t4");
    redis.writeFile("/t2/s1.txt", s1);
    redis.writeFile("/t2/t3/t4/s2.txt", s2);

    waitDelayed();
    redis.move("/t2", "/m4");
    msg("MOVE /t2 TO /m4");

    waitDelayed();
    file_eq(s1, "/m4/s1.txt");
    file_eq(s2, "/m4/t3/t4/s2.txt");
    dir_eq("/m4");
    dir_eq("/m4/t3");
    dir_eq("/m4/t3/t4");

    notExists("/t2/s1.txt", "/t2/t3/t4/s2.txt",
            "/t2/t3/t4", "/t2/t3", "/t2");

    del("/m4/s1.txt", "/m4/t3/t4/s2.txt",
            "/m4/t3/t4", "/m4/t3", "/m4");
  }


  /**
   * 写入的文件不是立即就可以读取, 消息传递有延迟
   */
  private static void waitDelayed() {
    Tool.sleep(1000);
  }


  /**
   * 从两个系统中读取文件, 必须与 content 相同
   */
  public void file_eq(byte[] content, String fileName) {
    byte[] s1 = redis.readFile(fileName);
    byte[] s2 = local.readFile(fileName);
    ok(Arrays.equals(s1, content), "redis file ok");
    ok(Arrays.equals(s2, content), "local file ok");
  }


  public void notExists(String ...files) {
    for (String file : files) {
      RedisFileAttr fs1 = redis.readAttribute(file);
      RedisFileAttr fs2 = local.readAttribute(file);
      ok(fs1 == null, "Redis no file:" + file);
      ok(fs2 == null, "Local no file:" + file);
    }
  }


  /**
   * 删除若干文件, 无视错误
   */
  public void del(String ...files) {
    for (String name : files) {
      try {
        redis.delete(name);
      } catch (Exception e) {
        msg("DEBUG: Delete", name, e);
      }
    }
  }


  /**
   * 从两个系统中读取目录, 目录内容相同测试正确, 否则抛出异常
   */
  public void dir_eq(String dir) {
    Set<RedisFileAttr> ls = local.readDir(dir);
    Set<RedisFileAttr> rs = redis.readDir(dir);
    try {
      ok(ls != null, "local dir");
      ok(rs != null, "redis dir");

      if (ls.containsAll(rs) == false || rs.containsAll(ls) == false) {
        throw new AssertionError("same dir not equals\n\tObject 1: '" + ls +
                "'\n\tObject 2: '" + rs + "'");
      }
      msg("OK same dir", dir);
    } catch (AssertionError t) {
      msg("Local:", ls);
      msg("Redis:", rs);
      throw t;
    }
  }


  public void read_dir() {
    sub("Read dirs");
    dir_eq("/t/paas");
  }


  public void local_and_redis() throws Throwable {
    sub("Write use redis, read from local");

    String test_dir = "/test/";
    String test_file = test_dir + "/a.js";
    byte[] content = randomString(100).getBytes();
    redis.makeDir("/test/");
    redis.writeFile(test_file, content);

    waitDelayed();

    byte[] r = local.readFile(test_file);
    ok(Arrays.equals(content, r), "content");

    Path p = local.normalize(test_file);
    byte[] r2 = Files.readAllBytes(p);
    ok(Arrays.equals(content, r2), "from fs");
    msg("content:", new String(r2));

    delete_non_empty_dir(test_dir);

    waitDelayed();
    redis.delete(test_file);
    redis.delete(test_dir);
  }


  /**
   * dir 是非空目录, 删除会抛出异常, 则完成测试, 若没有异常则是系统错误
   */
  public void delete_non_empty_dir(String dir) {
    sub("Check delete non-empty DIR", dir);
    boolean checkNon = false;
    try {
      redis.delete(dir);
    } catch (XBosonException.IOError io) {
      checkNon = io.toString().indexOf("non-empty dir") >= 0;
    }
    if (checkNon) {
      msg("OK, check non-empty dir");
    } else {
      fail("Delete non-empty dir");
    }
    ok(null != redis.readDir(dir), "redis not delete");
    ok(null != local.readDir(dir), "local not delete");
  }


  public void testLocal() throws Throwable {
    sub("Local file system");

    content = local.readFile(path);
    String s = new String(content);
    ok(s.indexOf("云平台登录")>=0, "html file read");
    ok(s.indexOf("忘记密码")>=0, "check ok");
  }


  public void test_redis_base() throws Throwable {
    sub("Test UI Redis base");

    RedisFileAttr fs = RedisFileAttr.createFile(path, 0, content);
    rb.setContent(fs);
    rb.getContent(fs);
    ok(Arrays.equals(fs.getFileContent(), content), "read/write redis");

    final Thread curr = Thread.currentThread();
    final boolean[] check = new boolean[1];

    FileModifyHandle fmh = new FileModifyHandle(new NullModifyListener() {
      public void noticeModifyContent(String file) {
        eq(path, file, "recive modify notice");
        check[0] = true;
        curr.interrupt(); // 中断 标记1 的休眠.
      }
    }, config);

    rb.sendModifyNotice(path);
    msg("Wait file modify notice:", path, " ...");
    Tool.sleep(10000); // 标记1
    ok(check[0], "waiting message, (如果运行了其他节点这个测试会失败.)");
    fmh.removeModifyListener();
  }


  abstract class NullModifyListener implements IFileChangeListener {
    @Override
    public void noticeModifyContent(String file) {}
    @Override
    public void noticeMakeDir(String dirname) {}
    @Override
    public void noticeDelete(String file) {}
    @Override
    public void noticeMove(String form, String to) {}
  }


  public static void main(String[] a) {
    new TestFace();
  }

}
