////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午3:33:52
// 原始文件路径: xBoson/src/com/xboson/service/UserService.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.service;

import com.xboson.app.AppContext;
import com.xboson.been.*;
import com.xboson.db.ConnectConfig;
import com.xboson.db.IDict;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.j2ee.container.XPath;
import com.xboson.j2ee.container.XService;
import com.xboson.log.slow.AccessLog;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.*;
import com.xboson.util.c0nst.IConstant;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@XPath("/user")
public class UserService extends XService implements IDict, IConstant {

  public static final int IP_BAN_COUNT    = 10;
  public static final int IP_WAIT_TIMEOUT = 10; // 分钟
  public static final int IP_NEED_CAPTCHA = 5;

  public static final String MSG
          = "Provide sub-service name '/user/[sub service]'";

  /**
   * 跳过登录检查的服务名称列表
   */
  private static final Set<String> skipCheckLogin = JavaConverter.param2set(
          "get_havinguser",
          "register"
  );


  private Config cf;
  private AccessLog access;


  public UserService() {
    cf = SysConfig.me().readConfig();
    access = new AccessLog();
  }


  /**
   * 除了登录/登出方法外, 其他 api 都是写在脚本中的.
   */
  public void service(CallData data) throws Exception {
    try {
      subService(data, MSG);
    } catch (XBosonException.NoService ns) {
      OpenApp.banAnonymous(data);

      ApiCall ac = new ApiCall(
        data.getString("org", 0, 100),
        data.getString("app", 1, 100),
        data.getString("mod", 1, 100),
        ns.getServiceName()
      );
      ac.call = data;

      if (skipCheckLogin.contains(ac.api)
              && ac.app.startsWith(SYS_APP_PREFIX)
              && ac.mod.startsWith(SYS_MOD_PREFIX)) {
        AppContext.me().call(ac);
      } else {
        checkLoging(data);
        AppContext.me().call(ac);
      }
    }
  }


  private void msg(CallData data, String msg, int code) throws IOException {
    data.xres.responseMsg(msg, code);
    if (data.sess.login_user != null) {
      access.log(data.sess.login_user, code, msg);
    } else {
      access.log(data.getString("userid", 0, 50), code, msg);
    }
  }


  /**
   * 检查登录攻击, 失败次数, 验证图片
   * password 参数必须已经 md5 后传入接口.
   *
   * @see com.xboson.test.TestSign#admin() 计算 root 用户密钥.
   */
	public void login(CallData data) throws Exception {
	  if (isLogin(data) && !OpenApp.isAnonymousUser(data)) {
      data.xres.bindResponse("openid", data.sess.login_user.userid);
      msg(data, "用户已经登录", 0);
      return;
    }

    final int login_count = checkIpBan(data);
    if (login_count >= IP_BAN_COUNT) {
      msg(data, "登录异常, 等待"+ IP_WAIT_TIMEOUT +"分钟后重试", 997);
	    return;
    }

    final String md5ps  = data.getString("password", 6, 40);
    final String userid = data.getString("userid",   4, 50);

    if (login_count >= IP_NEED_CAPTCHA) {
      String c = data.getString("c", 0, 20);
      if (! c.equalsIgnoreCase( data.sess.captchaCode )) {
        msg(data, "验证码错误", 10);
        ipLoginFail(data);
        return;
      }
    }

    OpenApp.banAnonymous(userid);

    LoginUser lu = LoginUser.fromDb(userid, cf.db, md5ps);
    if (lu == null) {
      msg(data, "用户不存在", 1014);
      ipLoginFail(data);
      return;
    }
    if (! ZR001_ENABLE.equals(lu.status)) {
      msg(data, "用户已锁定", 1004);
      ipLoginFail(data);
      return;
    }
    if (! lu.checkPS(md5ps)) {
      access.log(userid, 1001, "用户名或密码错误");
      throw new XBosonException("用户名或密码错误", 1001);
    }

    lu.bindUserRoles(cf.db);
    lu.password = null;

    data.sess.login_user = lu;
    data.xres.bindResponse("openid", lu.userid);
    msg(data, "成功登录系统", 0);
	}


	public void logout(CallData data) throws Exception {
    if (!isLogin(data)) {
      data.xres.responseMsg("未登录", 1000);
      return;
    }

    try {
      msg(data, "已登出", 1007);
    } finally {
      data.sess.login_user = null;
      data.sess.destoryFlag();
    }
  }


  /**
   * ip 登录失败次数超限返回 true
   */
  private int checkIpBan(CallData cd) {
    try (Jedis client = RedisMesmerizer.me().open()) {
      String key = "/ip-ban/" + cd.req.getRemoteAddr();
      String v = client.get(key);
      if (v == null) return 0;
      return Integer.parseInt(v);
    }
  }


  /**
   * 记录一次 ip 失败登录次数
   */
  private void ipLoginFail(CallData cd) {
	  try (Jedis client = RedisMesmerizer.me().open();
         Transaction t = client.multi() ) {
	    String key = "/ip-ban/" + cd.req.getRemoteAddr();
	    t.incr(key);
	    t.expire(key, IP_WAIT_TIMEOUT * 60);
	    t.exec();
    } catch (IOException e) {
      throw new XBosonException(e);
    }
  }


  private boolean isLogin(CallData data) {
    return data.sess.login_user != null
            && data.sess.login_user.userid != null;
  }


	@Override
	public boolean needLogin() {
		return false;
	}

}
