////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-19 下午7:35
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/script/lib/Process.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script.lib;

import com.xboson.script.SandboxFactory;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;


public class Process {

  public long[] hrtime() {
    return new long[] { System.currentTimeMillis() / 1000, System.nanoTime() };
  }


  public String engineVersion() {
    NashornScriptEngineFactory ef = SandboxFactory.getEM();
    return ef.getEngineVersion();
  }


  public String languageVersion() {
    NashornScriptEngineFactory ef = SandboxFactory.getEM();
    return ef.getLanguageVersion();
  }


  /**
   * 多线程同步, 在 lockTatget 对象上枷锁, 并执行 callback 方法, callback 返回后解锁.
   * 如果将 js 对象作为锁对象, js 对象会被 ScriptObjectMirror 包装, 直接作为锁不起作用.
   *
   * @param lockTatget
   * @param callback
   * @return 返回 callback 返回的对象.
   */
  public Object lock(Object lockTatget, ScriptObjectMirror callback) {
    synchronized (lockTatget) {
      return callback.call(lockTatget);
    }
  }


  public Object lock(ScriptObjectMirror lt, ScriptObjectMirror cb) {
    return lock(ScriptUtils.unwrap(lt), cb);
  }
}
