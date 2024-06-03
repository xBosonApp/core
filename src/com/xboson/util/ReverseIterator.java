////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-30 下午1:01
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/ReverseIterator.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import java.util.Iterator;
import java.util.Set;


public class ReverseIterator<E> implements Iterator<E>, Iterable<E> {

  private int p;
  private Object[] arr;


  /**
   * 初始化一个反向迭代器
   * @param set 如果不是 LinkedHashSet 类型则没有意义
   */
  public <E> ReverseIterator(Set<E> set) {
    this.p   = set.size();
    this.arr = new Object[p];

    int i = -1;
    for (E e : set) {
      arr[++i] = e;
    }
  }


  @Override
  public boolean hasNext() {
    return p > 0;
  }


  @Override
  public E next() {
    return (E) arr[--p];
  }


  @Override
  public Iterator<E> iterator() {
    return this;
  }
}
