////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-3 下午2:03
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/basic/IFileOperatorBase.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.basic;

import com.xboson.been.XBosonException;
import com.xboson.script.IVisitByScript;

import java.util.Set;


/**
 * 文件操作模式, 模式不同, api 也完全不同
 */
public interface IFileOperatorBase<ATTR extends IFileAttribute> {

  /** 根路径, 也是默认路径 */
  String ROOT = "/";


  /**
   * 读取路径上文件(目录)的属性, 不存在的路径返回 null.
   * @throws XBosonException.IOError
   */
  ATTR readAttribute(String path);


  /**
   * 读取目录
   *
   * @param path 目录路径, 如果是文件会抛出异常
   * @return 目录中的文件列表
   * @throws XBosonException.IOError
   */
  Set<ATTR> readDir(String path);


  /**
   * 移动文件/目录到新的目录, 如果目的目录已经存在或源目录不存在会抛出异常
   *
   * @param src 源目录/文件
   * @param to 目的目录
   * @throws XBosonException.IOError
   */
  void move(String src, String to);


  /**
   * 删除文件/目录, 非空目录抛异常, 如果目录不存在则抛出异常.
   *
   * @param file
   * @throws XBosonException.IOError
   */
  void delete(String file);



  /**
   * 文件的最后修改时间, 使用 readAttribute() 可以返回
   * 可用性更强的属性, 尽可能不使用该方法.
   *
   * @param path 路径
   * @return 文件修改时间, 毫秒; 如果文件不存在返回 -1.
   * @throws XBosonException.IOError
   */
  long modifyTime(String path);


  /**
   * 创建目录, 如果上级目录是不存在的, 在必要时会自动生成这些目录.
   * 如果目录已经存在, 则什么都不做.
   *
   * @param path 路径
   */
  void makeDir(String path);
}
