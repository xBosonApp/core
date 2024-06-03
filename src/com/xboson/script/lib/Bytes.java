////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-18 上午9:32
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/script/lib/Bytes.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script.lib;

import com.xboson.been.IJson;
import com.xboson.been.JsonHelper;
import com.xboson.util.Hex;


/**
 * 字节数组对象, 可以将字节数组和字符串互相转换.
 * 不可直接导出到 js 环境.
 */
public class Bytes implements IJson {
  private byte[] key;
  private String s_key;


  /**
   * @param base64url - base64 编码字符串
   */
  public Bytes(String base64url) {
    this.s_key = base64url;
  }


  public Bytes(byte[] k) {
    this.key = k;
  }


  /**
   * 0 字节数组
   */
  public Bytes() {
    this.key = new byte[0];
  }


  @Override
  public String toString() {
    if (s_key == null) {
      s_key = Hex.encode64(key);
    }
    return s_key;
  }


  public String toString(String coding) {
    return Hex.encode(coding, bin());
  }


  public String toHex() {
    if (key == null) {
      if (s_key == null) {
        return null;
      }
      bin();
    }
    return Hex.upperHex(key);
  }


  public byte[] bin() {
    if (key == null) {
      key = Hex.decode64(s_key);
    }
    return key;
  }


  public String toJavaString() {
    return new String(bin());
  }


  @Override
  public String toJSON() {
    return JsonHelper.toJSON(toString());
  }


  public Bytes concat(Bytes other) {
    byte[] a = this.bin();
    byte[] b = other.bin();
    byte[] c = new byte[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return new Bytes(c);
  }


  public int length() {
    return bin().length;
  }
}
