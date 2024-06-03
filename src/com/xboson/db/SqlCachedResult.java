////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-24 上午9:59
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/SqlCachedResult.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db;

import com.xboson.been.XBosonException;
import com.xboson.sleep.IBinData;
import com.xboson.sleep.ITimeout;
import com.xboson.sleep.RedisMesmerizer;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 该结果集封装了 Connection, ResultSet 等对象,
 * 在完成查询后应该调用 close 关闭, 更好的方式是使用 try(SqlResult r = ...) {};
 *  <br/><br/>
 * 与 SqlResult 不同, 查询的结果将被缓存, 相同的查询可以从缓存中获取,
 * 但是缓存有使用限制: 结果集不能超过 LIMIE_LINE 行, 否则报错.
 *
 * @see SqlResult
 * @see #LIMIE_LINE
 */
public class SqlCachedResult implements AutoCloseable {

  /** 查询结果行限制 */
  public final static int LIMIE_LINE = 100;
  /** 结果集缓存时间, ms */
  public final static int CACHE_TIME = 1 * 60 * 60 * 1000;


  private Connection conn;
  private ConnectConfig dbcf;


  /**
   * 使用指定的 conn 进行查询, 推荐使用 ConnectConfig 参数的构造函数,
   * 很多时候查询并不需要数据库连接.
   * @param conn 已经创建好的连接
   * @see SqlCachedResult#SqlCachedResult(ConnectConfig)
   */
  public SqlCachedResult(Connection conn) {
    if (conn == null) {
      throw new XBosonException.NullParamException("Connection conn");
    }
    this.conn = conn;
  }


  /**
   * 在需要的时候使用 db 配置建立连接, 然后执行查询.
   * @param dbcf 数据库配置
   */
  public SqlCachedResult(ConnectConfig dbcf) {
    if (dbcf == null) {
      throw new XBosonException.NullParamException("ConnectConfig dbcf");
    }
    this.dbcf = dbcf;
  }


  private Connection openConnect() {
    if (conn == null) {
      try {
        conn = DbmsFactory.me().open(dbcf);
      } catch (SQLException e) {
        throw new XBosonException.XSqlException(e);
      }
    }
    return conn;
  }


  /**
   * 执行查询, 并返回查询结果, 这个查询将被缓存一段时间;
   * 如果查询结果超过限制将会抛出异常或仅返回限制行数的数据;
   * 如果不是 select 查询返回 null;
   * 该方法没有加锁, 多线程需要外部同步, 否则可能多次执行相同查询 (没什么大不了);
   * 推荐使用绑定变量的 sql 查询, 这样效果最好.
   *
   * @param sql 查询
   * @param parm 参数列表, 每个参数都必须正确实现 toString(), 即 toString() 可以
   *             完整的表达值本身而非返回对象的 hash (当然如果实现错误, 也查不出结果).
   * @return List 的元素为一行数据, Map 元素为列名:数据.
   * @see SqlCachedResult#LIMIE_LINE
   */
  public List<Map<String, Object>> query(String sql, Object... parm) {
    final String id = computeID(sql, parm);

    SleepSql ss = queryWithoutDB(id);
    if (ss != null) {
      return ss.data;
    }

    ss = queryOnlyDB(id, sql, parm);
    if (ss != null) {
      return ss.data;
    }
    return null;
  }


  /**
   * 直接从缓存中取结果, 不经过 db.
   * @see #query(String, Object...)
   */
  public List<Map<String, Object>> queryWithoutDB(String sql, Object... parm) {
    final String id = computeID(sql, parm);
    SleepSql ss = queryWithoutDB(id);
    if (ss != null) {
      return ss.data;
    }
    return null;
  }


  protected SleepSql queryWithoutDB(String id) {
    return (SleepSql) RedisMesmerizer.me().wake(SleepSql.class, id);
  }


  protected SleepSql queryOnlyDB(String id, String sql, Object... parm) {
    try (PreparedStatement ps = openConnect().prepareStatement(sql)) {
      if (parm != null) {
        for (int i=0; i<parm.length; ++i) {
          ps.setObject(i+1, parm[i]);
        }
      }

      ps.setMaxRows(LIMIE_LINE);
      if (ps.execute() == false) {
        return null;
      }

      final ResultSet rs = ps.getResultSet();
      final ResultSetMetaData meta = rs.getMetaData();
      if (rs.getFetchSize() > LIMIE_LINE) {
        throw new Limitation(sql, rs.getFetchSize());
      }

      final int column_size = meta.getColumnCount();
      List<Map<String, Object>> rows = new ArrayList<>(rs.getFetchSize());
      int line = LIMIE_LINE;

      while (rs.next()) {
        Map<String, Object> col_data = new HashMap<>(column_size);
        rows.add(col_data);

        for (int i=1; i<=column_size; ++i) {
          col_data.put(meta.getColumnLabel(i), rs.getObject(i));
        }
        if (--line < 0) {
          throw new Limitation(sql, LIMIE_LINE - line);
        }
      }

      SleepSql ss = new SleepSql(rows, id);
      RedisMesmerizer.me().sleep(ss);

      return ss;
    } catch (SQLException e) {
      throw new XBosonException.XSqlException(sql, e);
    }
  }


  /**
   * 刷新查询对应的缓存, 发生在 db 数据更改后, 缓存变旧的情况;
   * 没有抛出异常即为成功; 该方法不需要数据库连接.
   *
   * @param sql
   * @param parm
   */
  public static void refresh(String sql, Object... parm) {
    SleepSql ss = new SleepSql();
    ss.id = computeID(sql, parm);
    RedisMesmerizer.me().remove(ss);
  }


  @Override
  public void close() throws Exception {
    if (conn != null) {
      conn.close();
      conn = null;
    }
  }


  private static String computeID(String sql, Object... parm) {
    StringBuilder buf = new StringBuilder();
    buf.append(sql);

    if (parm != null) {
      buf.append('#');
      buf.append(parm.length);

      for (int i = 0; i < parm.length; ++i) {
        String val = parm[i].toString();
        buf.append('&');
        buf.append(val);
      }
    }
    return buf.toString();
  }


  /**
   * 持久化的底层数据
   */
  static public class SleepSql implements IBinData, Serializable, ITimeout {
    List<Map<String, Object>> data;
    long endTime;
    String id;

    public SleepSql() {}

    public SleepSql(List<Map<String, Object>> data, String id) {
      this.data = data;
      this.id = id;
      this.endTime = System.currentTimeMillis() + CACHE_TIME;
    }

    @Override
    public String getid() {
      return id;
    }

    @Override
    public boolean isTimeout() {
      return System.currentTimeMillis() - endTime > 0;
    }
  }


  /**
   * 达到查询上限抛出异常
   */
  public class Limitation extends XBosonException {
    public Limitation(String msg, int outcount) {
      super(msg +"; Limit: "+ outcount +" > "+ LIMIE_LINE);
    }
  }
}
