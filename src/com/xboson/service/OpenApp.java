////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-19 下午1:16
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/service/OpenApp.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.service;

import com.xboson.app.AppContext;
import com.xboson.auth.PermissionException;
import com.xboson.been.*;
import com.xboson.j2ee.container.XPath;
import com.xboson.j2ee.container.XService;
import com.xboson.util.JavaConverter;
import com.xboson.util.SysConfig;

import java.sql.SQLException;
import java.util.Set;


/**
 * 只能被匿名用户访问的接口, 权限范围由平台上的 'anonymous' 用户确定.
 * 为安全起见, 匿名用户不能通过其他 service 访问接口, 并且黑名单
 * 中的应用无论如何也禁止访问 (即使匿名角色有该权限).
 */
@XPath("/openapp")
public class OpenApp extends XService {

  private static final String BAN_AM    = "Anonymous users are prohibited";
  private static final String BAN_APP   = "Application is forbidden";
  private static final String Anonymous = "anonymous";

  /**
   * 这些模块禁止访问, 对于信息安全敏感的接口模块都需要放在这里.
   */
  private static final Set<String>
  BlackAppList = JavaConverter.arr2set(new String[] {
          "03229cbe4f4f11e48d6d6f51497a883b", // 应用信息管理
          "0418a865dac144cfa77a1e4573e3f549", // 智云运营管理
          "c9e98ea6fc7148d186289e8c33776f8a", // 智云运营管理通用
          "26c0f25501d24c0993515d445e1215a5", // 缓存管理
          "a20a0c6a82fb4cb085cb816e5526d4bc", // 计划任务
          "ac25e37830ec4e6cbe367a51a4005b7e", // 导入导出
          "c770045becc04c7583f626faacd3b456", // 业务模型管理
          "c879dcc94d204d96a98a34e0b7d75676", // 元数据管理
          "cfb82858dc0a4598834d356c661a678f", // 日志分析
          "f7b67a9e96864350963f1a470ff0eda7", // 日志管理
          "d2c8511b47714faba5c71506a5029d94", // 主数据管理系统
          "e0ef1b25da204227b305fd40382693e6", // 进程管理
          "81092b8cd82041a2b81296409eba92da", // 区块链管理
          "apils",        // 平台 API 列表
          "auth",         // 授权管理
          "zyapp_ide",    // 平台系统应用 - IDE
          "zyapp_login",  // 用户登录
          "zyapp_menu",   // 主界面菜单
          "zyapp_sysmgt", // 智云系统管理
  });

  private Config cf;


  public OpenApp() {
    cf = SysConfig.me().readConfig();
  }


  @Override
  public void service(CallData data) throws Exception {
    if (data.sess.login_user == null) {
      data.sess.login_user = buildAnonymousUser();
    }

    AppContext af = AppContext.me();
    data.url.setErrorMessage(App.PATH_FOTMAT);

    ApiCall ac = new ApiCall(data.url);
    if (BlackAppList.contains(ac.app)) {
      throw new PermissionException(BAN_APP);
    }
    ac.call = data;

    af.call(ac);
  }


  /**
   * 如果是匿名用户返回 true, d 不能空
   */
  protected static boolean isAnonymousUser(CallData d) {
    if (d.sess == null ||
            d.sess.login_user == null ||
            d.sess.login_user.userid == null)
      return false;

    return Anonymous.equals(d.sess.login_user.userid);
  }


  protected static void banAnonymous(String userid) {
    if (Anonymous.equals(userid)) {
      throw new XBosonException(BAN_AM);
    }
  }


  /**
   * 如果登录用户是匿名用户, 则抛出异常
   */
  protected static void banAnonymous(CallData d) {
    if (isAnonymousUser(d)) {
      throw new XBosonException(BAN_AM);
    }
  }


  @Override
  public String logName() {
    return "open-app";
  }


  @Override
  public boolean needLogin() {
    return false;
  }


  /**
   * 尝试在平台上寻找 anonymous 用户, 并使用该用户作为登录用户, 并绑定角色.
   * 如果平台上不存在该用户, 则生成一个没有角色的临时用户.
   */
  private LoginUser buildAnonymousUser() {
    try {
      LoginUser user = LoginUser.fromDb(Anonymous, cf.db);
      if (user != null) {
        user.bindUserRoles(cf.db);
        return user;
      }
    } catch (SQLException e) {
      log.warn("Not found 'anonymous' user.");
    }
    return new Anonymous();
  }


  private class Anonymous extends LoginUser {
    private Anonymous() {
      pid       = Anonymous;
      userid    = Anonymous;
      loginTime = System.currentTimeMillis();
    }
  }
}
