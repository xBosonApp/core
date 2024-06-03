////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 上午8:47
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/NameCache.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 存储 KEY_TYPE 对应的名字, 该对象适合在 static 中使用, 线程安全
 * @param <KEY_TYPE>
 */
public class NameCache<KEY_TYPE> {

  private Map<KEY_TYPE, String> map;


  public NameCache() {
    map = new ConcurrentHashMap<>();
  }


  public void put(KEY_TYPE k, String name) {
    map.put(k, name);
  }


  public String get(KEY_TYPE k) {
    if (k == null) return null;
    return map.get(k);
  }


  /**
   * 将 class 类型名字转换为简单可识别的类型字符串
   */
  public static String formatClassName(Class cl) {
    String fullname = cl.getName();
    String name;

    if (fullname.indexOf("java.") >= 0) {
      name = "primitive::" + toNoneJavaName(cl.getSimpleName());
    } else if (fullname.indexOf("xboson.") >= 0) {
      name = "xboson::" + toNoneJavaName(cl.getSimpleName());
    } else {
      name = "dynamic::" + toNoneJavaName(cl.getSimpleName());
    }
    return name;
  }


  /**
   * 将 java 命名规则的字符串, 转换为下划线命名规则
   */
  public static String toNoneJavaName(String name) {
    StringBuffer out = new StringBuffer(name.length() * 2);
    for (int i=0; i<name.length(); ++i) {
      char c = name.charAt(i);
      if (Character.isUpperCase(c)) {
        if (i != 0) {
          out.append('_');
        }
        out.append(Character.toLowerCase(c));
      } else {
        out.append(c);
      }
    }
    return out.toString();
  }
}
