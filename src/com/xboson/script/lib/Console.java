/* CatfoOD yanming-sohu@sohu.com Q.412475540 */
////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月5日 上午11:50:29
// 原始文件路径: xBoson/src/com/xboson/script/lib/Console.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script.lib;

import com.xboson.log.Level;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.script.JSObject;
import com.xboson.util.Tool;
import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.Arrays;


public class Console extends JSObject {

  private Log log;


  public Console() {
    log = LogFactory.create("script.console");
  }


  public Console(String name) {
    log = LogFactory.create(name);
  }


  public Console create(String name) {
    if (name != null) {
      return new Console(name);
    } else {
      return new Console();
    }
  }


  @Override
  public String env_name() {
    return "console";
  }


  public Console info(Object ...msg) {
    log.logs(Level.INFO, join(msg));
    return this;
  }


  public Console log(Object ...msg) {
    log.logs(Level.INFO, join(msg));
    return this;
  }


  public Console debug(Object ...msg) {
    log.logs(Level.DEBUG, join(msg));
    return this;
  }


  public Console error(Object ...msg) {
    log.logs(Level.ERR, join(msg));
    return this;
  }


  public Console warn(Object ...msg) {
    log.logs(Level.WARN, join(msg));
    return this;
  }


  public Console fatal(Object ...msg) {
    log.logs(Level.FATAL, join(msg));
    return this;
  }


  public Console trace(Object ...msg) {
    return debug(msg);
  }


  private Object[] join(Object ...msg) {
    Object[] ret = new Object[msg.length];

    for (int i=0; i<msg.length; ++i) {
      if (msg[i] instanceof ScriptObjectMirror) {
        ScriptObjectMirror js = (ScriptObjectMirror) msg[i];

        if (!js.isEmpty()) {
          ret[i] = Tool.beautifyJson(ScriptObjectMirror.class, js);
        } else {
          ret[i] = js.toString();
        }
      }
      else {
        ret[i] = msg[i];
      }
    }
    return ret;
  }
}
