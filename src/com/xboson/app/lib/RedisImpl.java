////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-7 下午5:28
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/RedisImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.app.AppContext;
import com.xboson.auth.impl.RoleBaseAccessControl;
import com.xboson.auth.impl.ResourceRoleTypes;
import com.xboson.been.LoginUser;
import com.xboson.been.XBosonException;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.sleep.SafeDataFactory;
import com.xboson.util.Tool;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * 脚本将平台上的很多敏感数据(如数据库密码) 推送到缓存中, 部分敏感数据已经加密
 * @see SafeDataFactory 加密策略
 */
public class RedisImpl implements IApiConstant {

  private final String key_prefix;


  RedisImpl(String prefix) {
    key_prefix = prefix;
  }


  public void set(String region, String key, String value, int exp) {
    try (Jedis client = RedisMesmerizer.me().open()) {
      String tkey = key_prefix + region;
      SafeDataFactory.IEncryptionStrategy enc = SafeDataFactory.get(tkey);
      client.hset(tkey, enc.encodeKey(key), enc.encodeData(value));
      // 超时会导致整个 region 都失效, 不能这样设置
      // if (exp > 0) client.expire(tkey, exp);
    }
  }


  public String get(String region, String key) {
    try (Jedis client = RedisMesmerizer.me().open()) {
      String tkey = key_prefix + region;
      SafeDataFactory.IEncryptionStrategy enc = SafeDataFactory.get(tkey);
      String sval = client.hget(tkey, enc.encodeKey(key));
      if (sval == null) return null;
      return enc.decodeData(sval);
    }
  }


  public void del(String region, String key) {
    try (Jedis client = RedisMesmerizer.me().open()) {
      String tkey = key_prefix + region;
      SafeDataFactory.IEncryptionStrategy enc = SafeDataFactory.get(tkey);
      client.hdel(tkey, enc.encodeKey(key));
    }
  }


  public void del(String region) {
    try (Jedis client = RedisMesmerizer.me().open()) {
      String tkey = key_prefix + region;
      client.del(tkey);
    }
  }


  public List<Object> delAll(String region, String[] keys) throws IOException {
    try (Jedis client = RedisMesmerizer.me().open();
         Transaction t = client.multi() )
    {
      String tkey = key_prefix + region;
      SafeDataFactory.IEncryptionStrategy enc = SafeDataFactory.get(tkey);

      for (int i=0; i<keys.length; ++i) {
        t.hdel(tkey, enc.encodeKey(keys[i]));
      }
      return t.exec();
    }
  }


  public Object keys(ScriptObjectMirror list, String region) {
    try (Jedis client = RedisMesmerizer.me().open()) {
      String tkey = key_prefix + region;
      SafeDataFactory.IEncryptionStrategy enc = SafeDataFactory.get(tkey);
      Iterator<String> it = client.hkeys(tkey).iterator();
      int i = list.size() - 1;

      while (it.hasNext()) {
        list.setSlot(++i, enc.decodeKey(it.next()));
      }
    }
    return list;
  }


  public Object keys(ScriptObjectMirror list, String region, String pattern) {
    String tkey = key_prefix + region;

    SafeDataFactory.IEncryptionStrategy s = SafeDataFactory.getMaybeNull(tkey);
    if (s != null && !s.keyAmbiguous()) throw new UnsupportedOperationException(
              "The region '"+ region +"' is encrypted and cannot be ambiguous");

    String cursor = RedisMesmerizer.BEGIN_OVER_CURSOR;
    ScanParams sp = new ScanParams();
    sp.match(pattern);
    int i = list.size() - 1;

    try (Jedis client = RedisMesmerizer.me().open()) {
      for (;;) {
        ScanResult<Map.Entry<String, String>> sr = client.hscan(tkey, cursor, sp);
        Iterator<Map.Entry<String, String>> it = sr.getResult().iterator();

        while (it.hasNext()) {
          Map.Entry<String, String> item = it.next();
          list.setSlot(++i, item.getKey());
        }

        cursor = sr.getStringCursor();
        if (cursor.equals(RedisMesmerizer.BEGIN_OVER_CURSOR))
          break;
      }
    }
    return list;
  }


  /**
   * 返回当前用户角色, 对资源类型的访问权限数据
   *
   * @param type 资源类型
   * @param resourceId 资源的主键
   * @return 在 redis 中存储的值, 可能是 "0"
   */
  public Object getRoleInfo(ResourceRoleTypes type, String resourceId) {
    LoginUser user = (LoginUser) AppContext.me().who();
    return RoleBaseAccessControl.check(user, type, resourceId, false);
  }
}
