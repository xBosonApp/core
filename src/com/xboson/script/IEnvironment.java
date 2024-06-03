////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月5日 上午11:40:08
// 原始文件路径: xBoson/src/com/xboson/script/IEnvironment.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script;

/**
 * 将对象绑定到全局,
 * 每个环境的实例中的数据都是缓存的, 除非创建新的实例, 
 * 否则环境中的变量可能在配置新的沙箱时被修改数据.
 */
public interface IEnvironment extends IConfigSandbox {

  /**
   * 插入一个配置器, 当应用启动后将配置到应用的沙箱中去.
   */
  void insertConfiger(IConfigSandbox cs);


  /**
   * 将对象注册到全局, 可以在上下文直接引用, 在必要时初始化唯一实例
   * @param jsobj
   */
  void setEnvObject(Class<? extends IJSObject> jsobj);


  /**
   * @see #setEnvObject(Class)
   * @param list 将所有对象注册到全局.
   */
  void setEnvObjectList(Class<? extends IJSObject>[] list);


  /**
   * 释放内存
   */
  void destory();

}
