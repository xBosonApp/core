////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月5日 下午2:32:09
// 原始文件路径: xBoson/src/com/xboson/script/IScriptFileSystem.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.script;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 每个机构的每个应用都有一个虚拟文件系统,
 * 这个接口对文件整个进行操作, 不适合大文件, 但适合脚本应用文件.
 * 这个接口被设计为 Api Script 专用.
 */
public interface IScriptFileSystem {

  /**
   * 读取路径上的文件, 返回文件内容, 如果文件不存在返回 null,
   * 只有在出现异常的情况才应该抛出 IOException
   */
  ByteBuffer readFile(String path) throws IOException;


  /**
   * 读取文件属性
   */
  ScriptAttr readAttribute(String path) throws IOException;


  /**
   * 返回文件系统的id, 不同机构的id不同
   */
  String getID();


  /**
   * 返回文件系统类型
   */
  String getType();

}
