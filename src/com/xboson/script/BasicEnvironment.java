/* CatfoOD  yanming-sohu@sohu.com Q.412475540 */
////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月5日 上午11:33:42
// 原始文件路径: xBoson/src/com/xboson/script/BasicEnvironment.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script;

import java.util.*;

import javax.script.Bindings;
import javax.script.ScriptException;

import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.Tool;


/**
 * 创建基础 js 环境, 并提供可配置的接口,
 * 含有网络连接的复杂对象环境可以继承该类.
 */
public class BasicEnvironment implements IEnvironment {

  private final Log log;
  private List<IJSObject> objs;
  private Set<IConfigSandbox> configer;
  private IModuleProvider sys_mod;


  /**
   * 创建一个空环境
   */
  public BasicEnvironment() {
    log      = LogFactory.create();
    objs     = new ArrayList<>(30);
    configer = new HashSet<>(10);
  }


  /**
   * 创建环境并将一个沙箱配置器加入环境中
   * @param p
   */
  public BasicEnvironment(IConfigSandbox p) {
    this();
    insertConfiger(p);
  }


  public void insertConfiger(IConfigSandbox cs) {
    configer.add(cs);
  }


  public void setEnvObjectList(Class<? extends IJSObject>[] list) {
    for (int i=0; i<list.length; ++i) {
      setEnvObject(list[i]);
    }
  }


  public void setEnvObject(Class<? extends IJSObject> c) {
    try {
      IJSObject jso = (IJSObject) c.newInstance();
      jso.init();
      objs.add(jso);
    } catch(Exception e) {
      log.error(e.getMessage());
    }
  }


  public void config(Sandbox box, ICodeRunner runner) throws ScriptException {
    Bindings bind = box.getBindings();

    //
    // 配置 js 库对象.
    //
    Iterator<IJSObject> it = objs.iterator();
    while (it.hasNext()) {
      IJSObject o = it.next();
      String name = o.env_name();
      bind.put(name, o);
    }

    //
    // 使用配置器配置沙箱
    //
    Iterator<IConfigSandbox> it_config = configer.iterator();
    while (it_config.hasNext()) {
      IConfigSandbox ics = it_config.next();
      try {
        ics.config(box, runner);
      } catch (Exception e) {
        log.error("Config sanbox", ics, Tool.allStack(e));
      }
    }
  }


  public void destory() {
    Iterator<IJSObject> it = objs.iterator();

    while (it.hasNext()) {
      IJSObject o = it.next();
      try {
        o.destory();
      } catch(Exception e) {
        log.error("Destory", o, e);
      }
      it.remove();
    }

    configer = null;
    objs = null;
  }


  @Override
  protected void finalize() throws Throwable {
    destory();
  }

}
