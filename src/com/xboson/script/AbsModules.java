////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-25 下午3:18
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/script/AbsModules.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script;

import com.xboson.been.Module;

import java.util.ArrayList;
import java.util.List;


public abstract class AbsModules implements IConfigurableModuleProvider {


  /**
   * 该方法直接调用两个参数的 getModule
   * @see #getModule(String, Module)
   */
  public final Module getModule(String name) {
    return getModule(name, null);
  }


  /**
   * 返回所有可能的脚本加载路径
   * @param path_name 以该目录为基础
   * @return 路径数组
   */
  public static String[] get_module_paths(String path_name) {
    if (path_name == null) return null;

    List<String> paths = new ArrayList<>();
    paths.add(path_name);
    int i = path_name.lastIndexOf("/", path_name.length());

    while (i >= 0) {
      paths.add(path_name.substring(0, i) + MODULE_NAME);
      i = path_name.lastIndexOf("/", i-1);
    }
    return paths.toArray(new String[paths.size()]);
  }

}
