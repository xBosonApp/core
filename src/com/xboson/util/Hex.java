////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 下午1:07
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/Hex.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;
import com.xboson.chain.Base58;
import com.xboson.event.Names;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.Base64;


/**
 * 字节数组 16 进制字符串转换
 */
public class Hex {
  private static final Base64.Encoder b64e = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder b64d = Base64.getUrlDecoder();

  private static final char[] hexUpCode = "0123456789ABCDEF".toCharArray();
  private static final char[] hexLoCode = "0123456789abcdef".toCharArray();


  /**
   * 可使用的二进制编码集名称
   */
  public interface Names {
    String HEX    = "hex";
    String BASE58 = "base58";
    String BASE64 = "base64";
    String BASE64URL = "base64url";
  }


  public static String lowerHex(byte[] b) {
    return toHex(b, hexLoCode);
  }


  public static String upperHex(byte[] b) {
    return toHex(b, hexUpCode);
  }


  public static String toHex(byte[] bytes, char[] map) {
    char[] ch = new char[ bytes.length * 2 ];
    for (int i=0; i<bytes.length; ++i) {
      byte b = bytes[i];
      ch[  i<<1    ] = map[ b >> 4 & 15 ];
      ch[ (i<<1)+1 ] = map[ b & 15 ];
    }
    return new String(ch);
  }


  public static byte[] parse(String hexstr) {
    return DatatypeConverter.parseHexBinary(hexstr);
  }


  /**
   * base64 url without padding 编码
   */
  public static String encode64(byte[] bin) {
    return b64e.encodeToString(bin);
  }


  /**
   * base64 url 解码
   */
  public static byte[]  decode64(String base64str) {
    return b64d.decode(base64str);
  }


  /**
   * 按照指定的编码将 data 解密为字节数组
   * @param coding 编码名: base58/base64/base64url/hex
   * @param data 已编码字符串
   * @see Names 可用编码
   */
  public static byte[] decode(String coding, String data) {
    switch (coding.toLowerCase()) {
      case Names.BASE58:
        return Base58.decode(data);

      case Names.BASE64:
        return Base64.getDecoder().decode(data);

      case Names.BASE64URL:
        return b64d.decode(data);

      case Names.HEX:
        return parse(data);

      default:
        throw new XBosonException.BadParameter(
                "String coding", "unknow coding: "+ coding);
    }
  }


  /**
   * 编码字节数组为字符串
   * @see Names 可用编码
   */
  public static String encode(String coding, byte[] data) {
    switch (coding.toLowerCase()) {
      case Names.BASE58:
        return Base58.encode(data);

      case Names.BASE64:
        return Base64.getEncoder().encodeToString(data);

      case Names.BASE64URL:
        return b64e.encodeToString(data);

      case Names.HEX:
        return upperHex(data);

      default:
        throw new XBosonException.BadParameter(
                "String coding", "unknow coding: "+ coding);
    }
  }


  public static byte[] join(byte[] a, byte[] b) {
    byte[] c = new byte[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }


  /**
   * java 对象转字节码 (使用序列化算法)
   */
  public static byte[] toBytes(Object obj) throws IOException {
    ByteArrayOutputStream obyte = new ByteArrayOutputStream();
    ObjectOutputStream oobj = new ObjectOutputStream(obyte);
    oobj.writeObject(obj);
    oobj.flush();
    return obyte.toByteArray();
  }


  /**
   * 字节码转 java 对象
   */
  public static Object fromBytes(byte[] bin)
          throws IOException, ClassNotFoundException
  {
    ByteArrayInputStream ibyte = new ByteArrayInputStream(bin);
    ObjectInputStream iobj = new ObjectInputStream(ibyte);
    return iobj.readObject();
  }


  public static byte[] toBytesWithoutErr(Object obj) {
    try {
      return toBytes(obj);
    } catch (IOException e) {
      throw new XBosonException.IOError(e);
    }
  }
}
