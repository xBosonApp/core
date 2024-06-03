////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-25 下午12:53
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/Version.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

public class Version {

  public final static String xBoson = "2.2";
  public final static String Name = "智慧大数据开放平台";

  /**
   * 公钥 CRC, 不一致程序不能启动,
   * 控制台会输出 crc 的正确数字, 将数字替换 0xNL 中的 N 即可.
   * 一旦修改, 会导致使用该属性加密的数据不可用 (需刷新 redis).
   */
  public final static long PKCRC = Long.MAX_VALUE- 0x7fffffff9ca646f7L;

}
