////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-13 上午11:52
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/OrgApp.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app;

import com.xboson.been.CallData;
import com.xboson.been.Module;
import com.xboson.been.UrlSplit;
import com.xboson.been.XBosonException;
import com.xboson.db.IDict;
import com.xboson.db.SqlResult;
import com.xboson.fs.script.ScriptAttr;
import com.xboson.fs.script.IScriptFileSystem;
import com.xboson.script.Application;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * 每个 app 共享唯一的沙箱, 并缓存编译好的模块
 */
public class XjApp extends XjPool<XjModule> implements IDict, IScriptFileSystem {

  private ServiceScriptWrapper ssw;
  private Application runtime;
  private XjOrg org;
  private String name;
  private String id;
  private Map<String, Object> cacheData;


  XjApp(XjOrg org, String id) {
    this.org = org;
    this.id = id;
    this.cacheData = Collections.synchronizedMap(new WeakHashMap<>());
    init_app();

    try {
      this.ssw = new ServiceScriptWrapper();
      runtime = new Application(ssw.getEnvironment(), this);
    } catch (IOException|ScriptException e) {
      throw new XBosonException(e);
    }

    log.debug("App success", id);
  }


  Module buildJSModule(String path)
          throws IOException, ScriptException {
    return runtime.run(path);
  }


  void run(CallData cd, Module jsmodule, XjApi api) {
    ssw.run(cd, jsmodule, org, this, api);
  }


  public void run(CallData cd, String module_id, String api_id,
                  AppContext.ThreadLocalData tld)
          throws IOException, ScriptException {
    XjModule mod = getWithCreate(module_id);
    XjApi api = mod.getApi(api_id);
    api.fillOriginalApiCode(tld);
    api.run(cd, ApiPath.toFile(module_id, api_id));
  }


  private void init_app() {
    try (SqlResult res = org.query("open_app.sql", id)) {
      ResultSet rs = res.getResult();
      if (rs.next()) {
        if (!ZR001_ENABLE.equals(rs.getString("status"))) {
          throw new XBosonException("应用已经禁用");
        }
        name = rs.getString("appnm");
      } else {
        throw new XBosonException("找不到应用 " + id, 1202);
      }
    } catch (SQLException e) {
      throw new XBosonException(e);
    }
  }


  @Override
  protected XjModule createItem(String id) {
    return new XjModule(org, this, id);
  }


  private XjApi getApi(String path) {
    UrlSplit sp = new UrlSplit(path);
    sp.withoutSlash(true);

    XjModule mod = super.getWithCreate(sp.getName());
    return mod.getWithCreate(sp.getLast());
  }


  /**
   * 同步: 防止同一个脚本加载两次.
   */
  @Override
  public synchronized ByteBuffer readFile(String path) throws IOException {
    XjApi api = getApi(path);
    //
    // 在同一个上下文加载两次脚本, 则认为除了第一次之外的脚本都是通过 require 加载的.
    //
    if (api.isRequired() || AppContext.me().isRequired()) {
      log.debug("require() ->", path);
      api.setRequired(true);
      return ByteBuffer.wrap(api.getCode());
    }
    return ssw.wrap(api.getCode());
  }


  @Override
  public ScriptAttr readAttribute(String path) throws IOException {
    XjApi api = getApi(path);
    return api.getApiAttr();
  }


  /**
   * 通知 app 脚本内容修改
   */
  public void updateApiScript(XjApi api) {
    runtime.changed(api.getApiAttr().fullPath);
  }


  @Override
  public String getID() {
    return id;
  }


  @Override
  public String getType() {
    return "Script-FS";
  }


  /**
   * 应用范围内的缓存.
   * 返回的集合用于在脚本中保存一些常用数据, 支持多线程.
   * 不能保证一定能取出之前压入的值.
   */
  public Map<String, Object> getCacheData() {
    return cacheData;
  }


  @Override
  public String logName() {
    return "sc-core-app";
  }

}
