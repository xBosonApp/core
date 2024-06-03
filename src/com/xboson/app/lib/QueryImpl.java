////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-8 上午8:11
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/QueryImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.app.AppContext;
import com.xboson.been.Page;
import com.xboson.been.XBosonException;
import com.xboson.db.ConnectConfig;
import com.xboson.db.DbmsFactory;
import com.xboson.db.IDialect;
import com.xboson.db.SqlResult;
import com.xboson.db.analyze.SqlParser;
import com.xboson.db.analyze.SqlParserCached;
import com.xboson.log.Level;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.c0nst.IConstant;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Iterator;


/**
 * 使用 QueryFactory 来创建该对象的实例
 * @see QueryFactory
 */
public class QueryImpl {

  public static final int FETCH_SIZE = 100;
  private Log log;


  /**
   * QueryImpl 通过该接口打开数据库连接
   */
  public interface SqlConnect {
    /**
     * 该方法在必要时创建数据库连接, 之后会缓存该连接, 并在后续调用返回缓存的连接.
     * 返回的连接不可以关闭, 连接返回的资源可以关闭.
     */
    Connection open() throws Exception;
  }


  private RuntimeUnitImpl runtime;
  private SqlConnect sc;


  public QueryImpl(SqlConnect sc, RuntimeUnitImpl runtime) {
    this.sc = sc;
    this.runtime = runtime;
    this.log = LogFactory.create("app.sql");
  }


  /**
   * 针对 js 环境, 执行 sql 查询, 查询在参数无法解析时则绑定 null
   *
   * @param list 结果集将绑定在 list 上
   * @param sql 查询
   * @param param 参数绑定
   * @return 查询结果总数
   * @throws Exception
   */
  public int query(ScriptObjectMirror list, String sql, Object[] param)
          throws Exception {
    if (! log.blocking(Level.DEBUG)) {
      log(sql, param);
    }

    try (PreparedStatement ps = sc.open().prepareStatement(sql)) {
      if (param != null) {
        setParamter(ps, param);
      }

      ResultSet rs = ps.executeQuery();
      int row_count = copyToList(runtime, list, rs);

      rs.close();
      return row_count;
    }
  }


  /**
   * 用流的方式, 一行一行读取查询结果
   */
  public ResultReader queryStream(String sql, Object[] param) throws Exception {
    if (! log.blocking(Level.DEBUG)) {
      log(sql, param);
    }
    return new ResultReader(sql, param);
  }


  public int queryPaging(ScriptObjectMirror list, String sql, Object[] param,
                         Page p, ConnectConfig cc) throws Exception {
    IDialect dialect = DbmsFactory.me().getDriver(cc);

    if (p.totalCount <= 0) {
      try {
        //
        // mssql 在子查询中有 order by 会抛出异常.
        //
        SqlParserCached.ParsedDataHandle handle = SqlParserCached.parse(sql);
        String totalSql = SqlParser.removeOrder(handle);
        String countSql = dialect.count(totalSql);
        //
        // 不要关闭 sr, 否则 connect 也会被关闭
        //
        SqlResult sr = SqlResult.query(sc.open(), countSql, param);
        ResultSet rs = sr.getResult();
        rs.next();
        p.totalCount = rs.getInt(IDialect.TOTAL_SIZE_COLUMN);
        rs.close();

      } catch(Exception e) {
        SysImpl sys = (SysImpl) ModuleHandleContext._get("sys");
        sys.bindResult("warn0", "Calculate Page Fail: "+ e);
      }
    }

    String limitSql;
    if (p.totalCount > p.pageSize) {
      limitSql = dialect.limitResult(sql, p);
    } else {
      limitSql = sql;
    }

    query(list, limitSql, param);
    return p.totalCount;
  }


