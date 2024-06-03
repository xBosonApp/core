////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-19 上午11:41
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/ui/IFileChangeListener.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.redis;


/**
 * 仅对 ui 模块使用的文件通知器, 当其他节点对文件有操作,
 * 会通过网络将消息发送到本地文件系统, 通过该接口接收这些消息.
 *
 * @see FileModifyHandle
 */
public interface IFileChangeListener {

  /**
   * 通知文件改动或创建
   * @param vfile 改动的文件路径
   */
  void noticeModifyContent(String vfile);


  /**
   * 通知目录被创建
   * @param vdirname 目录路径
   */
  void noticeMakeDir(String vdirname);


  /**
   * 通知文件被删除
   * @param vfile 删除的文件路径
   */
  void noticeDelete(String vfile);


  /**
   * 通知文件被移动
   * @param form 源文件名
   * @param to 目的文件名
   */
  void noticeMove(String form, String to);

}
