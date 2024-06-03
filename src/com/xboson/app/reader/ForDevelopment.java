////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-16 上午10:23
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/reader/ForDevelopment.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.reader;

import com.xboson.app.ApiPath;
import com.xboson.app.XjOrg;
import com.xboson.been.XBosonException;
import com.xboson.db.SqlResult;
import com.xboson.fs.script.ScriptAttr;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ForDevelopment extends AbsReadScript {

  @Override
  public ScriptFile read(XjOrg org, String app, String mod, String api) {
    log.debug("Script From DB", mod, api);
    Object[] parm = new Object[] { app, mod, api };

    try (SqlResult res = org.query("open_api.sql", parm)) {
      ResultSet rs = res.getResult();
      ScriptAttr attr = new ScriptAttr();

      if (rs.next()) {
        if (! ZR001_ENABLE.equals(rs.getString("status")) ) {
          throw new XBosonException("API 已经禁用");
        }

        ScriptFile file = makeFile(attr, rs.getString("content"));
        attr.fileSize   = file.content.length;
        attr.fileName   = api;
        attr.pathName   = '/' + mod;
        attr.fullPath   = ApiPath.toFile(mod, api);
        attr.createTime = rs.getDate("createdt").getTime();
        attr.modifyTime = rs.getDate("updatedt").getTime();

        log.debug("Load Script from DB:", mod, '/', api);
        return file;
      }
    } catch (SQLException e) {
      throw new XBosonException.XSqlException(e);
    }
    throw new XBosonException.NotFound("API:" + api);
  }


  @Override
  public String logName() {
    return "read-dev-api";
  }
}
