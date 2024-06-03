////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-20 下午1:03
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/Digest.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.script.lib.Buffer;
import com.xboson.script.lib.Bytes;
import com.xboson.util.Hash;


/**
 * 摘要算法
 */
public class Digest extends RuntimeUnitImpl {


  public Digest() {
    super(null);
  }


  public HashWarp sha1() {
    return new HashWarp("SHA-1");
  }


  public HashWarp sha224() {
    return new HashWarp("SHA-224");
  }


  public HashWarp sha256() {
    return new HashWarp("SHA-256");
  }


  public HashWarp sha384() {
    return new HashWarp("SHA-384");
  }


  public HashWarp sha512() {
    return new HashWarp("SHA-512");
  }


  public HashWarp md5() {
    return new HashWarp("md5");
  }


  public HashWarp md2() {
    return new HashWarp("md2");
  }


  public static class HashWarp {
    private Hash h;

    public HashWarp(String algorithm) {
      h = new Hash(algorithm);
    }


    public void update(String s) {
      h.update(s);
    }


    public void update(int i) {
      h.update(i);
    }


    public void update(Bytes b) {
      h.update(b.bin());
    }


    public void update(Buffer.JsBuffer b) {
      h.update(b._buffer().array());
    }


    public Bytes digest() {
      return new Bytes(h.digest());
    }
  }
}
