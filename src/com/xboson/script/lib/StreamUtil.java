////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-6-5 下午2:51
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/script/lib/StreamUtil.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script.lib;

import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.c0nst.IConstant;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class StreamUtil {

  private Base64.Encoder bec;
  private Base64.Decoder bdc;


  public StreamUtil() {
    bec = Base64.getEncoder();
    bdc = Base64.getDecoder();
  }

  public JsOutputStream openGzipOutputStream(JsOutputStream out) throws IOException {
    GZIPOutputStream gzip = new GZIPOutputStream(out, true);
    return new JsOutputStream(gzip) {
      public void finish() throws IOException {
        gzip.finish();
      }
    };
  }


  public JsInputStream openGzipInputStream(JsInputStream in) throws IOException {
    GZIPInputStream gzip = new GZIPInputStream(in);
    return new JsInputStream(gzip);
  }


  public JsOutputStream openBase64OutputStream(JsOutputStream out) {
    return new JsOutputStream(bec.wrap(out));
  }


  public JsInputStream openBase64InputStream(JsInputStream in) {
    return new JsInputStream(bdc.wrap(in));
  }


  public JsOutputStream openStringBufferOutputStream() {
    StringBufferOutputStream buf = new StringBufferOutputStream();
    return new JsOutputStream(buf);
  }


  @SuppressWarnings("deprecation")
  public JsInputStream openStringInputStream(String s) {
    StringBufferInputStream r = new StringBufferInputStream(s);
    return new JsInputStream(r);
  }


  public LineNumberReader openLineReader(JsInputStream in) {
    return new LineNumberReader(new InputStreamReader(in, IConstant.CHARSET));
  }


  public JsOutputStream openXMLOutputStream(JsOutputStream out) throws XMLStreamException {
    return new JsOutputStream(new XmlContentWriter(out));
  }


  /**
   * 在写出 xml 文本时, 对特殊字符做转义.
   */
  public static class XmlContentWriter extends OutputStream {
    private JsOutputStream ori;

    public XmlContentWriter(JsOutputStream ori) {
      this.ori = ori;
    }

    @Override
    public void write(int i) throws IOException {
      if (i >= 0x80) {
        ori.write(i);
      } else {
        switch (i) {
          case '<' : ori.write("&lt;"); break;
          case '>' : ori.write("&gt;"); break;
          case '&' : ori.write("&amp;"); break;
          case '"' : ori.write("&quot;"); break;
          case '\'': ori.write("&apos;"); break;
          default  : ori.write(i);
        }
      }
    }
  }
}
