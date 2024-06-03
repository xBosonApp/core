////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-25 上午9:11
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/converter/ScriptObjectMirrorXmlConverter.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util.converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.xboson.util.Version;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.*;
import jdk.nashorn.internal.runtime.Context;

import java.lang.annotation.Native;
import java.util.Iterator;


/**
 * 帮助 ScriptObjectMirror 做 xml 转换
 */
public class ScriptObjectMirrorXmlConverter implements Converter {

  public static final ScriptObjectMirrorXmlConverter
          me = new ScriptObjectMirrorXmlConverter();


  @Override
  public void marshal(Object o, HierarchicalStreamWriter writer,
                      MarshallingContext context) {
    ScriptObjectMirror jsobj = (ScriptObjectMirror) o;
    if (jsobj.isFunction())
      return;

    String cname = jsobj.getClassName();
    if (cname.indexOf("Error") >= 0) {
      Object stack = jsobj.getMember("stack");
      if (stack != null) {
        writer.startNode("error");
        writer.addAttribute("type", cname);
        writer.setValue(String.valueOf(stack));
        writer.endNode();
        return;
      }
    }

    if (jsobj.isArray()) {
      mArray(jsobj, writer, context);
    } else {
      mObject(jsobj, writer, context);
    }
  }


  private void mArray(ScriptObjectMirror jsobj, HierarchicalStreamWriter writer,
                      MarshallingContext context) {
    int len = jsobj.size();
    writer.addAttribute("type", "array");
    writer.addAttribute("length", Integer.toString(len));

    for (int i = 0; i < len; ++i) {
      Object val = jsobj.getSlot(i);
      writer.startNode("element");
      writer.addAttribute("index", Integer.toString(i));

      if (val != null) {
        mType(val, writer);
        context.convertAnother(val);
      }
      writer.endNode();
    }
  }


  private void mObject(ScriptObjectMirror jsobj, HierarchicalStreamWriter writer,
                       MarshallingContext context) {
    writer.addAttribute("type", "object");

    Iterator<String> keys = jsobj.keySet().iterator();

    while (keys.hasNext()) {
      String name = keys.next();
      Object val = jsobj.get(name);
      writer.startNode(name);

      if (val != null) {
        mType(val, writer);
        context.convertAnother(val);
      }
      writer.endNode();
    }
  }


  private void mType(Object val, HierarchicalStreamWriter writer) {
    if (val instanceof Number) {
      writer.addAttribute("type", "number");
    } else if (val instanceof Boolean) {
      writer.addAttribute("type", "bool");
    } else if (val instanceof CharSequence) {
      writer.addAttribute("type", "string");
    }
  }


  @Override
  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context) {
    final String type = reader.getAttribute("type");
    if (type == null) return null;

    Object ret;
    switch (type) {
      case "array":
        ret = createArray(reader, context);
        break;

      case "object":
        ret = createObject(reader, context);
        break;

      case "bool":
        ret = Boolean.parseBoolean(reader.getValue());
        break;

      case "number":
        ret = NativeNumber.constructor(
                true, this, reader.getValue());
        break;

      case "string":
      default:
        ret = NativeString.constructor(
                true, this,reader.getValue());
    }

    return ScriptObjectMirror.wrap(ret, Context.getGlobal());
  }


  private Object createArray(HierarchicalStreamReader reader,
                             UnmarshallingContext context) {
    int len = Integer.parseInt( reader.getAttribute("length") );

    NativeArray arr = NativeArray.construct(true, null, len);
    ScriptObjectMirror som = (ScriptObjectMirror)
            ScriptObjectMirror.wrap(arr, Context.getGlobal());

    while (reader.hasMoreChildren()) {
      reader.moveDown();
      int index = Integer.parseInt( reader.getAttribute("index") );
      Object ret = unmarshal(reader, context);
      som.setSlot(index, ret);
      reader.moveUp();
    }
    return som;
  }


  private Object createObject(HierarchicalStreamReader reader,
                              UnmarshallingContext context) {
    Object obj = NativeObject.construct(true, null, null);
    ScriptObjectMirror som = (ScriptObjectMirror)
            ScriptObjectMirror.wrap(obj, Context.getGlobal());

    while (reader.hasMoreChildren()) {
      reader.moveDown();
      String name = reader.getNodeName();
      Object ret = unmarshal(reader, context);
      som.setMember(name, ret);
      reader.moveUp();
    }
    return som;
  }


  @Override
  public boolean canConvert(Class aClass) {
    return ScriptObjectMirror.class.isAssignableFrom(aClass);
  }


  /**
   * 如果使用了 ScriptObjectMirrorJsonConverter.Warp 做包装器
   */
  static public class WarpConverter implements Converter {

    @Override
    public void marshal(Object o, HierarchicalStreamWriter hierarchicalStreamWriter,
                        MarshallingContext marshallingContext) {
      ScriptObjectMirrorJsonConverter.Warp
              warp = (ScriptObjectMirrorJsonConverter.Warp) o;

      hierarchicalStreamWriter.addAttribute("xboson", Version.xBoson);
      me.marshal(warp.jsobj, hierarchicalStreamWriter, marshallingContext);
    }


    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
      return me.unmarshal(reader, context);
    }


    @Override
    public boolean canConvert(Class aClass) {
      return ScriptObjectMirrorJsonConverter.Warp.class.isAssignableFrom(aClass);
    }
  }
}
