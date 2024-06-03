////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-23 上午8:35
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/AES2.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;
import com.xboson.util.c0nst.IConstant;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;


/**
 * api 使用该算法加密, 存储该对象的实例使用效率最佳
 */
public class AES2 implements IConstant {

  private SecretKeySpec key;


  /**
   * 创建 aes 加密实例, 该对象可以缓存, 并且多线程安全
   * @param keystr 密钥
   */
  public AES2(String keystr) {
    init(keystr.getBytes(CHARSET));
  }


  /**
   * 创建 aes 加密实例, 该对象可以缓存, 并且多线程安全
   * @param keybin 密钥
   */
  public AES2(byte[] keybin) {
    init(keybin);
  }


  private void init(byte[] keybin) {
    try {
      KeyGenerator kgen = KeyGenerator.getInstance(AES_NAME);
      SecureRandom secureRandom = SecureRandom.getInstance(SHA1_PRNG_NAME);
      secureRandom.setSeed(keybin);
      kgen.init(128, secureRandom);

      SecretKey secretKey = kgen.generateKey();
      byte[] enCodeFormat = secretKey.getEncoded();
      key = new SecretKeySpec(enCodeFormat, AES_NAME);
    } catch(Exception e) {
      throw new XBosonException(e);
    }
  }


  /**
   * 返回加密后数据的 HEX 字符串形式
   * <b>encrypt 与 decrypt 不对称, 这是为了另一个使用该算法的类而做的优化</b>
   */
  public String encrypt(String code) {
    return Hex.upperHex(encryptBin(code.getBytes(CHARSET)));
  }


  /**
   * 返回解密后的二进制数据, mi 为 HEX 形式的加密数据
   */
  public byte[] decrypt(String mi) {
    return decryptBin(Hex.parse(mi));
  }


  /**
   * 加密二进制数据, 返回加密原始数据
   */
  public byte[] encryptBin(byte[] srcBytes) {
    try {
      Cipher cipher = Cipher.getInstance(AES_NAME);
      cipher.init(ENCRYPT_MODE, key);
      return cipher.doFinal(srcBytes);
    } catch (Exception e) {
      throw new XBosonException(e);
    }
  }


  /**
   * 解密二进制数据, 返回原始数据
   */
  public byte[] decryptBin(byte[] secret) {
    try {
      Cipher cipher = Cipher.getInstance(AES_NAME);
      cipher.init(DECRYPT_MODE, key);
      return cipher.doFinal(secret);
    } catch (Exception e) {
      throw new XBosonException(e);
    }
  }
}
