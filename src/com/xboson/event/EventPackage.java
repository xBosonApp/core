////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-12 上午10:19
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/event/EventPackage.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.event;


import com.squareup.moshi.JsonAdapter;
import com.xboson.util.Tool;

import java.io.IOException;
import java.util.UUID;

public class EventPackage {

  private static final JsonAdapter<EventPackage>
          ad = Tool.getAdapter(EventPackage.class);

  public Object data;
  public int    type;
  public String info;
  public long   from;
  public String className;


  public EventPackage(Object data, int type, String info, long from) {
    this.type = type;
    this.info = info;
    this.from = from;
    // 数据被再次编码为 string, 并保留类型
    if (data != null) {
      this.data = Tool.getAdapter((Class) data.getClass()).toJson(data);
      this.className = data.getClass().getName();
    }
  }


  public String tojson() {
    return ad.toJson(this);
  }


  /**
   * 解析数据并还原为原始类型
   */
  public void parseData() {
    //
    // 为安全而过滤了包名, 但是这并不起作用, 反而增加了麻烦
    // && className.indexOf("com.xboson.") == 0
    //
    if (className != null && data != null) {
      try {
        Class data_class = Class.forName(className);
        data = Tool.getAdapter(data_class).fromJson(data.toString());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }


  public static EventPackage fromjson(String str) throws IOException {
    return ad.fromJson(str);
  }
}
