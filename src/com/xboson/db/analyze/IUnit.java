////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-10 上午10:35
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/analyze/IUnit.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db.analyze;

/**
 * 解析后的语法单位;
 * 注意线程安全: 解析后的语法树会被缓存, 并循环利用.
 */
public interface IUnit<T> {

  /**
   * 给单位设置参数, 单位需要自行解析 sql 字符串为自身变量.
   * 注意: 该方法的实现在必要时使用线程级变量.
   */
  void setData(String d);

  /**
   * 获取值;
   * 注意: 该方法的实现在必要时使用线程级变量.
   * 该方法返回的值未必与 setData 设置的值相同, 不保证对称.
   */
  T getData();

  /**
   * 设置父级关键字
   */
  void setParent(IUnit n);
  IUnit getParent();

  void setOperating(UnitOperating t);
  UnitOperating getOperating();

  /**
   * 输出为 sql 时被调用
   */
  String stringify(SqlContext ctx);

  /**
   * 锁定当前组件, set 方法默认将抛出异常,
   * 由于组件可以被多线程访问, 实现需要在必要时检查锁, 决定 set 的行为.
   */
  void lock();
  boolean isLocked();
}
