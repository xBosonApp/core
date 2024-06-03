////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-6-5 下午2:15
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/script/lib/JsOutputStream.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script.lib;

import com.xboson.util.c0nst.IConstant;

import java.io.IOException;
import java.io.OutputStream;


/**
 * 将 java 流对象包装后暴露给 js 对象
 */
public class JsOutputStream extends OutputStream {

  private OutputStream ori;


  public JsOutputStream(OutputStream o) {
    this.ori = o;
  }


  /**
   * 返回原始输出流
   */
  protected OutputStream original() {
    return ori;
  }


  @Override
  public void write(byte[] bytes) throws IOException {
    ori.write(bytes);
  }


  @Override
  public void write(byte[] bytes, int i, int i1) throws IOException {
    ori.write(bytes, i, i1);
  }


  public void write(Bytes bs) throws IOException {
    ori.write(bs.bin());
  }


  @Override
  public void flush() throws IOException {
    ori.flush();
  }


  @Override
  public void close() throws IOException {
    ori.close();
  }


  @Override
  public void write(int i) throws IOException {
    ori.write(i);
  }


  /**
   * 有一些流不希望关闭时关闭底层流, 但是又需要完成最后的输出, 则实现该方法.
   * 默认什么都不做.
   */
  public void finish() throws IOException {
  }


  /**
   * 读取 from 中的数据写入输出流
   * @param from 数据来源
   * @param begin from 开始的字节
   * @param len 从 from 读取的字节长度
   */
  public void write(Buffer.JsBuffer from, int begin, int len) throws IOException {
    for (int i=0; i<len; ++i) {
      int b = from.readInt8(i + begin) & 0xFF;
      write(b);
    }
  }


  public void write(String str) throws IOException {
    write(str.getBytes(IConstant.CHARSET));
  }


  @Override
  public String toString() {
    return ori.toString();
  }
}
