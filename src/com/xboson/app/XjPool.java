////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-22 下午2:04
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/XjPool.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app;

import com.xboson.been.XBosonException;
import com.xboson.log.ILogName;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 方便创建层级式的对象池
 */
public abstract class XjPool<E> implements ILogName {

  private Map<String, E> pool;
  protected Log log;


  protected XjPool() {
    pool = new ConcurrentHashMap<>();
    log = LogFactory.create(this);
  }


  /**
   * 创建对象, 如果已经存在则返回
   * @param name
   * @return
   */
  protected E getWithCreate(String name) {
    if (name == null) {
      throw new XBosonException.NullParamException("String name");
    }
    E ret = pool.get(name);

    if (ret == null) {
      //
      // 即使 pool 已经同步, 仍然需要同步块来保证 createItem() 只调用一次.
      //
      synchronized (this) {
        ret = pool.get(name);
        if (ret == null) {
          ret = createItem(name);
          pool.put(name, ret);
        }
      }
    }
    return ret;
  }


  /**
   * 实现该方法, 当需要创建一个新的类型实例时被调用.
   * 该方法实现中通常不需要额外的同步操作, 是线程安全的.
   */
  protected abstract E createItem(String name);

}
