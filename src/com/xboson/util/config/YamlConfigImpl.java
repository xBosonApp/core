////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-9 下午6:05
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/config/YamlConfigImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util.config;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.xboson.been.Config;
import com.xboson.util.c0nst.IConstant;
import com.xboson.util.StringBufferOutputStream;

import java.io.IOException;


public class YamlConfigImpl extends AbsConfigSerialization {


  @Override
  public String convert(Config c) throws IOException {
    StringBufferOutputStream buf = new StringBufferOutputStream();
    YamlWriter w = new YamlWriter(buf.openWrite(), basicConfig());
    w.write(c);
    w.close();
    return addComments(buf.toString());
  }


  @Override
  public Config convert(String yaml) throws IOException {
    YamlReader r = new YamlReader(yaml);
    return r.read(Config.class);
  }


  public static YamlConfig basicConfig() {
    YamlConfig yc = new YamlConfig();
    yc.writeConfig.setKeepBeanPropertyOrder(true);
    yc.writeConfig.setWriteDefaultValues(true);
    yc.writeConfig.setEscapeUnicode(false);
    return yc;
  }


  /** YAML 支持注释不需要去掉就可以解析 */
  @Override
  public String reomveComments(String yaml) {
    return yaml;
  }


  @Override
  public String beginComment() {
    return "# ";
  }


  @Override
  public String endComment() {
    return IConstant.NULL_STR;
  }


  @Override
  public String find(String key) {
    return key + ':';
  }


  @Override
  public String fileName() {
    return "config.yaml";
  }
}
