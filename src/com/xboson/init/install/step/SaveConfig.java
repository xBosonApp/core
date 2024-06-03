////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-19 上午9:32
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/init/install/step/SaveConfig.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.init.install.step;

import com.xboson.been.Config;
import com.xboson.init.install.HttpData;
import com.xboson.init.install.IStep;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;


public class SaveConfig implements IStep {



  @Override
  public int order() {
    return 99;
  }


  @Override
  public boolean gotoNext(HttpData data) {
    String cjson = Tool.getAdapter(Config.class).toJson(data.cf);
    data.req.setAttribute("configstr", cjson);
    String act = data.req.getParameter("act");

    if ("reconfig".equals(act)) {
      data.reset = true;
    }
    else if ("restart".equals(act)) {
      try {
        String path = data.cf.configPath + com.xboson.init.Startup.INIT_FILE;
        File init_file = new File(path);
        FileWriter w = new FileWriter(init_file);
        w.write(new Date().toString());
        w.close();

        SysConfig.me().generateDefaultConfigFile(data.cf);
        data.msg = "系统即将重启...";
        return true;

      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    else {
      data.msg = "请选择一个操作";
    }

    return false;
  }


  @Override
  public String getPage(HttpData data) {
    return "save-config.jsp";
  }
}
