////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-2-7 下午3:27
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/init/install/step/RegLicense.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.init.install.step;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.xboson.been.License;
import com.xboson.crypto.Crypto;
import com.xboson.init.install.HttpData;
import com.xboson.init.install.IStep;
import com.xboson.util.Tool;
import com.xboson.util.Version;

import java.io.FileWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;


public class RegLicense implements IStep {

  @Override
  public int order() {
    return 0;
  }


  @Override
  public boolean gotoNext(HttpData data) throws Exception {
    int op = data.getInt("op");
    if (op == 1) {
      if (data.getStr("skip") != null) {
        return true;
      }
      op1genreq(data);
    }
    else if (op == 2) {
      return op2uplicense(data);
    }

    return false;
  }


  private boolean op2uplicense(HttpData data) throws Exception {
    data.ajax = true;

    String yaml = data.getStr("yaml");
    YamlReader r = new YamlReader(yaml);
    License li = r.read(License.class);
    li.setPublicKeyFile(data.sc);

    if (Tool.isNulStr(li.signature)) {
      throw new Exception("授权没有签名");
    }

    if (! Crypto.me().verification(li)) {
      throw new Exception("授权许可无效");
    }

    FileWriter out = new FileWriter(data.cf.configPath + li.LIC_FILE);
    li.writeTo(out);
    data.msg = "next";
    return true;
  }


  private void op1genreq(HttpData data) throws Exception {
    License req = new License();
    req.appName    = Version.Name;
    req.company    = data.getStr("company");
    req.dns        = data.getStr("dns");
    req.email      = data.getStr("email");

    int use = data.getInt("useTime");
    if (use <= 0) {
      data.msg = "\"使用时长无效\"";
      return;
    }
    if (Tool.isNulStr(req.company)) {
      data.msg = "\"公司名称无效\"";
      return;
    }
    if (Tool.isNulStr(req.dns)) {
      data.msg = "\"域名无效\"";
      return;
    }
    if (Tool.isNulStr(req.email)) {
      data.msg = "\"邮箱无效\"";
      return;
    }

    Date now = new Date();
    Calendar then = Calendar.getInstance();
    then.setTime(now);
    then.add(Calendar.YEAR, use);

    req.beginTime = now.getTime();
    req.endTime = then.getTimeInMillis();
    req.api = new HashSet<>();
    req.zz();

    StringWriter out = new StringWriter();
    req.writeTo(out);
    req.writeLicense();
    req.writeRequest();

    AjaxData d = new AjaxData();
    d.msg = "ok";
    d.code = out.toString();
    data.msg = Tool.getAdapter(AjaxData.class).toJson(d);
  }


  public static class AjaxData {
    public String msg;
    public String code;
  }


  @Override
  public String getPage(HttpData data) {
    return "reg-license.jsp";
  }
}
