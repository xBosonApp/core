////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-13 上午11:50
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/AppPool.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app;

import com.xboson.app.reader.AbsReadScript;
import com.xboson.been.XBosonException;
import com.xboson.db.ConnectConfig;
import com.xboson.util.SysConfig;


/**
 * 应用池维护机构下的所有 app, 这些对象已经缓存, 并且线程安全.
 */
public class AppPool extends XjPool<XjOrg> {

  private ConnectConfig dbcc;
  private AbsReadScript script_reader;


  public AppPool(AbsReadScript reader) {
    if (reader == null) {
      throw new XBosonException.NullParamException("AbsReadScript reader");
    }
    this.dbcc = SysConfig.me().readConfig().db;
    this.script_reader = reader;
  }


  @Override
  protected XjOrg createItem(String id) {
    return new XjOrg(dbcc, id, script_reader);
  }


  /**
   * 创建或获取缓存的 org
   * @param id
   * @return
   */
  public XjOrg getOrg(String id) {
    return super.getWithCreate(id);
  }


  @Override
  public String logName() {
    return "sc-core-pool";
  }
}
