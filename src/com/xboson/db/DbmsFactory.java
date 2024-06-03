////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 上午9:42
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/driver/DbmsFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db;

import com.xboson.been.XBosonException;
import com.xboson.db.driver.Mysql;
import com.xboson.event.OnExitHandle;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 负责管理多个数据库的驱动, 连接池, 异常处理
 * 事务管理
 */
public class DbmsFactory extends OnExitHandle {

  private static DbmsFactory instance;

  public static DbmsFactory me() {
    if (instance == null) {
      synchronized (DbmsFactory.class) {
        if (instance == null) {
          instance = new DbmsFactory();
        }
      }
    }
    return instance;
  }


  private KeyedObjectPool<ConnectConfig, Connection> pool;
  private Map<Integer, IDriver> idmap;
  private Map<String, IDriver> namemap;
  private Log log;


  private DbmsFactory() {
    idmap = new ConcurrentHashMap<>();
    namemap = new ConcurrentHashMap<>();
    log = LogFactory.create("dbms-factory");

    DBPoolConfig pc = SysConfig.me().readConfig().dbpool;
    ConnectPoolFactory pf = new ConnectPoolFactory(this);
    pool = new GenericKeyedObjectPool<>(pf, pc);
  }


  @Override
  protected synchronized void exit() {
    pool.close();
    pool = null;
    idmap = null;
    namemap = null;

    try {
      Enumeration<Driver> e = DriverManager.getDrivers();
      while (e.hasMoreElements()) {
        DriverManager.deregisterDriver(e.nextElement());
      }
    } catch(Exception e) {
      log.error(e);
    }
  }


  private void check_state() {
    if (pool == null)
      throw new XBosonException.Closed("DbmsFactory");
  }


  /**
   * 注册耽搁数据库驱动
   */
  public void registering(IDriver dr) {
    if (dr == null)
      throw new XBosonException.NullParamException("IDriver dr");

    log.info("Register DBMS Driver ", dr.name(), "["+ dr.id() +']');

    idmap.put(dr.id(), dr);
    namemap.put(dr.name(), dr);
    namemap.put(dr.driverClassName(), dr);
  }


  /**
   * 注册所有数据库驱动
   */
  public void registeringDefaultDriver() {
    Iterator<Class> it = null;
    try {
      Set<Class> cl = Tool.findPackage(Mysql.class);
      it = cl.iterator();
    } catch(Exception e) {
      log.error("registeringDefaultDriver", e);
      e.printStackTrace();
      return;
    }

    while (it.hasNext()) {
      Class c = it.next();
      try {
        IDriver dr = (IDriver) c.newInstance();
        Class.forName(dr.driverClassName());
        registering(dr);
      } catch(Exception e) {
        log.error("Register DBMS Driver", e);
      }
    }
  }


  /**
   * 从连接配置中生成完整的可用连接.
   * 从这里生成的连接, 没有池化.
   *
   * @param config host, dbname, username 属性必须有效
   */
  public Connection openWithoutPool(ConnectConfig config)
          throws SQLException {
    if (config == null)
      throw new XBosonException.NullParamException("ConnectConfig config");

    IDriver dr = getDriver(config);
    String url = getUrl(dr, config);

    Connection conn =
            DriverManager.getConnection(
                    url, config.username, config.password);
    return conn;
  }


  /**
   * 从连接配置中生成完整的可用连接, 连接已经池化
   *
   * @param config host, dbname, username 属性必须有效
   */
  public Connection open(ConnectConfig config) throws SQLException {
    if (config == null)
      throw new XBosonException.NullParamException("ConnectConfig config");

    //
    // 此处不可删除, 有实际用处
    //
    IDriver dr = getDriver(config);
    getUrl(dr, config);

    try {
      check_state();
      Connection original = pool.borrowObject(config);
      ConnectionProxy cp = new ConnectionProxy(pool, original, config);
      return cp.getProxy();

    } catch(Exception e) {
      log.error("open fail", Tool.allStack(e));
      throw new XBosonException.XSqlException(
              "open connection " + config.host +":"+ config.port, e);
    }
  }


  /**
   * 通过该方法生成数据库的 URL 连接,
   * 如果未设置 port 则使用数据库的默认端口.
   */
  public String getUrl(IDriver dr, ConnectConfig config) {
    if (config.host == null)
      throw new XBosonException.NullParamException("config.host");

    if (config.database == null)
      throw new XBosonException.NullParamException("config.database");

    if (config.port == null)
      config.port = Integer.toString(dr.port());

    return dr.getUrl(config);
  }


  /**
   * 调用该方法获取 DBMS 的驱动,
   * 该方法会尽可能补全 dbname/dbid 属性.
   */
  public IDriver getDriver(ConnectConfig config) {
    IDriver dr = null;
    check_state();

    if (namemap.size() <= 0) {
      throw new XBosonException("No DB driver in DbmsFactory");
    }

    if (config.dbname != null) {
      dr = namemap.get(config.dbname);
      if (dr != null && config.dbid == null) {
        config.setDbid(dr.id());
      }
    }

    if (dr == null && config.dbid != null) {
      dr = idmap.get(config.dbid);
      config.setDbname(dr.name());
    }

    if (dr == null) {
      throw new XBosonException.NullParamException(
              "config.dbname[dbid] " + config);
    }

    return dr;
  }


  /**
   * 使用 name 或 id 查询符合的驱动
   * @param nameOrID
   * @return
   */
  public IDriver findDriver(String nameOrID) {
    check_state();

    int id = -1;
    try {
      id = Integer.parseInt(nameOrID);
    } catch(Exception e) {}

    IDriver ret = null;
    if (id >= 0) {
      ret = idmap.get(id);
    }
    if (ret == null) {
      ret = namemap.get(id);
    }
    return ret;
  }

}
