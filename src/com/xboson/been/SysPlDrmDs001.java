////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-22 下午1:43
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/SysPlDrmDs001.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import com.xboson.script.IVisitByScript;

import java.math.BigDecimal;


/**
 * sys_pl_drm_ds001 表映射, 数据库资源管理
 */
public class SysPlDrmDs001 implements IVisitByScript {
  public String did;    // 数据源ID
  public String dn;     // 数据源名称
  public String owner;  // 所有者, 就是 orgid
  public String dbtype; // 数据库类型
  public String cn;     // 数据库中文名称
  public String flg;    // 0 平台, 1 第三方, 9 xBoson 创建
  public String mark;   // 说明
  public String status;

  public String dhost;
  public BigDecimal dport;
  public String url;
  public String user_name;
  public String pass;
  public String en;     // 数据库物理名称
}
