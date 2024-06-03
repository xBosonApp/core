////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 上午8:45
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestAuth.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.auth.*;
import com.xboson.auth.impl.LicenseAuthorizationRating;
import com.xboson.db.ConnectConfig;
import com.xboson.db.sql.SqlReader;
import com.xboson.db.SqlResult;
import com.xboson.util.Password;

import java.sql.ResultSet;


public class TestAuth extends Test {

  private ConnectConfig cc;

  public void test() throws Throwable {
    cc = TestDS.connect_config();
    password();
    sql_reader();
    licenseAuth();
  }


  public void licenseAuth() {
    sub("LicenseAuthorizationRating.class");

    PermissionSystem.applyWithApp(
            LicenseAuthorizationRating.class,
            ()-> "api.ide.code.modify.functions()");

    sub("Not pass");

    new Throws(LicenseAuthorizationRating.NoLicense.class) {
      public void run() {
        PermissionSystem.applyWithApp(
                LicenseAuthorizationRating.class,
                ()-> "app.nopass()");
      }
    };
  }


  public void sql_reader() throws Throwable {
    String code = SqlReader.read("login.sql");
    ok(code != null, "read 'login.sql'");

    code = SqlReader.read("login");
    ok(code != null, "read 'login'");

    String[] parmbind = new String[] {"attewfdsafdsaf", "", ""};
    try (SqlResult sr = SqlReader.query("login.sql", cc, parmbind)) {
      ResultSet rs = sr.getResult();
      TestDBMS.show(rs);
    }
  }


  public void password() throws Throwable {
    sub("Encode password");
    // userid
    String userid = "attewfdsafdsaf";
    // password
    String pstrue = "enw8dcnvczkhfd";
    // 加密的 password
    String psword = "D8D1BEB36B1F49238F0FFE376E11E1739570B99F095FEDCA7CC9864C6B358602";
    // 密码修改时间（password_dt）
    String date = "2017-11-18 10:48:32.0";

    String ps = Password.v1(userid, Password.md5lowstr(pstrue), date);
    eq(psword, ps, "encode password");
    msg(ps);
    msg(Password.md5lowstr("111111"));

    beginTime();
    for (int i=0; i<100000; ++i) {
      Password.v1(userid, pstrue, date);
    }
    endTime("100000 count encode password");
  }


  public static void main(String[] a) {
    new TestAuth();
  }


  static public class Who implements IAWho {
    private String id;
    Who(String id) { this.id = id; }
    public String identification() {
      return "/" + id;
    }
    public boolean isRoot() {
      return false;
    }
  }


  static public class Res implements IAResource {
    private String id;
    Res(String id) { this.id = id; }
    @Override
    public String description() {
      return id;
    }
  }


  static public class Where implements IAWhere {
    public boolean apply(IAWho who, IAResource res) {
      return res.description().equals( who.identification() );
    }
  }

}
