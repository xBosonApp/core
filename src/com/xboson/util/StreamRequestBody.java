////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-13 下午4:58
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/StreamRequestBody.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;


/**
 * 为 okhttp3 的 Body 提供流式操作支持.
 * 操作完成后输入流将被关闭.
 */
public class StreamRequestBody extends RequestBody {

  private InputStream i;
  private MediaType mt;


  public StreamRequestBody(InputStream i, MediaType mt) {
    if (i == null)
      throw new XBosonException.NullParamException("InputStream i");

    this.i  = i;
    this.mt = mt;
  }


  public StreamRequestBody(InputStream i, String mediaType) {
    this(i, MediaType.parse(mediaType));
  }


  public StreamRequestBody(InputStream i) {
    this(i, (MediaType)null);
  }


  @Nullable
  @Override
  public MediaType contentType() {
    return mt;
  }


  @Override
  public void writeTo(BufferedSink sink) throws IOException {
    Tool.copy(i, sink.outputStream(), true);
  }
}
