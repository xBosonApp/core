////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 上午9:53
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestDBMS.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.db.ConnectConfig;
import com.xboson.db.DbmsFactory;
import com.xboson.db.IDriver;
import com.xboson.db.SqlCachedResult;
import com.xboson.db.driver.Mysql;
import com.xboson.db.sql.SqlReader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TestDBMS extends Test {

  public final static int MAX_RESULT_LINE = 50;
  public final static int SHOW_RESULT_LINE = 3;

  protected DbmsFactory db;


  public void test() throws Throwable {
    init_db();
    mysql();
//    t10000();
    read_json_table();
    test_cache_sql();
  }


  public void test_cache_sql() throws Exception {
    sub("Test query sql from cache");
    try (SqlCachedResult scr = new SqlCachedResult(localdb()) ) {
      String sql = "select * from sys_eeb_detail where pname=? limit ?";
      String parm = "Http 服务端";

      List<Map<String, Object>> ret1 = scr.query(sql, parm, SHOW_RESULT_LINE);
      List<Map<String, Object>> ret2 = scr.queryWithoutDB(sql, parm, SHOW_RESULT_LINE);

      eq(ret1, ret2, "cache ok");
      msg(ret2);
    }
  }


  public void init_db() throws Throwable {
    sub("init DBMS factory");
    db = DbmsFactory.me();
  }


  public ConnectConfig localdb() {
    ConnectConfig cc = new ConnectConfig();
    cc.setDbname("mysql");
    cc.setDatabase("eeb");
    cc.setHost("localhost");
    cc.setUsername("root");
    cc.setPassword("root");
    return cc;
  }


  public void mysql() throws Throwable {
    sub("Mysql");

    ConnectConfig cc = localdb();

    IDriver dr = db.getDriver(cc);
    eq(dr.getClass(), Mysql.class, "mysql driver");

    int rl = SHOW_RESULT_LINE;
    query(cc, "select * from sys_eeb_detail limit ?", rl);
    query(cc, "select * from sys_eeb_run_conf limit ?", rl);
    query(cc, "select * from sys_eeb_statistics limit ?", rl);
  }


  public static void query(ConnectConfig cc, String sql, Object ...bindParam)
          throws SQLException {
    beginTime();
    PreparedStatement stat = null;
    ResultSet set = null;

    try (Connection conn = DbmsFactory.me().open(cc)) {
      endTime("Open Connection");
      beginTime();
      stat = conn.prepareStatement(sql);

      for (int i =1; i<=bindParam.length; ++i) {
        stat.setObject(i, bindParam[i-1]);
      }

      set = stat.executeQuery();
      show(set);
    } finally {
      endTime("Query", sql);
      if (stat != null) {
        ok(stat.isClosed(), "Statement closed yet");
      }
      if (set != null) {
        ok(set.isClosed(), "ResultSet closed yet");
      }
    }
  }


  public static void show(ResultSet rs) throws SQLException {
    ResultSetMetaData meta = rs.getMetaData();
    StringBuilder out = new StringBuilder("\n");
    final int cc = meta.getColumnCount();
    final String name = "[ TABLE - "+ meta.getTableName(1) +" ]";

    for (int i=1; i<=cc; ++i) {
      out.append(meta.getColumnLabel(i));
      out.append('\t');
    }
    out.append('\n');
    out.append(line);
    sub(out);
    out.setLength(0);

    int showline = MAX_RESULT_LINE;

    while (rs.next()) {
      for (int c = 1; c <= cc; ++c) {
        out.append(rs.getObject(c));
        out.append('\t');
      }
      msg(out);
      out.setLength(0);

      if (--showline <= 0)
        break;
    }

    if (showline <= 0) {
      sub("... More lines ... ", name, "\n");
    } else {
      sub(name, "\n");
    }
  }


  /**
   * 压力测试
   *
   * 创建到数据库的硬链接, 用获取的链接执行一个简单查询,
   * 然后放入列表并且不释放任何资源.
   * (win7 系统限制 1910 个 mysql 链接)
   *
   * 创建到 1260 个链接消耗时间  Used Time 3337 ms
   * ##### Heap utilization statistics [MB] #####
   *    Used Memory:139
   *    Free Memory:287
   *    Total Memory:427
   *    Max Memory:3620
   *
   * 查询一个万行表保留1000行数据
   * 创建到 1170 个链接消耗时间  Used Time 8501 ms
   * ##### Heap utilization statistics [MB] #####
   *    Used Memory:729
   *    Free Memory:369
   *    Total Memory:1099
   *    Max Memory:3620
   */
  public void t10000() throws Throwable {
    final int count = 1300;
    final int showc = (int)(count / 10);

    msg("创建", count, "个 mysql 硬链接来测试内存.");
    msg("");

    DbmsFactory df = DbmsFactory.me();
    ConnectConfig cc = localdb();

    List<Connection> connList = new ArrayList<>(count);
    beginTime();
    int i = 0;

    try {
      for (i = 0; i < count; ++i) {
        Connection conn = df.openWithoutPool(cc);
        Statement s = conn.createStatement();
        s.execute("select * from sys_eeb_detail limit 1000");
        connList.add(conn); // do not close
        if (i % showc == 0) {
          endTime("\n创建到", i, "个链接消耗时间");
          memuse();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("创建第"+ i +"个链接时发生异常", e);
    }
  }


  public void read_json_table() throws Exception {
    sub("Read sql from json file.");
    String user0003 = SqlReader.read("user0003");
    ok(user0003 != null, "from json");
    msg("SQL:", line, '\n', user0003);
    msg("Bind param count:", bind_parm_count(user0003));
  }


  public int bind_parm_count(String sql) {
    int ret = 0;
    for (int i=0; i<sql.length(); ++i) {
      if (sql.charAt(i) == '?') {
        ++ret;
      }
    }
    return ret;
  }


  public static void main(String []a) {
    new TestDBMS();
  }
}