  /**
   * 将结果集中的数据灌入 list 中, 每行都是一个 js 对象.
   * 当列值为 null 则放入空字符串.
   *
   * @param list 结果集放入 list 中并返回
   * @param rs db 查询结果集, 不关闭
   * @return 放入 list 中的行数
   */
  public static int copyToList(RuntimeUnitImpl rt,
                               ScriptObjectMirror list,
                               ResultSet rs)
          throws SQLException {

    ResultSetMetaData meta = rs.getMetaData();
    int column = meta.getColumnCount();
    int row_count = 0;
    int arri = list.size() - 1;

    String[] columnLabels = new String[column+1];
    for (int c = 1; c <= column; ++c) {
      columnLabels[c] = meta.getColumnLabel(c);
    }

    while (rs.next()) {
      ScriptObjectMirror row = rt.createJSObject();
      list.setSlot(++arri, rt.unwrap(row));
      ++row_count;

      for (int c = 1; c <= column; ++c) {
        Object d = rs.getObject(c);
        if (d == null) d = IConstant.NULL_STR;
        row.setMember(columnLabels[c], d);
      }
    }
    return row_count;
  }


  /**
   * 在必要时会替换 sql 中的元素
   */
  public String replaceSql(String sql) {
    return sql;
  }


  private void log(String sql, Object[] param) {
    AppContext ac = AppContext.me();
    log.debug("User:", ac.who(),
            "; API:", ac.getCurrentApiPath(),
            "; SQL:", sql,
            "; BIND:", Arrays.toString(param));
  }


  private void setParamter(PreparedStatement ps, Object[] param) {
    Object p = null;
    for (int i = 1; i <= param.length; ++i) {
      try {
        p = param[i - 1];
        ps.setObject(i, runtime.getSafeObjectForQuery(p));
      } catch(SQLException e) {
        log.error("PreparedStatement bind", p, e);
        try {
          ps.setObject(i, null);
        } catch (SQLException e1) {
          log.error("PreparedStatement setNull:", p, e1);
        }
      }
    }
  }


  /**
   * 用来遍历 sql 查询结果集
   */
  public class ResultReader implements AutoCloseable,
          Iterable<ScriptObjectMirror>, Iterator<ScriptObjectMirror> {

    private PreparedStatement ps;
    private ResultSetMetaData meta;
    private ResultSet rs;
    private final int columnc;
    private boolean next;
    private ScriptObjectMirror currLine;


    private ResultReader(String sql, Object[] param) throws Exception {
      this.ps = sc.open().prepareStatement(sql);
      this.ps.setFetchSize(FETCH_SIZE);
      if (param != null) {
        setParamter(ps, param);
      }
      this.rs = ps.executeQuery();
      this.meta = rs.getMetaData();
      this.columnc = meta.getColumnCount();
      this.next = false;
    }


    /**
     * 关闭可以节省内存, 或等待系统自动关闭
     */
    @Override
    public void close() throws Exception {
      ps.close();
      rs.close();
    }


    @Override
    public Iterator<ScriptObjectMirror> iterator() {
      return this;
    }


    /**
     * 调用该方法将把游标移动到下一行, 初始化时游标在第一行数据之前.
     */
    @Override
    public boolean hasNext() {
      try {
        next = true;
        return rs.next();
      } catch (SQLException e) {
        throw new XBosonException.XSqlException(e);
      }
    }


    /**
     * 获取当前游标指向的数据行, 返回该行数据的 map 对象.
     */
    @Override
    public ScriptObjectMirror next() {
      if (!next) {
        return currLine;
      }
      next = false;

      try {
        ScriptObjectMirror ret = runtime.createJSObject();
        for (int i=1; i<=columnc; ++i) {
          String name = meta.getColumnName(i);
          Object val  = rs.getObject(i);
          ret.setMember(name, val == null ? runtime.nullObj() : val);
        }
        currLine = ret;
        return ret;
      } catch(SQLException e) {
        throw new XBosonException.XSqlException(e);
      }
    }
  }
}
