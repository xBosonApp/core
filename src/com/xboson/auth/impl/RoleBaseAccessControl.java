////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改, 
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-11 上午11:41
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/auth/impl/RoleBaseAccessControl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.auth.impl;

import com.xboson.app.lib.IApiConstant;
import com.xboson.auth.IAResource;
import com.xboson.been.LoginUser;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.sleep.SafeDataFactory;
import com.xboson.util.Ref;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @see ResourceRoleTypes
 */
public class RoleBaseAccessControl {

  /** 表示用户可以访问对应的资源, 但是程序中只是判断是否返回非空 */
  public final static String PASS = "0";

  /** 和 NOAUTH 比较效率超过 PASS */
  public final static String NOAUTH = null;

  public final static String RBAC_HKEY
          = IApiConstant._R_KEY_PREFIX_
          + IApiConstant._CACHE_REGION_RBAC_;

  /**
   * [用户 : [资源ID : 缓存的权限值]]
   * 在内存中, 用户直接与资源关联, 抛弃角色这个中间层.
   * 当角色变更后, 用户需要重新登录方能更新权限.
   */
  private static Map<String, Map<String, Ref<String>>> user2resource =
          Collections.synchronizedMap(new WeakHashMap<>());

  private static final SafeDataFactory.IEncryptionStrategy
          enc = SafeDataFactory.get(SafeDataFactory.SCENES_RBAC);


  /**
   * @see #check(LoginUser, ResourceRoleTypes, String, boolean)
   */
  public static String check(LoginUser user,
                             ResourceRoleTypes type,
                             IAResource res,
                             boolean checkPublic) {
    return check(user, type, res.description(), checkPublic);
  }


  /**
   * 检查用户对资源的访问权限; 会尝试从缓存中检查;
   * 内存作为一级缓存, Redis 缓存作为二级缓存;
   *
   * @param user 用户
   * @param type 资源类型
   * @param resID 资源描述
   * @param checkPublic 是否检查对公共资源的权限,
   *                    如果资源没有公共权限, false 可以提升效率.
   * @return 在缓存中存储的值, 当前系统中, "0" 表示可以访问, null 表示禁止访问.
   */
  public static String check(LoginUser user,
                             ResourceRoleTypes type,
                             String resID,
                             boolean checkPublic) {

    String userMask = user.userid + user.loginTime;
    String resMask  = resID + type.onlyPrefix;
    Ref<String> ref = fromMemory(userMask, resMask);
    if (ref != null) return ref.x;

    String hasAuth  = NOAUTH;

    try (Jedis client = RedisMesmerizer.me().open()) {
      for (String roleid : user.roles) {
        //
        // 角色对应的资源
        // roleid : typeID : resource
        //
        hasAuth = client.hget(RBAC_HKEY, enc.encodeKey(type.toKEY(roleid, resID)));
        if (hasAuth != NOAUTH) break;

        //
        // 公共资源
        // typeID : resource
        //
        if (checkPublic) {
          hasAuth = client.hget(RBAC_HKEY, enc.encodeKey(type.toKEY(resID)));
          if (hasAuth != NOAUTH) break;
        }
      }
    }

    saveMemory(userMask, resMask, hasAuth);
    return hasAuth;
  }


  /**
   * 从内存中检查资源权限
   */
  private static Ref<String> fromMemory(String userMask,
                                        String resMask) {
    Map<String, Ref<String>> resourceList = user2resource.get(userMask);
    if (resourceList == null)
      return null;

    Ref<String> ref = resourceList.get(resMask);
    if (ref == null)
      return null;

    return ref;
  }


  /**
   * 保存用户对资源的访问权限到内存中
   */
  private static void saveMemory(String userMask,
                                 String resMask,
                                 String auth) {
    Map<String, Ref<String>> resourceList = user2resource.get(userMask);
    if (resourceList == null) {
      resourceList = new ConcurrentHashMap<>();
      user2resource.put(userMask, resourceList);
    }
    resourceList.put(resMask, new Ref(auth));
  }


  private RoleBaseAccessControl() {}
}
