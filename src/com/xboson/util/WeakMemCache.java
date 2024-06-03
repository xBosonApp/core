////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-14 上午10:23
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/WeakMemCache.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;

import java.util.Map;
import java.util.WeakHashMap;


/**
 * 使用 WeakHashMap 实现的内存对象缓存, 多线程安全
 */
public class WeakMemCache<K, V> {

  public interface ICreator<K, V> {
    V create(K init);
  }


  private Map<K, V> pool;
  private ICreator<K, V> creator;


  public WeakMemCache(ICreator<K, V> c) {
    this.pool = new WeakHashMap<>();
    this.creator = c;
  }


  public V getOrCreate(K key) {
    if (key == null)
      throw new XBosonException.NullParamException("key");

    V value;
    synchronized (this) {
      value = pool.get(key);

      if (value == null) {
        value = creator.create(key);
        pool.put(key, value);
      }
    }
    return value;
  }


  public void remove(K key) {
    synchronized (this) {
      pool.remove(key);
    }
  }
}
