////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-19 上午7:32
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/j2ee/resp/JsonPaddingResp.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.resp;

import com.squareup.moshi.JsonAdapter;
import com.xboson.been.ResponseRoot;
import com.xboson.j2ee.container.IXResponse;
import com.xboson.util.OutputStreamSinkWarp;
import com.xboson.util.Tool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


/**
 * 对于 jsonp 格式, 参数中必须有 "cb" 这个参数, 该参数作为 padding 函数.
 * 如果为提供该参数, 则使用 "cb" 这个函数.
 */
public class JsonPaddingResp implements IXResponse {

  private static final String MIME_JS = "application/javascript; charset=utf-8";
  private static final String FN_NAME = "cb";

  private final JsonAdapter<Map> jadapter;


  public JsonPaddingResp() {
    jadapter = Tool.getAdapter(Map.class);
  }


  @Override
  public void response(HttpServletRequest request, HttpServletResponse response,
                       Map<String, Object> ret_root) throws IOException {

    OutputStream out = response.getOutputStream();
    OutputStreamSinkWarp outwarp = new OutputStreamSinkWarp(out);

    String fnname = request.getParameter(FN_NAME);
    if (fnname == null) {
      fnname = FN_NAME;
    }

    response.setHeader("content-type", MIME_JS);
    outwarp.writeUtf8(fnname);
    outwarp.writeUtf8("(");
    jadapter.toJson(outwarp, ret_root);
    outwarp.writeUtf8(");");
  }

}
