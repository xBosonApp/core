////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 上午9:44
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/IDriver.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db;

/**
 * 驱动接口的实现负责创建到数据库的直接链接,
 * 一旦连接完成, 并不做后续的管理.
 */
public interface IDriver extends IDialect {

  /**
   * 返回驱动类的 className
   */
  String driverClassName();


  /**
   * 返回该驱动的唯一且简化的名字
   */
  String name();


  /**
   * 返回该驱动的唯一 ID 值
   */
  int id();


  /**
   * 使用连接配置创建到数据库的连接 URL
   */
  String getUrl(ConnectConfig config);


  /**
   * 返回数据库的默认连接端口
   */
  int port();

}
