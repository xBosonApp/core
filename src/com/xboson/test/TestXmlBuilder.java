////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-6-5 下午8:06
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestXmlBuilder.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.app.lib.XmlImpl;
import com.xboson.script.lib.JsOutputStream;
import com.xboson.util.StringBufferOutputStream;

import java.io.IOException;


public class TestXmlBuilder extends Test {

  @Override
  public void test() throws Throwable {
    basic();
  }


  public void basic() throws Exception {
    sub("basic");

    XmlImpl xml = new XmlImpl();
    StringBufferOutputStream buf = new StringBufferOutputStream();
    JsOutputStream jo = new JsOutputStream(buf);

    XmlImpl.XmlRoot root = xml.build(jo);
    XmlImpl.XmlTagWriter a = root.tag("a");
    XmlImpl.XmlTagWriter b = a.tag("b");
    XmlImpl.XmlTagWriter c = b.tag("c");

    b.tag("d").attr("type", "string");

    XmlImpl.XmlTagWriter txt = a.tag("txt");
    JsOutputStream text = txt.textWriter();
    text.write("<br/>");

    root.end();


    new Throws(IllegalStateException.class) {
      public void run() throws Throwable {
        b.text("bad");
      }
    };

    new Throws(IllegalStateException.class) {
      public void run() throws Throwable {
        a.attr("bad", "bad");
      }
    };

    String xmlstr = buf.toString();
    eq(xmlstr, "<a><b><c/><d type=\"string\"/></b><txt>&lt;br/&gt;</txt></a>", "xml");
    msg("ok", xmlstr);
  }


  public static void main(String[] av) throws IOException {
    new TestXmlBuilder();
  }

}
