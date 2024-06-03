////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-19 上午9:30
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/JavaConverter.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Java 常用数据类型转换
 */
public class JavaConverter {

  private JavaConverter() {}


  /**
   * 转换对象为字符串数组, 如果 objs 为空则返回 nullData.
   */
  public static String[] toStringArr(Object[] objs, String[] nullData) {
    if (objs == null)
      return nullData;

    String[] ps = new String[objs.length];
    for (int i = 0; i < ps.length; ++i) {
      ps[i] = (objs[i] == null) ? null : objs[i].toString();
    }
    return ps;
  }


  /**
   * 尝试将对象转换为 bool 值, 未知的类型将返回 false
   */
  public static boolean toBool(Object o) {
    if (o == null)
      return false;
    if (o instanceof String) {
      return Boolean.parseBoolean((String) o);
    }
    if (o instanceof Long) {
      return ((long) o) != 0;
    }
    if (o instanceof Double) {
      return ((double) o) != 0;
    }
    if (o instanceof Boolean) {
      return (boolean) o;
    }
    return false;
  }


  /**
   * 将 T 类型数组转换为 Set, 没有附加的处理
   */
  public static<T> Set<T> arr2set(T[] arr) {
    Set<T> set = new HashSet<>();
    for (int i=0; i<arr.length; ++i) {
      set.add(arr[i]);
    }
    return set;
  }


  /**
   * 比 arr2set 更适合进行构造
   */
  public static<T> Set<T> param2set(T... arr) {
    Set<T> set = new HashSet<>();
    for (int i=0; i<arr.length; ++i) {
      set.add(arr[i]);
    }
    return set;
  }


  /**
   * 转换为大写的 Set 集合
   */
  public static Set<String> arr2setUpper(String[] arr) {
    Set<String> set = new HashSet<>();
    for (int i=0; i<arr.length; ++i) {
      set.add(arr[i].toUpperCase());
    }
    return set;
  }


  /**
   * 转换为小写的 Set 集合
   */
  public static Set<String> arr2setLower(String[] arr) {
    Set<String> set = new HashSet<>();
    for (int i=0; i<arr.length; ++i) {
      set.add(arr[i].toLowerCase());
    }
    return set;
  }
}
