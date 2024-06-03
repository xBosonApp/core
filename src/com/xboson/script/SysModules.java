////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月11日 10:31
// 原始文件路径: xBoson/src/com/xboson/script/SysModules.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script;

import com.xboson.been.Module;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.Tool;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * require 方法提供的系统模块, 需要在初始化时将外部模块注入.
 * 每个环境只支持一个 SysModules 模块, 切只能绑定一个运行时.
 */
public class SysModules extends AbsModules implements IModuleProvider {

  private Log log = LogFactory.create();
  private ICodeRunner runner;

  /** 未实例化的 java 对象类型的 js 模块 */
  private Map<String, AbsWrapScript> modules;


  public SysModules() {
    modules = new ConcurrentHashMap<>();
  }


  /**
   * 返回一个本地模块
   */
  public Module getModule(String name, Module apply) {
    if (name == null)
      throw new NullPointerException("name");

    AbsWrapScript module = modules.get(name);
    if (module == null)
      return null;

    Module mod = runner.run(module);
    mod.loaderid = LOADER_ID_SYS_MODULE;
    modules.remove(name);
    return mod;
  }


  /**
   * 注册模块类, 在需要时创建类的实例
   */
  public void regClass(String name, Class<?> clazz) {
    if (name == null)
      throw new NullPointerException("name");
    if (clazz == null)
      throw new NullPointerException("clazz");

    modules.put(name, new NativeWrapScript(name, clazz));
  }


  /**
   * 注册集合中的模块类, 在需要时创建类的实例
   */
  public void regClasses(Map<String, Class> map) {
    for (Map.Entry<String, Class> en : map.entrySet()) {
      regClass(en.getKey(), en.getValue());
    }
  }


  /**
   * 注册 java 模块, 模块已经实例化
   */
  public void regLib(String name, Object lib) {
    modules.put(name, new NativeWrapScript(name, lib));
  }


  /**
   * 读取 js 脚本, 并作为系统模块
   * @param name - 模块名称
   * @param jsfile - js 文件路径, 相对于 SysModules 类
   */
  public void loadLib(String name, String jsfile) throws IOException {
    StringBufferOutputStream buf = Tool.readFileFromResource(getClass(), jsfile);
    modules.put(name, new WrapJavaScript(buf.toBuffer(), name));
  }


  /**
   * 调用 'bootstrap.js' 中的 __set_sys_module_provider 方法来初始化环境.
   */
  public void config(Sandbox box, ICodeRunner runner) throws ScriptException {
    box.invokeFunction("__set_sys_module_provider", this);
    this.runner = runner;
  }
}
