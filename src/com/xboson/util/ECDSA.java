////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-2-2 上午9:38
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/ECDSA.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;


/**
 * ECDSA 的对称加密算法辅助类, 使用 PEM 格式.
 *
 * 该对象依赖 BouncyCastleProvider 类,
 * BouncyCastleProvider 在 bcprov-jdk15on-1.59.jar 包,
 * 该包已经集成在 fabric-sdk-java 中.
 */
public class ECDSA {

  private static ECDSA instance;
  private KeyFactory fact;
  private BouncyCastleProvider provider;


  private ECDSA() {
    try {
      provider = new BouncyCastleProvider();
      fact = KeyFactory.getInstance("ECDSA", provider);
    } catch (NoSuchAlgorithmException e) {
      Tool.pl("WARN", e);
    }
  }


  public static ECDSA me() {
    if (instance == null) {
      synchronized (ECDSA.class) {
        if (instance == null) {
          instance = new ECDSA();
        }
      }
    }
    return instance;
  }


  /**
   * 将 pem 格式的私钥解析为私钥对象
   */
  public PrivateKey parsePrivateKey(String pem) {
    try {
      if (fact == null)
        fact = KeyFactory.getInstance("ECDSA", provider);

      String key = formatPrivateKey(pem);
      byte[] encoded = DatatypeConverter.parseBase64Binary(key);
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      return fact.generatePrivate(keySpec);

    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw new XBosonException(e);
    } catch (IOException e) {
      throw new XBosonException.IOError(e);
    }
  }


  public String formatPrivateKey(String pem) throws IOException {
    BufferedReader br = new BufferedReader(new StringReader(pem));
    StringBuilder builder = new StringBuilder();
    boolean begin = false;
    boolean end = false;

    for (String line = br.readLine(); line != null; line = br.readLine()) {
      if (line.startsWith("-----BEGIN ")
              && line.endsWith(" PRIVATE KEY-----")) {
        begin = true;
        break;
      }
    }

    if (begin) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (line.startsWith("-----END ")
                && line.endsWith(" PRIVATE KEY-----")) {
          end = true;
          break;
        }
        builder.append(line);
      }
    } else {
      throw new IOException("Bad PRIVATE KEY Format, Can not find the head");
    }
    if (!end) {
      throw new IOException("Bad PRIVATE KEY Format, Can not find the tail");
    }
    return builder.toString();
  }
}
