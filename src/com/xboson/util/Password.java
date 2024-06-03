////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 下午12:46
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/Password.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;
import com.xboson.util.c0nst.IConstant;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 用来计算密码摘要, 所有密码都是用摘要来保存的, 并且不可还原
 */
public final class Password implements IConstant {

  private Password() {}
  public static final String salt = "1985-02-24 01:02:03.4";

  static AES2 ekey;
  static AES2 iekey;

  static {
    try {
      String code = "1200"; // 从配置文件读取
      String encode = encodeSha256(code, "zr_zy秘");
      ekey = new AES2(code + encode);

      String ieCode = "import&&export";
      iekey = new AES2(ieCode);
    } catch(Exception e) {
      e.printStackTrace();
      System.exit(2);
    }
  }


  /**
   * 第一版密钥
   * @param userid 用户 id
   * @param password 密码的 md5 值
   * @param datestr 来自于数据中 password_dt 字段 (密码修改/创建时间)
   * @return 返回用于持久化的密钥
   * @throws com.xboson.been.XBosonException 失败会抛出异常
   */
  public static String v1(String userid, String password, String datestr) {
    try {
      return encodeSha256(
              password,
              encodeSha256(userid, datestr.substring(0, 10)) );
    } catch(Exception e) {
      throw new XBosonException("password v1()", e);
    }
  }


  /**
   * 第一版密钥, 没有日期作为盐
   */
  public static String v1(String userid, String password) {
    return v1(userid, password, salt);
  }


  public static String encodeSha256(String a, String b) throws Exception {
    MessageDigest md = MessageDigest.getInstance(SHA256_NAME);
    md.update(a.getBytes(CHARSET));
    md.update(b.getBytes(CHARSET));
    byte[] e1 = md.digest();
    return Hex.upperHex(e1);
  }


  public static String md5lowstr(String s) {
    return Hex.lowerHex(md5(s));
  }


  public static byte[] md5(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance(MD5_NAME);
      return md.digest(s.getBytes(CHARSET));
    } catch (NoSuchAlgorithmException e) {
      throw new XBosonException("password md5", e);
    }
  }


}
