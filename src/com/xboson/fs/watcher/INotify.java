////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-15 下午2:26
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/watcher/INotify.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.watcher;

import java.io.IOException;
import java.nio.file.WatchEvent;


/**
 * 当文件有变动时, 通知该对象
 */
public interface INotify {

  /**
   * 通知文件的变动
   * @param basename 正在监听的目录
   * @param filename 改变的文件
   * @param event 变动消息对象
   * @param kind 变动类型
   * @throws IOException
   */
  void notify(String basename, String filename,
              WatchEvent event, WatchEvent.Kind kind) throws IOException;


  /**
   * 监听的目录被删除时调用
   * @param basename 正在监听的目录
   */
  void remove(String basename);
}
