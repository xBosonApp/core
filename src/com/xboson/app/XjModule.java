////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-22 下午2:02
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/XjModule.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app;

import com.xboson.been.XBosonException;
import com.xboson.db.IDict;
import com.xboson.db.SqlResult;

import java.sql.ResultSet;
import java.sql.SQLException;


public class XjModule extends XjPool<XjApi> implements IDict {

  private XjOrg org;
  private XjApp app;
  private String name;
  private String id;


  XjModule(XjOrg org, XjApp app, String id) {
    this.id  = id;
    this.org = org;
    this.app = app;
    init_module();
    log.debug("Module success", id);
  }


  private void init_module() {
    try (SqlResult res = org.query("open_module.sql", app.getID(), id)) {
      ResultSet rs = res.getResult();
      if (rs.next()) {
        if (! ZR001_ENABLE.equals(rs.getString("status")) ) {
          throw new XBosonException("模块已经禁用");
        }
        name = rs.getString("modulenm");
      } else {
        throw new XBosonException.NotFound("Module:" + id);
      }
    } catch (SQLException e) {
      throw new XBosonException.XSqlException(e);
    }
  }


  public XjApi getApi(String api_id) {
    return getWithCreate(api_id);
  }


  @Override
  protected XjApi createItem(String apiid) {
    return new XjApi(org, app, this, apiid);
  }


  public String id() {
    return id;
  }


  @Override
  public String logName() {
    return "sc-core-module";
  }

}
