////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-18 下午4:49
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/sleep/LuaScript.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.sleep;

import com.xboson.been.XBosonException;
import com.xboson.util.JavaConverter;
import com.xboson.util.StringBufferOutputStream;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisNoScriptException;


/**
 * 在 Redis 中运行的 Lua 脚本, 线程安全
 */
public class LuaScript {

  private final static String[] NUL_PARA = new String[0];

  private String luaScript;
  private String hash;


  private LuaScript(String src) {
    this.luaScript = src;
    this.hash = null;
  }


  private void _complie(Jedis client) {
    hash = client.scriptLoad(luaScript);
  }


  /**
   * 编译一个脚本
   * @param luaScript
   * @return
   */
  public static LuaScript compile(String luaScript) {
    return new LuaScript(luaScript);
  }


  /**
   * @see #compile(String)
   */
  public static LuaScript compile(StringBufferOutputStream buf) {
    return compile(buf.toString());
  }


  /**
   * 运行脚本, 无参数
   */
  public Object eval() {
    return eval(0);
  }


  /**
   * 运行脚本,
   *    key 在脚本中用 KEYS[1~ ] 引用
   *    参数在脚本中使用 ARGV[1~ ] 引用
   *
   * @param keyCount key 在参数中的数量
   * @param parameters 包含 key 和参数
   * @return 结果集
   */
  public Object eval(int keyCount, Object... parameters) {
    try (Jedis client = RedisMesmerizer.me().open()) {
      if (hash == null) {
        _complie(client);
      }

      int retry = 2;
      String[] ps = JavaConverter.toStringArr(parameters, NUL_PARA);

      do {
        try {
          return client.evalsha(hash, keyCount, ps);
        } catch (JedisNoScriptException e) {
          _complie(client);
        }
      } while (--retry > 0);

      throw new XBosonException("Cannot eval lua");
    }
  }


  @Override
  public String toString() {
    return luaScript;
  }
}
