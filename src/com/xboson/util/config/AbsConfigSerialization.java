////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-9 下午5:53
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/config/AbsConfigSerialization.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util.config;

import java.util.Arrays;
import java.util.Map;


public abstract class AbsConfigSerialization implements IConfigSerialization {

  /**
   * 在 json 中加入注释, 实现效率很低, 返回的 json 是不符合规范的.
   */
  public final String addComments(String json) {
    StringBuilder buf = new StringBuilder();
    for (Map.Entry entry : DefaultConfig.comments().entrySet()) {
      String find = find((String) entry.getKey());
      int i = 0;

      while (i < json.length()) {
        i = json.indexOf(find, i);

        if (i == 0 || (i>0 && Character.isWhitespace(json.charAt(i-1)))) {
          int a = json.lastIndexOf("\n", i);

          if (a >= 0) {
            buf.setLength(0);
            char[] sp = new char[i-a-1];
            Arrays.fill(sp, ' ');

            buf.append(json.substring(0, a))
                    .append("\n\n")
                    .append(sp)
                    .append(beginComment())
                    .append(entry.getValue())
                    .append(endComment());

            i = sp.length + buf.length() + find.length();
            buf.append(json.substring(a));
            json = buf.toString();
          } else {
            i += find.length();
          }
        } else {
          break;
        }
      }
    }
    return json;
  }


  /**
   * 删除注释, 返回标准 json 字符串
   */
  public abstract String reomveComments(String json);


  /**
   * 返回注释开始字符
   */
  public abstract String beginComment();


  /**
   * 返回注释结束字符
   */
  public abstract String endComment();


  /**
   * 查询 key 名称的配置, 对查询字符串进行格式化并返回.
   */
  public abstract String find(String key);
}
