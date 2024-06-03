////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 下午1:33
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/sql/SqlReader.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db.sql;

import com.xboson.been.XBosonException;
import com.xboson.db.ConnectConfig;
import com.xboson.db.DbmsFactory;
import com.xboson.db.SqlResult;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.Tool;
import okio.Buffer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * 读取本目录中的 sql 语句
 */
public class SqlReader {

  private final static Map<String, String> sqlCache = new HashMap<>();


  /**
   * 初始化一些数据
   */
  public static void me() {
    SqlReader.readSqlFromJson(
            "sys_sqls.table-data.json",
            "sqlid", "content");
  }


  /**
   * 返回该类所在包下的 sql 文内容.
   *
   * @param file 可以是文件名, 也是可是缓存的 sql 名称.
   */
  public static String read(String file) {
    String ret = sqlCache.get(file);
    if (ret != null)
      return ret;

    synchronized (sqlCache) {
      ret = sqlCache.get(file);
      if (ret != null)
        return ret;

      URL sqlfile = SqlReader.class.getResource("./" + file);
      if (sqlfile == null) {
        sqlfile = SqlReader.class.getResource("./" + file + ".sql");
      }
      if (sqlfile == null) {
        throw new XBosonException("cannot found .sql file: " + file);
      }

      StringBufferOutputStream str = new StringBufferOutputStream();
      try {
        str.write(sqlfile.openStream());
        ret = str.toString();
        sqlCache.put(file, ret);
      } catch (IOException e) {
        throw new XBosonException("read file: " + file, e);
      }
    }

    return ret;
  }


  /**
   * 执行 sql 文, 返回一个连接和数据的封装包, 可以对数据做进一步处理.
   *
   * @param filename 保存 sql 文的文件名, 也是可是缓存的 sql 名称.
   * @param config 数据库连接配置
   * @param parm sql 文绑定数据
   * @return 对 sql 相关对象的封装
   * @throws XBosonException.XSqlException
   */
  public static SqlResult query(String filename, ConnectConfig config,
                                Object...parm) {
    String sql = read(filename);
    return queryTxt(sql, config, parm);
  }


  /**
   * 从文件中读取 sql 模板, 模板使用 String.format 语法绑定变量.
   * 该方法绑定变量的效率不高.
   *
   * @param filename sql 模板文件名
   * @param bind 模板绑定变量
   * @param config 数据库连接设置
   * @param parm 数据库绑定参数
   */
  public static SqlResult query(String filename, Object[] bind,
                                ConnectConfig config, Object...parm) {
    String sqltpl = read(filename);
    String sql = String.format(sqltpl, bind);
    return queryTxt(sql, config, parm);
  }


  private static SqlResult queryTxt(String sqltxt, ConnectConfig cnf,
                                   Object...parm) {
    Connection conn = null;
    boolean needClose = true;
    try {
      conn = DbmsFactory.me().open(cnf);
      SqlResult sr = SqlResult.query(conn, sqltxt, parm);
      needClose = false;
      return sr;
    } catch(XBosonException xe) {
      throw xe;
    } catch(Exception e) {
      throw new XBosonException.XSqlException(sqltxt, e);
    } finally {
      if (needClose) Tool.close(conn);
    }
  }


  /**
   * 从 json 文件中读取 sql 语句, 每一行为一个 sql 对象.
   * 之后可以用 read/query 利用 sql 文.
   *
   * @param file 文件名, 相对于本类的路径
   * @param key_name 作为 sql 名称的属性名
   * @param val_name 作为 sql 文的属性名
   */
  public static void readSqlFromJson(String file, String key_name, String val_name) {
    URL jsonfile = SqlReader.class.getResource("./" + file);
    if (jsonfile == null)
      throw new XBosonException("cannot found " + file);

    try (InputStream in = jsonfile.openStream()) {
      Buffer jr = new Buffer();
      jr.readFrom(in);
      List list = Tool.getAdapter(List.class).fromJson(jr);
      Iterator it = list.iterator();

      synchronized (sqlCache) {
        while (it.hasNext()) {
          Map row = (Map) it.next();
          sqlCache.put(row.get("sqlid").toString(),
                  row.get("content").toString());
        }
      }
    } catch(Exception e) {
      throw new XBosonException(e);
    }
  }
}
