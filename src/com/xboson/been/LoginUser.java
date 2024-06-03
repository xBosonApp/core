////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 上午8:23
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/auth/LoginUser.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import com.xboson.auth.IAWho;
import com.xboson.been.IBean;
import com.xboson.been.JsonHelper;
import com.xboson.db.ConnectConfig;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.service.UserService;
import com.xboson.util.Hash;
import com.xboson.util.Password;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class LoginUser extends JsonHelper implements IBean, IAWho {
  public String pid;        // uuid
  public String userid;     // 就是用户登录 name, 唯一, 也是 openid
  public String tel;
  public String email;
  public String status;
  public long   loginTime;

  transient public String password;
  transient public String password_dt;

  public List<String> roles;


  @Override
  public String identification() {
    return pid;
  }


  @Override
  public boolean isRoot() {
    return false;
  }


  /**
   * 检查输入的密码是否与当前用户密码一致
   * @param md5ps 密码明文经过 md5 后的字符串
   * @return 密码一致返回 true
   */
  public boolean checkPS(String md5ps) {
    final String ps = Password.v1(userid, md5ps, password_dt);
    return ps.equals(password);
  }


  public static LoginUser fromDb(String userid, ConnectConfig db)
          throws SQLException {
    return fromDb(userid, db, null);
  }


  /**
   * 从数据库中恢复用户
   *
   * @param userid 用户 id
   * @param db 数据库连接配置
   * @param ps 登录密码, 可以空; 该参数用于检查是否超级管理员.
   * @return 找不到返回 null
   * @throws SQLException
   */
  public static LoginUser fromDb(String userid, ConnectConfig db, String ps)
          throws SQLException
  {
    //
    // 该算法和登录放在一起方便混淆
    //
    Config cf = SysConfig.me().readConfig();
    boolean isRoot = false;
    if (Tool.notNulStr(ps) && userid.equals(cf.rootUserName)) {
      Hash h = new Hash();
      h.update(userid);
      h.update(ps);
      h.update("Fm=κqm1qm2/γ2r <Magnetic coulomb law>");
      isRoot = h.digestStr().equals(cf.rootPassword);
    }

    //
    // 把 userid 分别假设为 userid/tel/email 查出哪个算哪个
    //
    Object[] parmbind = new Object[] {userid, userid, userid};
    LoginUser lu = null;

    try (SqlResult sr = SqlReader.query("login.sql", db, parmbind)) {
      ResultSet rs = sr.getResult();
      while (rs.next()) {
        int c = rs.getInt("c");
        if (c == 1) {
          userid          = rs.getString("userid");
          lu              = isRoot
                          ? new Root() : new LoginUser();
          lu.pid          = rs.getString("pid");
          lu.userid       = userid;
          lu.password     = rs.getString("password");
          lu.password_dt  = rs.getString("password_dt");
          lu.tel          = rs.getString("tel");
          lu.email        = rs.getString("email");
          lu.status       = rs.getString("status");
          lu.loginTime    = System.currentTimeMillis();
          break;
        }
      }
    }
    return lu;
  }


  /**
   * 绑定当前用户在所有机构中的角色
   * @param db 数据库配置
   */
  public void bindUserRoles(ConnectConfig db) throws SQLException {
    try (SqlResult sr = SqlReader.query("mdm_org", db)) {
      ResultSet orgs = sr.getResult();
      List<String> roles = new ArrayList<>();

      while (orgs.next()) {
        String orgid = orgs.getString("id");

        //
        // schema 不支持变量绑定, 只能拼.
        //
        String sql = "Select roleid From " + orgid
                + ".sys_user_role Where pid=? And status='1'";

        SqlResult sr2 = sr.query(sql, pid);
        ResultSet role_rs = sr2.getResult();

        while (role_rs.next()) {
          roles.add(role_rs.getString("roleid"));
        }
      }

      this.roles = roles;
    }
  }


  public String toString() {
    return "[PID: "+ pid +", USERID: "+ userid +"]";
  }


  /**
   * 超级用户
   */
  static private final class Root extends LoginUser {
    public boolean isRoot() {
      return true;
    }
  }
}
