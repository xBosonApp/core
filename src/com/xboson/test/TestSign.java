////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-2-5 下午6:13
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestSign.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.been.License;
import com.xboson.util.Hash;
import com.xboson.util.Hex;
import com.xboson.util.Password;
import com.xboson.util.Version;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;


public class TestSign extends Test {


  @Override
  public void test() throws Throwable {
    init();
    write();
    admin();
  }


  private void init() throws UnsupportedEncodingException {
    // 使用后注释, 防止出现在代码中
    String s[] = new String[] {
//            "Bad License, Copy protection",
//            "Bad License, Wrong application",
//            "Bad License, Signature fail",
//            "Bad License, Has not yet started",
//            "Bad License, Over the use of time",
//            "License",
//            "Update",
//            "bad public key",
//            "No license to run the function, ",
//            "未授权的产品, 由[上海竹呗信息技术有限公司]提供技术支持",
    };

    msg("public static final String s[] = new String[] {");
    for (int i=0; i<s.length; ++i) {
      msg("  /*", i, '>', s[i], "*/");
      s[i] = Base64.getEncoder().encodeToString(s[i].getBytes("UTF8"));
      msg("        \""+ s[i] +"\",");
    }
    msg("};");
  }


  /**
   * 计算超级管理员密钥, 使用后全部注释, 防止出现在代码中
   * @throws Exception
   */
  private void admin() throws Exception {
//    sub("Root password");
//    String userid = "admin-pl";
//    String ps = "vfr4#3edc";
//    Hash h = new Hash();
//    h.update(userid);
//    h.update(Hex.lowerHex(Password.md5(ps)));
//    h.update("Fm=κqm1qm2/γ2r <Magnetic coulomb law>");
//    msg("ROOT:", userid, h.digestStr());
  }


  private void write() throws Exception {
    sub("Generate License Request");
    License req = new License();
    req.appName    = Version.Name;
    req.company    = "内测";
    req.dns        = "www.xboson.x.y.z";
    req.email      = "yanmingsohu@live.com";
    req.beginTime  = 1514736000000L;
    req.endTime    = 1546099200000L;
    req.zz();

    File reqFile = req.writeRequest();
    byte[] b = Files.readAllBytes(Paths.get(reqFile.toURI()));
    String reqStr = new String(b);
    msg(reqStr);
  }


  public static void main(String[] a) {
    new TestSign();
  }

}
