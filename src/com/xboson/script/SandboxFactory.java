////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月5日 上午10:52:43
// 原始文件路径: xBoson/src/com/xboson/script/SandboxFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script;

import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.script.safe.BlockAllFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * 线程安全, 并对线程优化的沙箱工厂
 */
public class SandboxFactory {

  private static ThreadLocal<NashornScriptEngineFactory> seml = new ThreadLocal<NashornScriptEngineFactory>();
  private static Log log = LogFactory.create();
  private static BlockAllFilter blockall = new BlockAllFilter();


  /**
   * 创建一个独立的沙箱, 该沙箱对象与线程绑定.
   * 在同一个线程上调用该方法, 总是返回唯一的沙箱对象.
   *
   * @throws ScriptException
   */
  public static Sandbox create() throws ScriptException {
    ScriptEngine se = getEM().getScriptEngine(blockall);
    return new Sandbox(se);
  }


  public static NashornScriptEngineFactory getEM() {
    NashornScriptEngineFactory em = seml.get();
    if (em == null) {
      em = new NashornScriptEngineFactory();
      seml.set(em);
    }
    return em;
  }


  public static void version() {
    NashornScriptEngineFactory n = getEM();
    log.info("Script ENGINE:",
        "[" + n.getEngineName(), n.getEngineVersion() + "]",
        "[" + n.getLanguageName(), n.getLanguageVersion() + "]");
  }
}
