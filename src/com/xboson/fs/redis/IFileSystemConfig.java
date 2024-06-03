////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-22 上午8:58
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/redis/IFileSystemConfig.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.redis;

/**
 * 通过继承该接口来配置文件系统中的参数.
 * 这些参数保证不同的文件系统不会冲突, 所谓名称就是 redis 保存数据中的 key.
 */
public interface IFileSystemConfig {

  /**
   * 返回文件修改消息队列的名称;
   * 文件修改通知首先发送到队列中, 再由消息迁移线程发送到订阅通知中.
   * 该队列维护的消息保证节点离线后再次上线也可以接受到文件修改通知.
   * 例如 "XB.UI.File.ChangeQueue"
   */
  String configQueueName();


  /**
   * 返回保存文件结构信息的名称, 文件结构包括文件属性和目录结构
   * 例如: "XB.UI.File.Struct"
   */
  String configStructName();


  /**
   * 返回保存文件内容的名称
   * 例如: "XB.UI.File.CONTENT"
   */
  String configContentName();


  /**
   * 文件修改通知 '订阅名称'
   * 例如: "ui.file.change"
   */
  String configFileChangeEventName();


  /**
   * 返回本地文件存储的目录
   */
  String configLocalPath();


  /**
   * 该方法起动事件迁移队列线程, 是否单例由实现决定
   */
  void startMigrationThread();

}
