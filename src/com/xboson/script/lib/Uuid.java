////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-12 下午5:34
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/script/lib/Uuid.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script.lib;

import com.xboson.script.JSObject;
import com.xboson.util.Hex;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;


public class Uuid extends JSObject {

  public static final long HALF = (long)(Long.MAX_VALUE / 2);
  public static final long v1 = 1l << 12;
  public static final long versionMask = ~(0xF << 12);

  private static long id = (long)(Math.random() * HALF);


  /**
   * 返回标准 UUID 字符串, 基于时间生成可能重复.
   */
  public String v1() {
    return v1obj().toString();
  }


  /**
   * 返回标准 UUID 字符串, 可以生成健壮的随机 UUID.
   */
  public String v4() {
    return v4obj().toString();
  }


  /**
   * 返回标准 UUID 对象, 基于时间生成可能重复.
   */
  public UUID v1obj() {
    long m = System.currentTimeMillis();
    long l = ++id;
    if (l < HALF) l = -l;
    if (m < HALF) m = -m;
    m = m & versionMask | v1;
    return new UUID(m, l);
  }


  /**
   * 返回标准 UUID 对象, 可以生成健壮的随机 UUID.
   */
  public UUID v4obj() {
    return UUID.randomUUID();
  }


  /**
   * 生成原先 DS 平台的 UUID 字符串
   */
  public String ds() {
    return ds(v4obj());
  }


  /**
   * 生成原先 DS 平台的 UUID 字符串
   */
  public String ds(UUID id) {
    byte[] bytes = getBytes(id);
    return Hex.lowerHex(bytes);
  }


  /**
   * 解析原 DS 平台字符串到 UUID 对象
   */
  public UUID parseDS(String ds) {
    return UUID.fromString(
            ds.substring( 0,  8) + "-" +
            ds.substring( 8, 12) + "-" +
            ds.substring(12, 16) + "-" +
            ds.substring(16, 20) + "-" +
            ds.substring(20, 32)
    );
  }


  /**
   * 转换为 16 字节
   */
  public byte[] getBytes(UUID id) {
    ByteBuffer buf = ByteBuffer.allocate(16);
    buf.putLong(0, id.getMostSignificantBits());
    buf.putLong(8, id.getLeastSignificantBits());
    return buf.array();
  }


  /**
   * 生成压缩的 UUID 字符串
   */
  public String zip(UUID id) {
    byte[] b = getBytes(id);
    return Hex.encode64(b);
  }


  /**
   * 解压缩使用 zip() 压缩的字符串, 还原为 UUID 对象
   */
  public UUID unzip(String z) {
    byte[] b = Hex.decode64(z);
    ByteBuffer buf = ByteBuffer.wrap(b);
    return new UUID(buf.getLong(0), buf.getLong(8));
  }


  /**
   * 生成压缩的 UUID 字符串, 长度 24 字符
   */
  public String zip() {
    return zip(v4obj());
  }
}
