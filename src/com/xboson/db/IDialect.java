////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 上午10:40
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/IDialect.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db;

import com.xboson.been.Page;


/**
 * 方言接口, 定义各种方言语句
 */
public interface IDialect {

  String TOTAL_SIZE_COLUMN = "total_size";
  String NOW_TIME_COLUMN = "_now_";


  /**
   * 返回 SQL 文, 用来查询数据库服务器的当前时间, 列名为 _now_
   */
  String nowSql();


  /**
   * 返回创建 Catalog 的 sql 文, 没有参数绑定.
   * Catalog 在 mysql 中是 schema, 在 oracle 中是表空间.
   */
  String createCatalog(String name);


  /**
   * 将带有 select 的查询语句转换为返回一行 total_size 列的语句,
   * total_size 列返回 selectSql 查询实际返回多少行
   *
   * @param selectSql - 带有 select 的 sql 文.
   * @return 转换后的 sql 文
   */
  String count(String selectSql);


  /**
   * 限制结果集的返回行数, 包装 selectSql 后
   *
   * @param selectSql 带有 select 的 sql 文.
   * @param page 对结果集的限制
   * @return 转换后的 sql 文
   */
  String limitResult(String selectSql, Page page);

}
