////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-15 下午5:01
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/ApiPath.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app;

import com.xboson.been.ApiCall;

import java.util.Map;


/**
 * 统一所有关于 api 生成抽象路径的算法, 保证同样参数生成的路径相同;
 * 路径中已经含有文件系统前缀.
 */
public class ApiPath {


  /** 核心 */
  public static String getPath(String org, String app, String mod, String api) {
    return "/" + org + "/" + app + "/" + mod + "/" + api;
  }


  public static String getPath(ApiCall ac) {
    return getPath(ac.org, ac.app, ac.mod, ac.api);
  }


  public static String getPath(Map<String, Object> map, String api) {
    return getPath((String) map.get("org"),
            (String) map.get("app"), (String) map.get("mod"), api);
  }


  public static String getModPath(Map<String, Object> map) {
    return "/"+ map.get("org") +'/'+ map.get("app") +'/'+ map.get("mod");
  }


  public static String getAppPath(Map<String, Object> map) {
    return "/"+ map.get("org") +'/'+ map.get("app");
  }


  public static String toFile(String module_id, String api_id) {
    return '/' + module_id + '/' + api_id;
  }


  public static String getPathOrgFromContext(String app, String mod, String api) {
    return getPath(AppContext.me().originalOrg(), app, mod, api);
  }


  /**
   * 返回 api 脚本修改事件的消息名称
   * type - 消息类型前缀
   * filename
   */
  public static String getEventPath(String type, String filename) {
    return type + filename.toLowerCase();
  }


  public static String getEventPath(String filename) {
    ApiTypes type = AppContext.me().getApiModeType();
    return getEventPath(type.eventPrifix, filename);
  }


  public static String getEventPath() {
    AppContext ac = AppContext.me();
    return getEventPath(ac.getApiModeType(), ac.getCurrentApiPath());
  }


  public static String getEventPath(ApiTypes type, String filename) {
    return getEventPath(type.eventPrifix, filename);
  }
}
