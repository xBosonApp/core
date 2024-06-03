////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-19 上午11:11
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/ScriptEnvConfiguration.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app;

import com.xboson.app.lib.*;

import java.util.HashMap;
import java.util.Map;


public final class ScriptEnvConfiguration {

  /**
   * 返回环境配置脚本列表,
   * 这些脚本, 在创建沙箱时被调用, 可以绑定全局变量.
   */
  static String[] environment_script() {
    return new String[] {
            "lib/array_sort_comparator.js",
            "lib/sys_functions_impl.js",
            "lib/string_functions.js",
            "lib/compatible-syntax.js",
            "lib/strutil.js",
            "lib/ide.js",
            "lib/pre-init.js",
    };
  }


  /**
   * 脚本全局对象, 在脚本中直接引用.
   */
  static Class[] global_library() {
    return new Class[] {
            MapImpl.class,
            DateImpl.class,
            ListImpl.class,
            ModuleHandleContext.class,
    };
  }


  /**
   * 脚本动态库, 在脚本中通过 require(..) 来引入
   */
  static Map<String, Class> dynamic_library() {
    Map<String, Class> mod = new HashMap<>();
    mod.put("fs",        FsImpl.class);
    mod.put("mongodb",   MongoImpl.class);
    mod.put("shell",     Shell.class);
    mod.put("schedule",  Schedule.class);
    mod.put("pm",        PmImpl.class);
    mod.put("cluster",   Cluster.class);
    mod.put("fabric",    FabricImpl.class);
    mod.put("image",     ImageImpl.class);
    mod.put("xml",       XmlImpl.class);
    mod.put("ws",        WebService.class);
    mod.put("chain",     Chain.class);
    mod.put("digest",    Digest.class);
    mod.put("crypto",    CryptoImpl.class);
    mod.put("count",     CountImpl.class);
    mod.put("lock",      LockImpl.class);
    mod.put("config",    ConfigImpl.class);
    mod.put("docker",    DockerImpl.class);
    return mod;
  }
}
