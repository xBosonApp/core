////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-11 上午11:58
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/sleep/SleepFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.sleep;

import java.util.HashMap;
import java.util.Map;


/**
 * 当有其他用途时再进行设计
 *
 * @deprecated 通常直接使用 RedisMesmerizer
 * @see RedisMesmerizer
 */
public class SleepFactory {

  private static SleepFactory instance;

  static {
    instance = new SleepFactory();
  }


  /**
   * 需要考虑线程安全
   * @return
   */
  public static SleepFactory me() {
    return instance;
  }


///////////////////////////////////////////////////////////////////////////////

  private Map<Class<?>, IMesmerizer> map;
  private IMesmerizer default_mes;


  private SleepFactory() {
    map = new HashMap<>();
  }


  /**
   * 配置对象类型的持久化算法
   */
  public void config(Class<?> type, IMesmerizer mes) {
    if (type == null)
      throw new NullPointerException("Class");
    if (mes == null)
      throw new NullPointerException("IMesmerizer");
    map.put(type, mes);
  }


  public void configDefault(IMesmerizer mes) {
    if (mes == null)
      throw new NullPointerException("IMesmerizer");
    default_mes = mes;
  }


  public IMesmerizer getMesmerizer(Class<?> c) {
    IMesmerizer mes = map.get(c);
    if (mes == null) {
      throw new NullPointerException("cannot config any Mesmerizer");
    }
    return mes;
  }


  public IMesmerizer getMesmerizer() {
    if (default_mes == null) {
      throw new NullPointerException("cannot get default Mesmerizer");
    }
    return default_mes;
  }

}
