////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午4:23:19
// 原始文件路径: xBoson/src/com/xboson/test/TestTool.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.app.lib.CountImpl;
import com.xboson.been.XBosonException;
import com.xboson.db.driver.Mysql;
import com.xboson.fs.watcher.INotify;
import com.xboson.fs.watcher.IWatcher;
import com.xboson.fs.watcher.LocalDirWatcher;
import com.xboson.util.ChineseInital;
import com.xboson.util.CreatorFromUrl;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.Tool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;


public class TestTool extends Test {

  public void test() throws Throwable {
    tool();
    color();
    local_file_watcher();
    // uri_object();
    red(new XBosonException("test").getMessage());
    // check_string_hash();
    test_chinese();
    test_id();
    test_http_get();
    test_calendar();
    read_package();
    null_speed();
    copy_bytes();
    url_creator();
    count_impl();
  }


  private void count_impl() throws Exception {
    sub("Count Implement");
    Date now = new Date();
    String key = "a000";
    CountImpl c = new CountImpl();
    String pass = c.create(key);
    c.inc(key);

    Throws(Exception.class, ()-> c.openSearch(key, "bad"));

    CountImpl.Search s = c.openSearch(key, pass);
    msg("Total:", s.get(c.TOTAL, now) );
    msg("Range YEAR:", s.range(c.YEAR, now));
    msg("Range MONTH:", s.range(c.MONTH, now));
    msg("Range DAY:", s.range(c.DAY, now));
    msg("Range HOUR:", s.range(c.HOUR, now));
    msg("Fix FIX_MONTH:", s.range(c.FIX_MONTH, now));
    msg("Fix FIX_DAY_MON:", s.range(c.FIX_DAY_MON, now));
    msg("Fix FIX_DAY_YEAR:", s.range(c.FIX_DAY_YEAR, now));
    msg("Fix FIX_HOUR:", s.range(c.FIX_HOUR, now));
    msg("Fix FIX_WEEK:", s.range(c.FIX_WEEK, now));

    NumberFormat nf = new DecimalFormat("000");
    msg("NUM:", nf.format(1));
  }


  private void url_creator() throws Exception {
    sub("URL parse and creator");
    CreatorFromUrl<String> cf = new CreatorFromUrl<>();
    cf.reg("a", (v, p, u, d) -> p +'='+ v);
    cf.reg("b", (v, p, u, d) -> v +'='+ p);

    eq("a=000", cf.create("a://000"), "url1");
    eq("000=b", cf.create("b://000"), "url2");

    Throws(XBosonException.class, ()-> cf.create("c://000"));
    Throws(XBosonException.class, ()-> cf.create("000"));

    cf.reg(true, "def", (v, p, u, d) -> v +'!'+ p);

    eq("000!def", cf.create("def://000"), "url3");
    eq("111!null", cf.create("111"), "default protocol");
  }


  public void copy_bytes() throws IOException {
    sub("Copy bytes [Tool / StringBufferOutputStream]");
    StringBufferOutputStream out = new StringBufferOutputStream();
    InputStream input = new Counter();
    out.write(input);

    byte[] b = out.toBytes();
    for (int i=0; i<b.length; ++i) {
      if (i%16 != b[i]) {
        fail("copy bytes on offset", i, b[i], "!=", i%16);
        return;
      }
    }
    msg("ok, bytes length is", b.length);
  }


  private class Counter extends InputStream {
    int i = -1;

    @Override
    public int read() throws IOException {
      if (i > 10240) return -1;
      return ++i%16;
    }
  }


  /**
   * 空字符串性能测试对比
   * count     trim(ms) Tool(ms) 效能(倍)
   * 10000     3        4        0.9
   * 100000    25       8        3
   * 1000000   108      20       5
   * 10000000  807      84       10
   */
  public void null_speed() {
    int count = 1000000;
    String str = "  cccc    ";

    beginTime();
    for (int i=0; i<count; ++i) {
      if (str.trim().length() == 0) {
        throw new XBosonException("bad");
      }
    }
    endTime("String.trim().length()", count);
    msg("ok");

    beginTime();
    for (int i=0; i<count; ++i) {
      if (Tool.isNulStr(str)) {
        throw new XBosonException("bad");
      }
    }
    endTime("Tool.isNulStr()", count);
    msg("ok");
  }


  public void color() {
    for (int i=1; i<8; ++i) {
      System.out.print("\u001b[90;"+ i
              +"m颜色测试字符串 Color Test ["+ i +"]\u001b[m\t");
    }
    for (int i=30; i<48; ++i) {
      System.out.print("\u001b[90;"+ i
              +"m颜色测试字符串 Color Test ["+ i +"]\u001b[m\t");
    }
    for (int i=91; i<107; ++i) {
      System.out.print("\u001b[90;"+ i
              +"m颜色测试字符串 Color Test ["+ i +"]\u001b[m\t");
    }
  }


  public void read_package() throws Exception {
    sub("Read package");
    Set<Class> all = Tool.findPackage(TestTool.class);
    msg("Package (com.xboson.test.*)", all);

    Set<Class> a2 = Tool.findPackage(okio.BufferedSink.class);
    msg("Package (okio.*)", a2);

    Set<Class> cl = Tool.findPackage(Mysql.class);
    msg("Package (db.driver.*)", cl);

    com.xboson.script.lib.Path p = com.xboson.script.lib.Path.me;

    eq("com.xboson.db.driver.DB2",
            p.toClassPath("/com/xboson/db/driver/DB2.class"), "class1");

    eq("com.xboson.db.driver.DB2",
            p.toClassPath("com/xboson/db/driver/DB2.class"), "class2");

    eq("com.xboson.db.driver.DB2",
            p.toClassPath("com/xboson\\db\\/driver.DB2.class"), "class3");

    msg("ok");
  }


