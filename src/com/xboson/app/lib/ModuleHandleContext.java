////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-11 下午2:10
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/ModuleHandleContext.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.been.XBosonException;
import com.xboson.script.IJSObject;
import com.xboson.util.CloseableSet;
import com.xboson.util.Tool;

import java.util.HashMap;
import java.util.Map;


/**
 * 在外部模块和服务脚本上下文之间传递对象, 线程级别的.
 * [不要传递不安全的对象]
 */
public class ModuleHandleContext implements IJSObject {

  /**
   * 注册一个该名称的 CloseableSet 对象到上下文,
   * 之后可以调用 autoClose 来自动关闭可关闭对象.
   * @see CloseableSet
   */
  public static final String CLOSE = "close_set";

  private static ThreadLocal<Map<String, Object>> moduleHandle;


  private static Map<String, Object> getMap() {
    Map<String, Object> map = moduleHandle.get();
    if (map == null) {
      map = new HashMap<>();
      moduleHandle.set(map);
    }
    return map;
  }


  /**
   * 在脚本中调用, 获取当前脚本上下文中 name 对象的引用
   */
  public Object get(String name) {
    return _get(name);
  }


  /**
   * 注册 SqlImpl 对象到当前上下文, 之后便可以引用
   */
  public static void register(String name, Object val) {
    getMap().put(name, val);
  }


  /**
   * 当对象退出脚本上下文后被关闭
   */
  public static void autoClose(AutoCloseable ac) {
    CloseableSet c = (CloseableSet) _get(CLOSE);
    if (c == null)
      throw new XBosonException.NotFound("Not found CloseableSet");
    c.add(ac);
  }


  public static Object _get(String name) {
    Object modimpl = getMap().get(name);
    if (modimpl == null) {
      throw new XBosonException.NotExist("Cannot get '" + name
              +"' Module, Maybe current user not enough authority,"
              +" or not in Application context.");
    }
    return modimpl;
  }


  @Override
  public String env_name() {
    return "moduleHandleContext";
  }


  @Override
  public boolean freeze() {
    return true;
  }


  @Override
  public void init() {
    if (moduleHandle == null) {
      moduleHandle = new ThreadLocal<>();
    }
  }


  @Override
  public void destory() {
    moduleHandle = null;
  }


  /**
   * 返回的关闭类在线程退出时调用.
   */
  public static void exitThread() {
    if (moduleHandle == null) return;
    Map<String, Object> map = moduleHandle.get();
    if (map != null) {
      map.clear();
    }
  }
}
