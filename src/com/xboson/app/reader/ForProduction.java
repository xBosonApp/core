////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-16 上午10:24
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/reader/ForProduction.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.reader;


import com.xboson.app.ApiPath;
import com.xboson.app.XjOrg;
import com.xboson.been.XBosonException;
import com.xboson.fs.script.ScriptAttr;
import com.xboson.sleep.RedisMesmerizer;
import redis.clients.jedis.Jedis;

import java.util.Date;


public class ForProduction extends AbsReadScript {

  public final static String REGION = _R_KEY_PREFIX_ + _CACHE_REGION_API_;


  @Override
  public ScriptFile read(XjOrg org, String app, String mod, String api) {
    log.debug("Script From Redis", mod, api);

    try (Jedis j = RedisMesmerizer.me().open()) {
      String key = (app + mod + api).toLowerCase();
      String arr = j.hget(REGION, key);

      if (arr != null) {
        int a = arr.indexOf("\"");
        if (a >= 0) {
          ++a;
          int b = arr.indexOf("\"", a);
          if (b > a) {
            Date now        = new Date();
            ScriptAttr attr = new ScriptAttr();
            ScriptFile file = makeFile(attr, arr.substring(a, b));
            attr.fileSize   = file.content.length;
            attr.fileName   = api;
            attr.pathName   = '/' + mod;
            attr.fullPath   = ApiPath.toFile(mod, api);
            attr.createTime = now.getTime();
            attr.modifyTime = now.getTime();

            log.debug("Load Script from CACHE:", mod, '/', api);
            return file;
          }
        }
      }
    } catch (Exception e) {
      log.warn("Script from Redis fail", mod, api);
    }
    throw new XBosonException.NotFound("API:" + api);
  }


  @Override
  public String logName() {
    return "read-product-api";
  }
}
