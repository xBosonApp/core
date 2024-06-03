////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月6日 10:21
// 原始文件路径: xBoson/src/com/xboson/script/IModuleProvider.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script;

import com.xboson.been.Module;


/**
 * 模块加载器
 */
public interface IModuleProvider extends IVisitByScript {

  int LOADER_ID_APPLICATION = 1;
  int LOADER_ID_SYS_MODULE  = 2;
  int LOADER_ID_NODE_MODULE = 3;

  String MODULE_NAME = "/node_modules";


  /**
   * 从模块路径返回模块, 并且应该将模块缓存给 Application,
   * 在必要时从 applyMod 模块中提取加载路径, 并尝试从这些路径中加载模块.
   * 必要时该方法必须同步.
   *
   * @param name 模块路径
   * @param applyMod 加载模块的模块
   * @return 如果模块加载器找不到模块应该返回 null
   * @see Application
   */
  Module getModule(String name, Module applyMod);

}
