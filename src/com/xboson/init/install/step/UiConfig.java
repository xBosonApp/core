////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-20 下午5:12
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/init/install/step/UiConfig.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.init.install.step;

import com.xboson.init.install.HttpData;
import com.xboson.init.install.IStep;
import com.xboson.fs.redis.IRedisFileSystemProvider;
import com.xboson.fs.redis.LocalFileMapping;
import com.xboson.util.Tool;

import java.io.File;


public class UiConfig implements IStep {

  @Override
  public int order() {
    return 6;
  }


  @Override
  public boolean gotoNext(HttpData data) throws Exception {
    data.cf.uiListDir = Boolean.parseBoolean(
            data.req.getParameter("uiListDir"));

    data.cf.uiWelcome = data.req.getParameter("uiWelcome");
    if (Tool.isNulStr(data.cf.uiWelcome)) {
      data.msg = "必须设置根路径跳转";
      return false;
    }

    String clname = data.req.getParameter("uiProviderClass");
    try {
      if (!(clname.equals("local") || clname.equals("online"))) {
        data.msg = clname + " 不是 UI 文件映射接口";
        return false;
      }
    } catch (Exception e) {
      data.msg = "服务类型错误:" + e.getMessage();
      return false;
    }

    if (clname.equals("local")) {
      data.cf.uiUrl = data.req.getParameter("uiUrl");
      if (Tool.isNulStr(data.cf.uiUrl )) {
        data.msg = "请设置 '静态文件根目录'";
        return false;
      }
      return data.isDirectory(data.cf.uiUrl);

    } else {
      data.cf.uiUrl = "< Not use >";
    }
    return true;
  }


  @Override
  public String getPage(HttpData data) {
    return "ui.jsp";
  }
}