  public void test_calendar() {
    sub("Calendar");
    Calendar c = Calendar.getInstance();
    msg(c.getTime());
    c.add(Calendar.DAY_OF_YEAR, 2);
    msg(c.getTime());
    c.add(Calendar.WEEK_OF_YEAR, 2);
    msg(c.getTime());
  }


  public void test_http_get() throws Exception {
    sub("Http client from okio");

    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
            .url("http://bing.com")
            .build();

    try {
      Response response = client.newCall(request).execute();
      msg("GET", response.body().string().substring(0, 300), "...");
    } catch (Exception e) {
      msg("BAD", e);
    }
  }


  public void test_id() {
    sub("SnowflakeIdWorker");
    beginTime();
    for (int i=0; i<5000000; ++i) {
      Tool.nextId();
    }
    endTime("生成 id");
    for (int i=0; i<5; ++i) {
      msg("Next ID", Tool.nextId());
    }
  }


  public void test_chinese() {
    check("中华人民共和国", "zhrmghg");
    check("山高似水深", "sgsss");
    check("窗前明月光", "cqmyg");
    check("酰孢苷酯喹呋喃瘾痫癫祛厥", "xbgzkfnyxdqj");

//    //
//    // 使用缓存: to First Letter 1000000  Used Time 389 ms
//    // 不用缓存: to First Letter 1000000  Used Time 1220 ms
//    //
//    beginTime();
//    int c = 1000000;
//    for (int i=0; i<c; ++i) {
//      ChineseInital.getAllFirstLetter("中华人民共和国");
//    }
//    endTime("to First Letter", c);
  }


  public void check(String cn, String en) {
    String s = ChineseInital.getAllFirstLetter(cn);
    eq(en, s, "ch -> en");
  }


  /**
   * Random len:  5 total: 1000000 conflict: 184  Used Time 2169 ms
   * Random len: 10 total: 1000000 conflict: 110  Used Time 2169 ms
   * Random len: 20 total: 1000000 conflict:  93  Used Time 2386 ms
   */
  public void check_string_hash() {
    sub("Check random string 10 length hash value conflict");
    int total = 1000000;
    int d = total / 10;
    int conflict = 0;
    int strlen = 5;

    beginTime();
    Map<Integer, String> check = new HashMap<>();
    for (int i=0; i<total; ++i) {
      String s = randomString(strlen);
      int hash = s.hashCode();
      String c = check.get(hash);

      if (c == null) {
        check.put(hash, s);
      } else if (!s.equals(c)){
        msg("hash conflict:", s, c, hash);
        ++conflict;
      }

      if (i % d == 0) {
        endTime("hash", i, "conflict:", conflict);
      }
    }
    endTime("Random len:", strlen, "total:", total, "conflict:", conflict);
  }


  /**
   * URI 是轻量对象, 可以大量使用
   *
   * create URI object 40000  Used Time 171 ms
   * ##### Heap utilization statistics [MB] #####
   *    Used Memory:35
   *    Free Memory:209
   *    Total Memory:245
   *    Max Memory:3620
   */
  public void uri_object() throws URISyntaxException {
    sub("Url parse speed");
    final int c= 50000;
    final int p = c / 5;

    URI[] arr = new URI[c];
    beginTime();

    for (int i=0; i<c; ++i) {
      arr[i] = new URI("test://", "localhost", "/TestTool");
      if (i % p == 0) {
        endTime("create URI object", i);
        memuse();
      }
    }
  }


	public void tool() throws Throwable {
		Exception e = create(20);
//		msg(Tool.allStack(e));
		msg(Tool.miniStack(e, 5));

		eq(10, Tool.randomString(10).length(), "length");
    eq(20, Tool.randomString(20).length(), "length");
    for (int i=0; i<10; ++i) {
      msg(Tool.randomString(79));
    }
	}


	public void local_file_watcher() throws Throwable {
    LocalDirWatcher lfw = LocalDirWatcher.me();
    String base = System.getenv("TMP");
//    String base = SysConfig.me().readConfig().configPath;
    Path p = Paths.get(base);

    final boolean[] sw = new boolean[1];

    IWatcher w = lfw.watchAll(p, new INotify() {
      public void notify(String basename, String filename, WatchEvent event,
                         WatchEvent.Kind kind) throws IOException {
        msg(kind, basename, filename, event.count());
        sw[0] = true;
      }
      public void remove(String basename) {
        msg("removed", basename);
      }
    });


    msg("Wait for DIR change:", p);
    Tool.sleep(1 * 1000);

    File testfile = new File(base + "/test_file.txt");
    FileWriter fw = new FileWriter(testfile);
    fw.write("Test file watcher\n");
    fw.write(lfw.getClass().toString());
    fw.write('\n');
    String str = new Date().toString();
    fw.write(str);
    fw.close();

    Tool.sleep(1 * 1000);
    ok(sw[0], "Received a file change notification");
  }
	
	
	public Exception create(int i) {
		if (i > 0) {
			return create(--i);
		}
		return new Exception("Test Stack");
	}


	public static void main(String[] a) {
		new TestTool();
	}
}
