////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月7日 10:41
// 原始文件路径: xBoson/src/com/xboson/script/EnvironmentFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script;

import com.xboson.script.lib.*;
import com.xboson.util.SysConfig;

import java.io.IOException;


public class EnvironmentFactory {

  private EnvironmentFactory() {}


  /**
   * 创建 nodejs 环境.
   */
  public static BasicEnvironment createBasic() throws IOException {
    BasicEnvironment env = new BasicEnvironment();
    configEnv(env);
    return env;
  }


  /**
   * 创建一个空环境
   */
  public static BasicEnvironment createEmptyBasic() {
    return new BasicEnvironment();
  }


  /**
   * 创建已经绑定了默认模块的 SysModules
   */
  public static SysModules createDefaultSysModules() throws IOException {
    SysModules sysmod = new SysModules();
    return setupNodeModules(sysmod);
  }


  /**
   * 将默认模块加载到 sys 中, 并返回 sysmod.
   */
  public static SysModules setupNodeModules(SysModules sysmod)
          throws IOException {

    sysmod.regClass("console",
            Console.class);
    sysmod.regClass("path",
            Path.class);
    sysmod.regClass("sys/buffer",
            Buffer.class);
    sysmod.regClass("sys/uuid",
            Uuid.class);
    sysmod.regClass("vm",
            Vm.class);
    sysmod.regClass("sys/process",
            com.xboson.script.lib.Process.class);
    sysmod.regClass("streamutil",
            StreamUtil.class);

    sysmod.loadLib("process",
            "lib/process.js");
    sysmod.loadLib("sys/util",
            "lib/sysutil.js");
    sysmod.loadLib("sys/json",
            "lib/JSON.js");
    sysmod.loadLib("util",
            "lib/util.js");
    sysmod.loadLib("assert",
            "lib/assert.js");
    sysmod.loadLib("events",
            "lib/events.js");
    sysmod.loadLib("buffer",
            "lib/buffer.js");
    sysmod.loadLib("querystring",
            "lib/querystring.js");
    sysmod.loadLib("punycode",
            "lib/punycode.js");
    sysmod.loadLib("url",
            "lib/url.js");
    sysmod.loadLib("uuid",
            "lib/uuid.js");

    return sysmod;
  }


  /**
   * 将 env 配置为 nodejs 环境.
   */
  public static BasicEnvironment configEnv(BasicEnvironment env)
          throws IOException {
    SysModules sysmod = createDefaultSysModules();
    env.insertConfiger(sysmod);
    env.setEnvObject(Console.class);
    return env;
  }


  /**
   * 将 env 配置为 nodejs 环境.
   */
  public static BasicEnvironment configEnv(BasicEnvironment env, SysModules sysmod)
          throws IOException {
    env.insertConfiger(sysmod);
    env.setEnvObject(Console.class);
    return env;
  }

}
