////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 下午1:47
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/ConnectPoolFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db;

import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.Tool;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * 连接池, 池中集合的连接, 有不同的数据库类型和不同的权限
 * 正确的存取依赖于 ConnectConfig 的正确参数
 */
public class ConnectPoolFactory implements
        KeyedPooledObjectFactory<ConnectConfig, Connection> {

  public final static int VALIDATE_TIMEOUT = 3500; //ms

  private DbmsFactory dbms;
  private Log log;


  public ConnectPoolFactory(DbmsFactory dbms) {
    this.dbms = dbms;
    this.log = LogFactory.create("conn-pool-fact");
  }


  @Override
  public PooledObject<Connection> makeObject(ConnectConfig key)
          throws Exception {
    Connection conn = dbms.openWithoutPool(key);
    log.debug("Make Object", conn, key);
    return new DefaultPooledObject<>(conn);
  }


  /**
   * 销毁对象，当对象池检测到某个对象的空闲时间(idle)超时，
   * 或使用完对象归还到对象池之前被检测到对象已经无效时，就会调用这个方法销毁对象。
   * 对象的销毁一般和业务相关，但必须明确的是，当调用这个方法之后，对象的生命周期必须结果。
   * 如果是对象是线程，线程必须已结束，如果是socket，socket必须已close，
   * 如果是文件操作，文件数据必须已flush，且文件正常关闭。
   */
  @Override
  public void destroyObject(ConnectConfig key, PooledObject<Connection> p)
          throws Exception {
    Connection c = p.getObject();
    if (!c.isClosed()) {
      c.close();
    }
    log.debug("Destory", c, key);
  }


  /**
   * 检测一个对象是否有效。在对象池中的对象必须是有效的，这个有效的概念是，
   * 从对象池中拿出的对象是可用的。比如，如果是socket,那么必须保证socket是连接可用的。
   * 在从对象池获取对象或归还对象到对象池时，会调用这个方法，判断对象是否有效，
   * 如果无效就会销毁。
   */
  @Override
  public boolean validateObject(ConnectConfig key, PooledObject<Connection> p) {
    boolean ret = false;
    try {
      Connection c = p.getObject();
      if (!c.isClosed()) {
        ret = c.isValid(VALIDATE_TIMEOUT);
      }
      log.debug("Validate", c, key, ret);
    } catch (SQLException e) {
      log.debug("Validate Fail", p, key, e);
    }
    return ret;
  }


  /**
   * 激活一个对象或者说启动对象的某些操作。比如，如果对象是socket，如果socket没有连接，
   * 或意外断开了，可以在这里启动socket的连接。它会在检测空闲对象的时候，
   * 如果设置了测试空闲对象是否可以用，就会调用这个方法，在borrowObject的时候也会调用。
   * 另外，如果对象是一个包含参数的对象，可以在这里进行初始化。
   * 让使用者感觉这是一个新创建的对象一样。
   */
  @Override
  public void activateObject(ConnectConfig key, PooledObject<Connection> p)
          throws Exception {
    Connection c = p.getObject();
    c.setAutoCommit(true);

    if (key.database != null) {
      c.setCatalog(key.database);
    }
    log.debug("Activate", c, key);
  }


  /**
   * 钝化一个对象。在向对象池归还一个对象时会调用这个方法。这里可以对对象做一些清理操作。
   * 比如清理掉过期的数据，下次获得对象时，不受旧数据的影响。
   */
  @Override
  public void passivateObject(ConnectConfig key, PooledObject<Connection> p)
          throws Exception {
    Connection c = p.getObject();
    if (!c.getAutoCommit()) {
      c.commit();
    }
    c.setAutoCommit(false);
    log.debug("Passivate", c, key);
  }
}
