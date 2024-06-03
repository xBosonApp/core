////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-19 上午9:28
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/init/install/step/ConfigCoreDB.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.init.install.step;

import com.xboson.db.ConnectConfig;
import com.xboson.db.DbmsFactory;
import com.xboson.db.IDriver;
import com.xboson.init.install.HttpData;
import com.xboson.init.install.IStep;
import com.xboson.util.Tool;

import java.sql.Connection;


public class ConfigCoreDB implements IStep {

  @Override
  public int order() {
    return 3;
  }


  @Override
  public boolean gotoNext(HttpData data) {
    ConnectConfig db = data.cf.db;
    db.setHost(data.req.getParameter("host"));
    db.setPort(data.req.getParameter("port"));
    db.setDbname(data.req.getParameter("dbname"));
    db.setUsername(data.req.getParameter("username"));
    db.setPassword(data.req.getParameter("password"));
    db.setDatabase("");

    String catalog = data.req.getParameter("database");
    if (Tool.isNulStr(catalog)) {
      data.msg = "必须指定 database/catalog/schema 名称";
      return false;
    }

    String createdb = data.req.getParameter("createdb");
    boolean autocreate = createdb != null && "1".equals(createdb);

    try (Connection conn = DbmsFactory.me().openWithoutPool(db)) {
      try {
        conn.setCatalog(catalog);
      } catch (Exception e) {
        if (!autocreate)
          throw e;

        IDriver d = DbmsFactory.me().getDriver(db);
        String sql = d.createCatalog(catalog);
        conn.createStatement().execute(sql);

        data.msg = "创建了 " + catalog;
        return false;
      }
      db.setDatabase(catalog);
      return true;
    } catch(Exception e) {
      data.msg = e.getMessage();
      e.printStackTrace();
    }
    return false;
  }


  @Override
  public String getPage(HttpData data) {
    return "db.jsp";
  }
}
