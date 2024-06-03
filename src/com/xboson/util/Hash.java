////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-13 下午5:41
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/Hash.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;
import com.xboson.chain.SignNode;
import com.xboson.util.c0nst.IConstant;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


/**
 * 摘要封装, 支持除了 byte[] 以外的摘要计算
 */
public class Hash {

  public static final String DEFAULT_ALGORITHM = "SHA-256";

  private final MessageDigest md;
  private byte[] digest;


  /**
   * 默认使用 SHA-256 计算摘要
   */
  public Hash() {
    this(DEFAULT_ALGORITHM);
  }


  public Hash(String algorithm) {
    try {
      md = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new XBosonException(e);
    }
  }


  public void update(String s) {
    md.update(s.getBytes(IConstant.CHARSET));
  }


  public void update(byte[] b) {
    md.update(b);
  }


  public void update(Date d) {
    update(d.getTime());
  }


  public void update(long l) {
    md.update((byte) (l & 0xFF));
    md.update((byte) ((l>>8 ) & 0xFF));
    md.update((byte) ((l>>16) & 0xFF));
    md.update((byte) ((l>>24) & 0xFF));

    md.update((byte) ((l>>32) & 0xFF));
    md.update((byte) ((l>>40) & 0xFF));
    md.update((byte) ((l>>48) & 0xFF));
    md.update((byte) ((l>>56) & 0xFF));
  }


  public void update(int l) {
    md.update((byte) (l & 0xFF));
    md.update((byte) ((l>>8 ) & 0xFF));
    md.update((byte) ((l>>16) & 0xFF));
    md.update((byte) ((l>>24) & 0xFF));
  }


  /**
   * sn 不能为空
   */
  public void update(SignNode sn) {
    SignNode n = sn;
    do {
      update(n.id);
      update(n.sign);
      n = n.next;
    } while (n != null);
  }


  public byte[] digest() {
    if (digest == null) {
      digest = md.digest();
    }
    return digest;
  }


  /**
   * 返回摘要的 16 进制字符串.
   */
  public String digestStr() {
    return Hex.lowerHex(md.digest());
  }


  public static byte[] sha256(byte[] i) {
    Hash h = new Hash();
    h.update(i);
    return h.digest();
  }


  public static byte[] join(byte[] a, byte[] b) {
    return Hex.join(a, b);
  }
}
