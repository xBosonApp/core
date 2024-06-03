////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-23 上午11:22
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/CacheImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.been.CallData;
import com.xboson.util.c0nst.IConstant;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.IOException;


/**
 * 每个请求一个实例
 */
public class CacheImpl extends RuntimeUnitImpl {

  private RedisImpl redis;


  public CacheImpl(CallData cd, String orgid) {
    super(cd);
    redis = new RedisImpl("/user_space/" + orgid + '/');
  }


  public void set(String region, String key, Object value) {
    set(region, key, value, IConstant.DEFAULT_TIMEOUT);
  }


  public void set(String region, String key, Object value, int exp) {
    String str = jsonStringify(value);
    redis.set(region, key, str, exp);
  }


  public Object get(String region, String key) {
    String s = redis.get(region, key);
    return jsonParse(s);
  }


  public Object del(String region, String key) {
    redis.del(region, key);
    return key;
  }


  public Object delAll(String region, String[] keys) throws IOException {
    return redis.delAll(region, keys);
  }


  public Object keys(String region) {
    return redis.keys(createJSList(), region);
  }
}
