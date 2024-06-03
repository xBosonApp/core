////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-13 上午9:51
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/JsonHelper.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import com.xboson.util.Tool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


/**
 * 帮助对象 json 化,
 * 该实现尽可能不增加子类的内存负担
 */
public abstract class JsonHelper implements IBean, IJson {

  public final static int F_MOD =
          Modifier.STATIC | Modifier.FINAL | Modifier.NATIVE;

  /**
   * 别名: moshi 使用这样的命名
   * 该方法不需要重写, 只要重写 toJSON 即可.
   */
  public final String toJson() {
    return toJSON();
  }


  /**
   * 可以正确处理深层对象
   */
  @Override
  public String toJSON() {
    return toJSON(this);
  }


  public static String toJSON(Object o) {
    return Tool.getAdapter((Class) o.getClass()).toJson(o);
  }


  /**
   * 输出所有属性, 方便调试
   */
  public String toString() {
    return toString(this);
  }


  public static String toString(Object o) {
    StringBuilder out = new StringBuilder();
    Field[] fs = o.getClass().getDeclaredFields();
    out.append(o.getClass());

    for (int i=0; i<fs.length; ++i) {
      Field f = fs[i];
      f.setAccessible(true);
      out.append("\n\t");
      out.append(f.getName());
      out.append(" - ");
      try {
        out.append(f.get(o));
      } catch (IllegalAccessException e) {
        out.append(e.getMessage());
      }
    }
    return out.toString();
  }
}
