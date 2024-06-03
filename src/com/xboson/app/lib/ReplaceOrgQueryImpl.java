////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-10 下午5:33
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/ReplaceOrgQueryImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.been.Page;
import com.xboson.db.ConnectConfig;
import com.xboson.db.analyze.*;
import com.xboson.util.JavaConverter;
import com.xboson.util.SysConfig;
import com.xboson.util.c0nst.IConstant;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.Set;


/**
 * 在查询前会替换 schema
 */
public class ReplaceOrgQueryImpl extends QueryImpl
        implements IUnitListener, IConstant {

  private final String replaceSchemaPrefix;
  private final Set<String> sysTables;


  public ReplaceOrgQueryImpl(SqlConnect sc,
                             RuntimeUnitImpl runtime,
                             String replaceOrg) {
    super(sc, runtime);
    this.replaceSchemaPrefix = replaceOrg + ".";
    this.sysTables = JavaConverter.arr2set(
            SysConfig.me().readConfig().sysTableList);
  }


  @Override
  public int query(ScriptObjectMirror list, String sql, Object[] param)
          throws Exception {
    return super.query(list, replaceSql(sql), param);
  }


  @Override
  public int queryPaging(ScriptObjectMirror list, String sql,
                         Object[] param, Page p, ConnectConfig cc) throws Exception {
    return super.queryPaging(list, replaceSql(sql), param, p, cc);
  }


  public String replaceSql(String sql) {
    SqlContext ctx = new SqlContext();
    SqlParserCached.ParsedDataHandle handle = SqlParserCached.parse(sql);
    SqlParser.tableNames(ctx, handle, this);
    return SqlParser.stringify(ctx, handle);
  }


  @Override
  public void on(SqlContext ctx, IUnit u) {
    String tableName = (String) u.getData();

    //
    // 已经有前缀的不再处理
    //
    if (tableName.indexOf(".") >= 0)
      return;

    if (tableName.startsWith(SYS_TABLE_NOT_REPLACE))
      return;

    if (sysTables.contains(tableName))
      return;

    ctx.set(u, replaceSchemaPrefix + tableName);
  }
}
