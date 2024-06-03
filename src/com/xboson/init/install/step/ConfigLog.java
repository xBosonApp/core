////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-20 下午4:05
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/init/install/step/ConfigLog.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.init.install.step;

import com.xboson.init.install.HttpData;
import com.xboson.init.install.IStep;
import com.xboson.log.Level;
import com.xboson.util.Tool;


public class ConfigLog  implements IStep {

  @Override
  public int order() {
    return 5;
  }


  @Override
  public boolean gotoNext(HttpData data) {
    data.cf.loggerWriterType =
            data.req.getParameter("log_type");
    data.cf.logLevel =
            data.req.getParameter("log_level");
    data.cf.logPath =
            data.cf.configPath + '/' + data.req.getParameter("log_path");

    try {
      if (Tool.isNulStr(data.cf.loggerWriterType)) {
        data.msg = "日志类型无效";
        return false;
      }

      if (Tool.isNulStr(data.cf.logLevel)) {
        data.msg = "日志级别无效";
        return false;
      }

      if (Tool.isNulStr(data.req.getParameter("log_path"))) {
        data.msg = "路径无效";
        return false;
      }

      Class.forName("com.xboson.log.writer." + data.cf.loggerWriterType);
      Level lv = Level.find(data.cf.logLevel);
      if (lv == Level.INHERIT) {
        throw new Error("全局禁止使用 INHERIT 级别");
      }
      data.cf.logLevel = lv.getName();

      return true;
    } catch(Exception e) {
      e.printStackTrace();
    }

    return false;
  }


  @Override
  public String getPage(HttpData data) {
    return "log.jsp";
  }
}
