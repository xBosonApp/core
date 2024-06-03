////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-12 下午1:12
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/ResourceRoleTypes.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.auth.impl;

import com.xboson.auth.IAResource;


/**
 * 资源 id 的枚举, 存储在 redis 中, 用于区分不同资源类型.
 * @see RoleBaseAccessControl
 */
public enum ResourceRoleTypes {
  API("01"),
  MODEL("02"),
  PAGE("04"),
  ELEMENT("05");


  ResourceRoleTypes(String name) {
    this.name       = name;
    this.mid        = ':' + name + ':';
    this.onlyPrefix = ':' + name;
    this.onlySuffix = name + ':';
  }


  public String toString() {
    return mid;
  }


  /**
   * 带有前后 ':' 符号的 name, 适合放在权限的中间.
   */
  public final String mid;

  /**
   * 名称字符串
   */
  public final String name;

  /**
   * 前面带有 ':' 符号的 name
   */
  public final String onlyPrefix;


  /**
   * 后面带有 ':' 符号的 name
   */
  public final String onlySuffix;


  /**
   * 生成角色对该类型资源的访问 key, 该 key 用于访问缓存.
   *
   * @param roleID 角色 id
   * @param resourceDesc 资源描述符
   */
  public String toKEY(String roleID, String resourceDesc) {
    return roleID + mid + resourceDesc;
  }


  /**
   * 生成该类型公共资源的访问 key, 该 key 用于访问缓存.
   *
   * @param resourceDesc 公共资源描述符
   */
  public String toKEY(String resourceDesc) {
    return onlySuffix + resourceDesc;
  }


  /**
   * @see #toKEY(String, String)
   */
  public String toKEY(String roldID, IAResource resource) {
    return toKEY(roldID, resource.description());
  }


  /**
   * @see #toKEY(String)
   */
  public String toKEY(IAResource res) {
    return toKEY(res.description());
  }
}
