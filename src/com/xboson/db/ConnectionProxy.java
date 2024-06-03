////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 下午6:19
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/ConnectionProxy.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db;

import com.xboson.been.XBosonException;
import com.xboson.event.timer.TimeFactory;
import com.xboson.util.AutoCloseableProxy;
import com.xboson.util.CloseableSet;
import com.xboson.util.ResourceLeak;
import com.xboson.util.Tool;
import org.apache.commons.pool2.KeyedObjectPool;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;


/**
 * 生成数据库连接代理, 在关闭时并不关闭连接而是返回连接池.
 * 在回收连接时, 所有在 Connection 上打开的资源都会被关闭.
 */
public class ConnectionProxy extends AutoCloseableProxy<Connection> {

  private KeyedObjectPool<ConnectConfig, Connection> pool;
  private ConnectConfig config;
  private CloseableSet closelist;
  private ResourceLeak leak;


  public ConnectionProxy(KeyedObjectPool<ConnectConfig, Connection> pool,
                         Connection original,
                         ConnectConfig config) {
    super(original);

    if (pool == null)
      throw new XBosonException.NullParamException("KeyedObjectPool pool");
    if (original == null)
      throw new XBosonException.NullParamException("ConnectConfig config");

    this.pool = pool;
    this.config = config.clone();
    this.closelist = new CloseableSet();
    //this.leak = closelist.add(new ResourceLeak(this));
  }


  @Override
  protected void doClose(Connection original, Object proxy) throws Exception {
    pool.returnObject(config, original);
    closelist.close();
  }


  /**
   * 拦截 Connection 返回的对象, 比如 Statement
   * 在关闭时一起关闭
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
          throws Throwable {

    Object ret = super.invoke(proxy, method, args);
    if (ret instanceof AutoCloseable) {
      closelist.add((AutoCloseable) ret);
    }
    return ret;
  }


  @Override
  protected Class[] appendInterfaces(Class[] interfaces) {
    for (int i=0; i<interfaces.length; ++i) {
      if (interfaces[i] == Connection.class) {
        return interfaces;
      }
    }
    Class[] ret = Arrays.copyOf(interfaces, interfaces.length + 1);
    ret[interfaces.length] = Connection.class;
    return ret;
  }
}
