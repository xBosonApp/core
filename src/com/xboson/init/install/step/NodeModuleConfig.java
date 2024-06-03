////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-22 下午1:41
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/init/install/step/NodeModuleConfig.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.init.install.step;

import com.xboson.init.install.HttpData;
import com.xboson.init.install.IStep;
import com.xboson.util.Tool;


public class NodeModuleConfig implements IStep {

  @Override
  public int order() {
    return 7;
  }


  @Override
  public boolean gotoNext(HttpData data) throws Exception {
    String url = data.getStr("nodeUrl");
    String type = data.getStr("nodeProviderClass");
    data.cf.nodeUrl = url;
    data.cf.nodeProviderClass = type;

    if ("local".equals(type)) {
      if (Tool.isNulStr(url)) {
        data.msg = "本地模式必须设置 '静态文件根目录'";
        return false;
      }
      return data.isDirectory(url);

    } else if ("online".equals(type)) {
      return true;
    }

    data.msg = "无效的模式: " + type;
    return false;
  }


  @Override
  public String getPage(HttpData data) {
    return "nodemodule.jsp";
  }
}
