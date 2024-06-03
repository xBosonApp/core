////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 上午10:40
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/NullDriver.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db;

import com.xboson.been.Page;


/**
 * 方言的默认实现, 都会抛出异常
 */
public abstract class NullDriver implements IDialect, IDriver {

  static final String NOW_SQL = "select now() " + NOW_TIME_COLUMN;
  static final String COUNT_SQL_0 = "Select count(1) AS "
          + TOTAL_SIZE_COLUMN +" From ( ";
  static final String COUNT_SQL_1 = " ) cnt_tbl";


  @Override
  public String nowSql() {
    return NOW_SQL;
  }


  public String createCatalog(String name) {
    throw new UnsupportedOperationException("Unsupported Create Catalog");
  }


  @Override
  public String count(String selectSql) {
    return COUNT_SQL_0+ selectSql + COUNT_SQL_1;
  }


  @Override
  public String limitResult(String selectSql, Page page) {
    throw new UnsupportedOperationException("Unsupported Limit Result");
  }
}
