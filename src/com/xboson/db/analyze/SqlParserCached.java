////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-10 下午4:43
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/analyze/SqlParserCached.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db.analyze;

import com.xboson.been.XBosonException;
import com.xboson.util.SysConfig;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;


/**
 * 用于生成缓存的 sql 语法树
 */
public class SqlParserCached extends BaseKeyedPooledObjectFactory<String, ParsedData> {

  /**
   * 小于这个长度的 sql 直接解析不放入缓存
   */
  public static final int CACHE_SQL_LENGTH = 100;


  private static GenericKeyedObjectPool<String, ParsedData> cached;
  static {
    cached = new GenericKeyedObjectPool<>(new SqlParserCached(),
            SysConfig.defaultKeyPoolConfig());
  }


  private SqlParserCached() {}


  @Override
  public ParsedData create(String s) throws Exception {
    return SqlParser.parse(s);
  }


  @Override
  public PooledObject<ParsedData> wrap(ParsedData parsedData) {
    return new DefaultPooledObject<>(parsedData);
  }


  /**
   * 生成的语法树可能来自缓存
   * @param sql
   * @return
   */
  public static ParsedDataHandle parse(String sql) {
    if (sql.length() < CACHE_SQL_LENGTH) {
      return new ParsedDataHandle(SqlParser.parse(sql));
    } else {
      try {
        ParsedData pd = cached.borrowObject(sql);
        return new ParsedDataHandle(sql, pd);
      } catch (Exception e) {
        throw new XBosonException(e);
      }
    }
  }


  /**
   * 解析后 sql 语法树的句柄, 该对象可回收, 对象不可变
   */
  static public class ParsedDataHandle implements AutoCloseable {
    ParsedData pd;
    private String sql;

    public ParsedDataHandle(String sql, ParsedData pd) {
      this.pd = pd;
      this.sql = sql;
    }

    /** 不缓存 */
    public ParsedDataHandle(ParsedData pd) {
      this.pd = pd;
    }

    public void close() {
      if (sql != null) {
        cached.returnObject(sql, pd);
        pd = null;
        sql = null;
      }
    }

    public String toString() {
      return pd.toString();
    }

    protected void finalize() throws Throwable {
      close();
    }
  }
}
