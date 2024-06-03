////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-23 上午9:55
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/log/slow/AccessLog.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.log.slow;

import com.xboson.been.LoginUser;
import com.xboson.db.sql.SqlReader;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;


/**
 * 记录用户登录时的日志
 */
public class AccessLog extends AbsSlowLog {

  public static final String SQL_FILE = "log_access.sql";


  public AccessLog() {
  }


  @Override
  protected String getSql() {
    return SqlReader.read(SQL_FILE);
  }


  /**
   * 记录用户登录日志
   * @param user 登录用户
   * @param state 字典 ZR.0023 (<9999)
   * @param message 自定义消息, 可以 null
   */
  public void log(LoginUser user, int state, String message) {
    log(user.pid, state, message);
  }


  public void log(String user_pid, int state, String message) {
    insert( uuid.ds(),
            nowInternet(),
            message,
            user_pid,
            Integer.toString(state) );
  }


  @Override
  public String logName() {
    return "log-access";
  }
}
