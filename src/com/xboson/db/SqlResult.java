////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 下午1:53
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/sql/SqlResult.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db;

import com.xboson.been.XBosonException;
import com.xboson.util.Tool;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;


/**
 * 该结果集封装了 Connection, ResultSet 等对象,
 * 在完成查询后应该调用 close 关闭, 更好的方式是使用 try(SqlResult r = ...) {}.
 * <br/><br/>
 * 大部分时候推荐使用 SqlCachedResult 替换 SqlResult
 *
 * @see SqlCachedResult
 */
public class SqlResult implements AutoCloseable {

  private Connection conn;
  private PreparedStatement ps;
  private boolean is_update;


  public SqlResult(Connection conn, PreparedStatement ps) throws SQLException {
    this.conn = conn;
    this.ps = ps;
    this.is_update = ! ps.execute();
  }


  /**
   * 执行 sql 文, 返回一个连接和数据的封装包, 可以对数据做进一步处理.
   *
   * @param conn 数据库连接
   * @param sql 查询文
   * @param parm sql 文绑定数据
   * @return 对 sql 相关对象的封装
   * @throws XBosonException.XSqlException
   */
  public static SqlResult query(Connection conn, String sql, Object...parm) {
    try {
      PreparedStatement ps = conn.prepareStatement(sql,
              ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

      if (parm != null) {
        for (int i=0; i<parm.length; ++i) {
          ps.setObject(i+1, parm[i]);
        }
      }
      return new SqlResult(conn, ps);

    } catch(Exception e) {
      String bind = Arrays.toString(parm);
      throw new XBosonException.XSqlException(sql +"\nBind parm: "+ bind, e);
    }
  }


  /**
   * 使用现有数据库连接进行一个新的查询.
   *
   * @param sql
   * @param parm
   * @return 创建一个新查询对象, 不需要主动关闭, 因为父级关闭后自动关闭
   */
  public SqlResult query(String sql, Object...parm) {
    return query(conn, sql, parm);
  }


  /**
   * 返回查询结果集, 如果查询不是 select, 则返回 null.
   */
  public ResultSet getResult() {
    if (is_update) return null;
    try {
      return ps.getResultSet();
    } catch (SQLException e) {
      throw new XBosonException.XSqlException(e);
    }
  }


  /**
   * 如果是更新语句返回更新的行数, 否则返回 -1
   */
  public int getUpdateCount() {
    if (is_update) {
      try {
        return ps.getUpdateCount();
      } catch (SQLException e) {
        throw new XBosonException.XSqlException(e);
      }
    } else {
      return -1;
    }
  }


  /**
   * 如果语句只是更新, 没有返回结果集.
   */
  public boolean isUpdate() {
    return is_update;
  }


  /**
   * 关闭所有资源
   */
  @Override
  public void close() {
    Tool.close(conn);
  }


  /**
   * 将结果数据库集转换为列表对象并返回, 每行数据是一个 map.
   * 适合小数据集的转换, 并将结果附加在 json 上.
   */
  public List resultToList() throws SQLException {
    List<Map<String, Object>> ret;

    try (ResultSet rs = getResult()) {
      ret = new ArrayList<>(rs.getFetchSize());
      ResultSetMetaData meta = rs.getMetaData();
      int cc = meta.getColumnCount();

      while (rs.next()) {
        Map<String, Object> row = new HashMap<>(cc);
        ret.add(row);
        for (int i=1; i<=cc; ++i) {
          row.put(meta.getColumnLabel(i), rs.getObject(i));
        }
      }
    }
    return ret;
  }


  /**
   * 将唯一一行数据通过反射设置到对象上
   * @param o 保存数据的对象
   * @return 返回读取的行数, 0 没有数据, 1 一行数据, 没有其他的情况
   */
  public int oneRowTo(Object o)  {
    int rl = 0;
    try (ResultSet rs = getResult()) {
      ResultSetMetaData meta = rs.getMetaData();
      int cc = meta.getColumnCount();

      if (rs.next()) {
        for (int i=1; i<=cc; ++i) {
          String cname = meta.getColumnLabel(i);
          Field f = o.getClass().getDeclaredField(cname);
          f.setAccessible(true);
          f.set(o, rs.getObject(i));
        }
        ++rl;
      }
    } catch (IllegalAccessException e) {
      throw new XBosonException("反射时权限错误", e);
    } catch (SQLException e) {
      throw new XBosonException.XSqlException(e);
    } catch (NoSuchFieldException e) {
      throw new XBosonException("无法通过反射设置属性", e);
    }
    return rl;
  }
}
