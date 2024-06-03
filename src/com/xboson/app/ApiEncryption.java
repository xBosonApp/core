////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-23 上午8:39
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/ApiEncryption.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app;

import com.xboson.util.AES2;
import com.xboson.util.Password;


/**
 * API 脚本加密/解密
 */
public class ApiEncryption {

  private static AES2 ekey;


  static {
    try {
      String code = "1200"; // 从配置文件读取
      String encode = Password.encodeSha256(code, "zr_zy秘");
      ekey = new AES2(code + encode);
    } catch(Exception e) {
      e.printStackTrace();
      System.exit(2);
    }
  }


  public static String encryptApi(String code) {
    return ekey.encrypt(code);
  }


  public static byte[] decryptApi(String mi) {
    return ekey.decrypt(mi);
  }

}
