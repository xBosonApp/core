////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-23 下午1:14
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/impl/LogServletOutputStream.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.emu;

import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.StringBufferOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;


/**
 * 控制台必须是 utf8 编码, 否则乱码
 */
public class LogServletOutputStream extends ServletOutputStream {

  private StringBufferOutputStream out;
  private Log log;


  public LogServletOutputStream(Log log) {
    this.log = log;
    out = new StringBufferOutputStream(1000);
  }


  @Override
  public boolean isReady() {
    return true;
  }


  @Override
  public void setWriteListener(WriteListener writeListener) {
  }


  @Override
  public void write(int i) throws IOException {
    out.write(i);
  }


  @Override
  public void flush() throws IOException {
    log.info(out.toString());
    out.clear();
  }


  @Override
  public void close() throws IOException {
    flush();
    out = null;
    log = null;
  }


  public Writer openWriter() {
    return out.openWrite();
  }
}
