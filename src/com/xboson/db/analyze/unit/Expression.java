////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-10 下午12:20
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/analyze/unit/Expression.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db.analyze.unit;

import com.xboson.db.analyze.AbsUnit;
import com.xboson.db.analyze.SqlContext;


/**
 * 该对象线程安全.
 * 可通过 setData() 改变表达式的前缀.
 * 多线程复用时, 必须保证 setData() 总是被调用, 否则
 * 会残留上一个线程设置的值.
 */
public class Expression extends AbsUnit<String> {
  private final String exp;


  public Expression(String exp) {
    this.exp = exp;
  }


  @Override
  public void setData(String d) {
    throw new UnsupportedOperationException();
  }


  @Override
  public String getData() {
    return exp;
  }


  @Override
  public String stringify(SqlContext ctx) {
    Object rep = ctx.get(this);
    if (rep == null) {
      return exp;
    } else {
      return rep.toString();
    }
  }
}
