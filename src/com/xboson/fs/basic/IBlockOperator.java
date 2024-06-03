////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-3 上午10:16
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/basic/IBlockOperator.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.basic;

import com.xboson.been.XBosonException;
import com.xboson.script.lib.Buffer;


public interface IBlockOperator<ATTR extends IFileAttribute>
        extends IFileOperatorBase<ATTR> {

  /**
   * 快速读取文件内容, 不推荐使用; 尝试读取目录会抛出异常.
   * 应该使用 readAttribute()/readFileContent() 的组合来读取文件.
   *
   * @param path 路径
   * @return 文件的字节内容, 文件不存在返回 null
   * @throws XBosonException.IOError
   */
  byte[] readFile(String path);


  /**
   * 修改文件/创建文件, 同时会改变文件的修改时间;
   * 如果文件的路径中包含不存在的目录, 必要时会自动生成这些目录.
   *
   * @param path 文件
   * @param bytes 文件内容.
   * @throws XBosonException.IOError
   */
  void writeFile(String path, byte[] bytes);


  /**
   * 将 js 中的 Buffer 写入文件.
   */
  default void writeFile(String path, Buffer.JsBuffer buf) {
    writeFile(path, buf._buffer().array());
  }
}
