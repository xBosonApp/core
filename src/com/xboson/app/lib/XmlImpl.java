////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-6-5 下午6:13
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/XmlImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.script.IVisitByScript;
import com.xboson.script.lib.JsInputStream;
import com.xboson.script.lib.JsOutputStream;
import com.xboson.script.lib.StreamUtil;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;
import com.xboson.util.c0nst.IXML;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class XmlImpl implements IXML, IVisitByScript {

  public static final String BEG_TN      = "<";
  public static final String END_TN      = ">";
  public static final String SELF_END_TN = "/>";
  public static final String END_END_TN  = "</";
  public static final String ATTR_EQ     = "=";
  public static final String ATTR_QM     = "\"";
  public static final String SP          = " ";
  public static final String ENTER       = "\n";

  public static final int ST_BEGIN       = 0;
  public static final int ST_BEG_ATTR    = 2;
  public static final int ST_BET_BODY    = 5;
  public static final int ST_SUB_TAG     = 10;
  public static final int ST_END_TAG     = 99;

  public static boolean DEBUG = false;


  public XmlRoot build(JsOutputStream out, boolean pretty) throws IOException {
    return new XmlRoot(out, pretty);
  }


  public XmlRoot build(JsOutputStream out) throws IOException {
    return build(out, DEBUG);
  }


  public TagStruct parse(JsInputStream in) throws Exception {
    InputSource src = new InputSource(in);
    src.setEncoding(IConstant.CHARSET_NAME);
    TagConvert tc = new TagConvert();
    XMLReader xmlReader = XMLReaderFactory.createXMLReader();
    xmlReader.setContentHandler(tc);
    xmlReader.parse(src);

    if (tc.root != null && tc.root.childrenNode != null) {
      if (tc.root.childrenNode.size() == 1) {
        return tc.root.childrenNode.get(0);
      }
    }
    return tc.root;
  }


  public void stringify(TagStruct root, JsOutputStream out) throws Exception {
    TagConvert tc = new TagConvert();
    XmlRoot xroot = tc.initStringify(root, out);
    xroot.end();
  }


  public String stringify(TagStruct root) throws Exception {
    StringBufferOutputStream buf = new StringBufferOutputStream();
    JsOutputStream out = new JsOutputStream(buf);
    stringify(root, out);
    return buf.toString();
  }


  public class XmlRoot implements IVisitByScript {

    private JsOutputStream out;
    private XmlTagWriter last;
    private boolean pretty;


    private XmlRoot(JsOutputStream out, boolean pretty) throws IOException {
      this.out    = out;
      this.pretty = pretty;
    }


    public void writeHead() throws IOException {
      if (last != null)
        throw new IllegalStateException();

      out.write(XML_HEAD);
    }


    public XmlTagWriter tag(String name) throws IOException {
      if (last != null) last.end();
      XmlTagWriter x = new XmlTagWriter(name, out, pretty);
      x.begin();
      last = x;
      return x;
    }


    public void end() throws IOException {
      if (last != null) {
        last.end();
        last = null;
      }
      if (out != null) {
        out.flush();
        out = null;
      }
    }
  }


  /**
   * 对象转换为 xml 字符串时使用该类
   */
  public class XmlTagWriter implements IVisitByScript {

    private JsOutputStream out;

    /** 记录最后一个子节点 */
    private XmlTagWriter lastSub;
    /** 文本输出器, 可能为 null */
    private JsOutputStream textWriter;
    private String name;
    private int state;
    private boolean pretty;
    private int deep;


    private XmlTagWriter(String name, JsOutputStream out, boolean pretty)
            throws IOException {
      this.name   = name;
      this.state  = ST_BEGIN;
      this.out    = out;
      this.pretty = pretty;
    }


    private void begin() throws IOException {
      if (pretty) indentation();
      out.write(BEG_TN);
      out.write(name);
    }


    public XmlTagWriter attr(String name, Object val) throws IOException {
      return attr(name, String.valueOf(val));
    }


    public XmlTagWriter attr(String name, String value) throws IOException {
      if (state > ST_BEG_ATTR)
        throw new IllegalStateException();

      this.state = ST_BEG_ATTR;
      out.write(SP);
      out.write(name);
      out.write(ATTR_EQ);
      out.write(ATTR_QM);
      out.write(value);
      out.write(ATTR_QM);
      return this;
    }


    private void indentation() throws IOException {
      out.write(ENTER);
      for (int i=0; i<deep; ++i) {
        out.write(SP);
      }
    }


    public XmlTagWriter tag(String name) throws IOException {
      if (state > ST_SUB_TAG) {
        throw new IllegalStateException();
      }
      if (state < ST_BET_BODY) {
        out.write(END_TN);
      }
      state = ST_SUB_TAG;

      if (lastSub != null) lastSub.end();
      XmlTagWriter x = new XmlTagWriter(name, out, pretty);
      x.deep = deep + 2;
      x.begin();
      lastSub = x;
      return x;
    }


    public XmlTagWriter text(Object body) throws IOException {
      return text(String.valueOf(body));
    }


    public XmlTagWriter text(String body) throws IOException {
      beginBodyWithoutEnd();

      OutputStreamWriter w = new OutputStreamWriter(out, IConstant.CHARSET);
      int len = body.length();

      for (int i=0; i<len; ++i) {
        char c = body.charAt(i);
        switch (c) {
          case '<' : w.write("&lt;"); break;
          case '>' : w.write("&gt;"); break;
          case '&' : w.write("&amp;"); break;
          case '\'': w.write("&apos;"); break;
          case '"' : w.write("&quot;"); break;
          default  : w.append(c); break;
        }
      }
      // dont close
      w.flush();
      return this;
    }


    public XmlTagWriter xml(Object body) throws IOException {
      return xml(String.valueOf(body));
    }


    public XmlTagWriter xml(String body) throws IOException {
      beginBodyWithoutEnd();
      out.write(body);
      return this;
    }


    public JsOutputStream textWriter() throws IOException {
      beginBodyWithoutEnd();
      if (textWriter != null) {
        textWriter.flush();
      }
      textWriter = new JsOutputStream(new StreamUtil.XmlContentWriter(out));
      return textWriter;
    }


    private void beginBodyWithoutEnd() throws IOException {
      if (state > ST_BET_BODY) {
        throw new IllegalStateException();
      } else if (state < ST_BET_BODY) {
        out.write(END_TN);
      }
      this.state = ST_BET_BODY;
    }


    public void end() throws IOException {
      if (state == ST_END_TAG) {
        return;
      }
      else if (state <= ST_BEG_ATTR) {
        out.write(SELF_END_TN);
        return;
      }

      if (lastSub != null) {
        lastSub.end();
        lastSub = null;
      }
      if (textWriter != null) {
        textWriter.flush();
      }

      if (pretty && state >= ST_SUB_TAG) indentation();
      this.state = ST_END_TAG;

      out.write(END_END_TN);
      out.write(name);
      out.write(END_TN);
    }
  }


  /**
   * 将 xml 字符串换换为 对象时, 使用该类
   */
  public static class TagStruct implements IVisitByScript {
    public String name;
    public String qName;
    public String uri;
    public transient TagStruct  parent;
    public Map<String, String>  attributes;
    public List<TagStruct>      childrenNode;
    public StringBuilder        text;

    TagStruct(TagStruct parent) {
      this.attributes   = new HashMap<>();
      this.childrenNode = new ArrayList<>();
      this.parent       = parent;
      this.text         = new StringBuilder();
    }
  }


  private class TagConvert extends DefaultHandler {

    private TagStruct root;
    private TagStruct current;
    private Map<String, String> nsMap;
    private int id;


    void write(TagStruct s, XmlTagWriter t) throws IOException {
      for (Map.Entry<String, String> attr : s.attributes.entrySet()) {
        t.attr(attr.getKey(), attr.getValue());
      }
      if (s.text.length() > 0) {
        t.text(s.text);
      }
      for (TagStruct ch : s.childrenNode) {
        XmlTagWriter chw = t.tag(getTagName(ch));
        write(ch, chw);
      }
    }


    XmlRoot initStringify(TagStruct root, JsOutputStream out) throws IOException {
      nsMap = new HashMap<>();
      XmlRoot xroot = new XmlRoot(out, DEBUG);
      xroot.writeHead();
      write(root, xroot.tag(getTagName(root)) );
      return xroot;
    }


    String getTagName(TagStruct t) {
      if (! Tool.isNulStr(t.uri)) {
        String prefix = nsMap.get(t.uri);
        if (prefix == null) {
          prefix = "XnS"+ Integer.toString(id++, 16);
          nsMap.put(t.uri, prefix);
          t.attributes.put(NS_PF + prefix, t.uri);
        }
        return prefix +':'+ t.name;
      }
      return t.name;
    }


    @Override
    public void startDocument() throws SAXException {
      root = new TagStruct(null);
      current = root;
    }


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      current.text.append(ch, start, length);
    }


    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attr) throws SAXException
    {
      TagStruct t   = new TagStruct(current);
      t.name  = localName;
      t.qName = qName;
      t.uri   = uri;

      for (int i = attr.getLength()-1; i>=0; --i) {
        t.attributes.put(attr.getLocalName(i), attr.getValue(i));
      }
      current.childrenNode.add(t);
      current = t;
    }


    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      current = current.parent;
    }
  }
}
