////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 下午7:56
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestXML.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.thoughtworks.xstream.XStream;
import com.xboson.been.ResponseRoot;
import com.xboson.j2ee.resp.XmlResponse;
import com.xboson.util.Tool;
import com.xboson.util.converter.XmlDataMapConverter;

import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


public class TestXML extends Test {


  public void test() throws Throwable {
    test_thread();
    test_resp();
  }


  public void test_resp() throws Throwable {
    XmlResponse x = new XmlResponse();

    Map<String, Object> ret_root = new ResponseRoot();
    ret_root.put("code", 0);
    ret_root.put("message", "ok");

    Map<Object, Object> a = new HashMap<>();
    a.put("xxx", 1);
    a.put("yyu", 2);
    ret_root.put("a", a);

    Writer out = new StringWriter();
    out.write('\n');
    out.write(x.XML_HEAD);

    XStream xs = Tool.createXmlStream();
    xs.toXML(ret_root, out);
    out.write('\n');

    String xmlstr = out.toString();
    Object o = xs.fromXML(xmlstr);
    msg("From Xml:", o);

    msg(xmlstr);
  }



  /**
   * XStream 是线程安全的
   */
  public void test_thread() {
    sub("Mutil XStream Thread Safe");
    final XStream xs = Tool.createXmlStream();
    TestData td = new TestData();
    td.change();
    msg("---------- XML:\n", xs.toXML(td));

    beginTime();
    Thread []ts = new Thread[1000];
    for (int i=0; i<ts.length; ++i) {
      ts[i] = new Thread(new Runnable() {
        public void run() {
          TestData td = new TestData();
          td.change();
          xs.toXML(td);
        }
      });
      ts[i].start();
    }

    for (int i=0; i<ts.length; ++i) {
      Tool.waitOver(ts[i]);
    }
    endTime(ts.length, "Thread over");
  }


  public static void main(String[] a) {
    new TestXML();
  }

}
