////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-13 下午6:26
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/IBytesWriter.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;
import com.xboson.util.c0nst.IConstant;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Signature;
import java.security.SignatureException;


/**
 * 一个简单的字节写出器, (写出后并不考虑如何还原)
 */
public interface IBytesWriter extends AutoCloseable {

  /**
   * 写出字节数组
   */
  void write(byte[] b);


  /**
   * 默认调用 write(byte[]) 写出字符串的字节形式
   * @see IConstant#CHARSET 字符串的字节编码方式
   */
  default void write(String x) {
    write(x.getBytes(IConstant.CHARSET));
  }


  /**
   * 写出长整型数据
   */
  default void write(long l) {
    write(new byte[] {
            (byte) (l & 0xFF),
            (byte) ((l>>8 ) & 0xFF),
            (byte) ((l>>16) & 0xFF),
            (byte) ((l>>24) & 0xFF),
            (byte) ((l>>32) & 0xFF),
            (byte) ((l>>40) & 0xFF),
            (byte) ((l>>48) & 0xFF),
            (byte) ((l>>56) & 0xFF),
    });
  }


  /**
   * 默认的关闭方法什么都不做
   */
  default void close() {}


  /**
   * 包装签名器为一个字节写出器, 签名器抛出的异常包装为 XBosonException
   * @see XBosonException
   */
  static IBytesWriter wrap(Signature si) {
    return (byte[] b) -> {
      try {
        si.update(b);
      } catch (SignatureException e) {
        throw new XBosonException(e);
      }
    };
  }


  /**
   * 包装输出流为一个字节写出器, 输出流抛出的异常包装为 XBosonException.IOError
   * @see XBosonException.IOError
   */
  static IBytesWriter wrap(OutputStream out) {
    return new IBytesWriter() {
      public void write(byte[] b) {
        try {
          out.write(b);
        } catch (IOException e) {
          throw new XBosonException.IOError(e);
        }
      }

      public void close() {
        try {
          out.close();
        } catch (IOException e) {
          throw new XBosonException.IOError(e);
        }
      }
    };
  }

}
