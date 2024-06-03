////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-5-21 下午12:49
// 原始文件路径: E:/xboson/xBoson/src/com/xboson/app/lib/Multipart.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.been.XBosonException;
import com.xboson.script.lib.Buffer;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.c0nst.IConstant;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.ParameterParser;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class Multipart extends RuntimeUnitImpl {

  public static final int BUFFER_SIZE = 4 * 1024;

  private HttpServletRequest req;
  private byte[] boundary;
  private int bufferSize;
  private ScriptObjectMirror ret;


  public Multipart(HttpServletRequest req) {
    super(null);
    this.req = req;
    this.bufferSize = BUFFER_SIZE;
  }


  public ScriptObjectMirror parse() throws IOException {
    if (req.getContentLength() == 0) return null;
    parseBoundary();
    ret = createJSList();
    parseBody();
    return ret;
  }


  public void checkLimit(int limit) {
    final int clen = req.getContentLength();
    if (clen > limit) {
      throw new XBosonException(
              "Http Body too bigher, max: "+ limit +" bytes.");
    }
  }


  private void parseBody() throws IOException {
    MultipartStream ms = new MultipartStream(
            req.getInputStream(), boundary, bufferSize, null);

    boolean nextPart = ms.skipPreamble();
    int count = ret.size();

    while(nextPart) {
      ScriptObjectMirror item = createJSObject();
      ret.setSlot(count++, item);

      String header = ms.readHeaders();
      parseHeader(item, header);

      StringBufferOutputStream output = new StringBufferOutputStream();
      ms.readBodyData(output);
      Buffer.JsBuffer buf = new Buffer().from(output.toBytes());
      item.setMember("content", buf);

      nextPart = ms.readBoundary();
    }
  }


  private void parseHeader(ScriptObjectMirror item, String headers) {
    ScriptObjectMirror header = createJSObject();
    item.setMember("header", header);
    ParameterParser pp = new ParameterParser();
    int end = 0, st = 0;

    for (;;st = end+2) {
      end = headers.indexOf('\r', st);
      if (end < 0) break;

      String line = headers.substring(st, end);
      int i = line.indexOf(':');
      if (i >= 0) {
        String hname = line.substring(0, i);
        String hvalue = line.substring(i+1);

        switch (hname.toLowerCase()) {
          case "content-disposition":
            Map<String, String> p = pp.parse(hvalue, ';');
            header.putAll(p);
            break;

          default:
            header.put(hname, hvalue);
        }
      }
    }
  }

  private void parseBoundary() {
    do {
      String type = req.getContentType();
      if (type == null) break;

      String find = "multipart/form-data";
      int i = type.indexOf(find);
      if (i < 0) break;

      i += find.length();
      find = "boundary=";
      i = type.indexOf(find, i);
      if (i < 0) break;

      String str = type.substring(i + find.length());
      boundary = str.getBytes(IConstant.CHARSET);
    } while (false);

    if (boundary == null) {
      throw new XBosonException.BadParameter(
              "Content-type", "Not multipart/form-data");
    }
  }

}
