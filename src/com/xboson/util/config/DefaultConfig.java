////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 下午8:04
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/DefaultConfig.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util.config;

import com.xboson.been.Config;
import com.xboson.been.MongoConfig;
import com.xboson.been.XBosonException;
import com.xboson.db.ConnectConfig;
import com.xboson.db.DBPoolConfig;
import com.xboson.script.lib.Uuid;
import com.xboson.test.Test;
import com.xboson.util.Password;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;


/**
 * 配置文件默认值
 */
public final class DefaultConfig {

  /** `sys_pl_` 前缀的表自动设置为共享表 */
  public static final String default_sys_tables = "sys_tenant,sys_tenant_user," +
          "sys_base_tbl,sys_config,sys_server,sys_system,sys_sqls,sys_userinfo," +
          "sys_user_identity,mdm_personal_info,mdm_org,sys_eeb_run_conf," +
          "sys_eeb_work_node,sys_eeb_jobgroup,sys_eeb_varnish,sys_eeb_sche," +
          "sys_eeb_statistics,sys_eeb_detail";

  public static final String default_share_app = "ZYAPP_IDE,ZYAPP_MENU," +
          "ZYAPP_SYSMGT,ZYAPP_LOGIN,auth,c9e98ea6fc7148d186289e8c33776f8a," +
          "03229cbe4f4f11e48d6d6f51497a883b,d2c8511b47714faba5c71506a5029d94," +
          "26c0f25501d24c0993515d445e1215a5,c770045becc04c7583f626faacd3b456," +
          "c879dcc94d204d96a98a34e0b7d75676,a20a0c6a82fb4cb085cb816e5526d4bc," +
          "e0ef1b25da204227b305fd40382693e6,apils";

  private static Properties p;


  /**
   * 将配置设置成为默认设置
   */
  public static void setto(Config c) {
    Uuid uuid = new Uuid();

    c.configVersion     = Config.VERSION;
    c.loggerWriterType  = "ConsoleOut";
    c.logLevel          = "info";
    c.sessionTimeout    = IConstant.DEFAULT_TIMEOUT / 60;
    c.sessionPassword   = Test.randomString(20);
    c.debugService      = true;
    c.rootUserName      = "admin-pl";
    c.rootPassword      = "unnecessary";
    c.rootPid           = "unnecessary";

    c.rootPassword =
            Password.v1(c.rootUserName, Password.md5lowstr(c.rootPassword));

    c.uiProviderClass   = "local";
    c.uiUrl             = "/web4xboson/public";
    c.uiWelcome         = "/face/ui/paas/login.html";
    c.clusterNodeID     = IConstant.DEFAULT_NODE_ID_SHORT;
    c.sysTableList      = toList(default_sys_tables);
    c.shareAppList      = toList(default_share_app);
    c.nodeProviderClass = "local";
    c.nodeUrl           = "/web4xboson/xboson-node-modules";
    c.shellUrl          = "/web4xboson/shell-script";

    JedisPoolConfig j = c.jedispool = new JedisPoolConfig();
    j.setMaxIdle(10);
    j.setMinIdle(0);
    j.setMaxTotal(200);

    DBPoolConfig d = c.dbpool = new DBPoolConfig();
    d.setMaxTotal(2000);
    d.setBlockWhenExhausted(true);
    d.setMaxWaitMillis(3000);
    d.setTestOnBorrow(true);
    d.setTestOnCreate(true);
    d.setTestOnReturn(false);
    d.setTimeBetweenEvictionRunsMillis((long)(1 * 3600e3));
    d.setTestWhileIdle(true);
    d.setNumTestsPerEvictionRun(-1);

    ConnectConfig db = c.db = new ConnectConfig();
    db.setHost("localhost");

    ConnectConfig redis = c.redis = new ConnectConfig();
    redis.setHost("localhost");
    redis.setPort("6379");
    redis.setPassword("");

    MongoConfig mc = c.mongodb = new MongoConfig();
    mc.host = "localhost";
    mc.port = 27017;
    mc.database = "xboson";
    mc.username = "";
    mc.password = "";
    mc.enable = false;
  }


  /**
   * 配置属性的注释, 在当前包 config-comments.prop 文件中.
   */
  public synchronized static Properties comments() {
    if (p == null) {
      StringBufferOutputStream buf = Tool.readFileFromResource(
              DefaultConfig.class, "config-comments.prop");
      p = new Properties();
      try {
        StringReader r = new StringReader(buf.toString());
        p.load(r);
      } catch (IOException e) {
        throw new XBosonException.IOError(e);
      }
    }
    return p;
  }


  private static String[] toList(String str) {
    return str.split(",");
  }


  private DefaultConfig() {}
}
