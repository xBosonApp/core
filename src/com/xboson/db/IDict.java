////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-22 下午1:32
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/IDict.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db;

/**
 * 提供了常用字典的常量, 直接继承该接口
 */
public interface IDict {

  String ZR001_ENABLE = "1";
  String ZR001_DISABLE = "0";


  /** SYS08.00.006.00 */
  String ADMIN_FLAG_USER = "0";
  String ADMIN_FLAG_ADMIN = "1";
  String ADMIN_FLAG_TENANT_ADMIN = "2";


  int JOB_UNIT_YEAR   = 30;
  int JOB_UNIT_MONTH  = 31;
  int JOB_UNIT_WEEK   = 40;
  int JOB_UNIT_DAY    = 50;
  int JOB_UNIT_DAY2   = 60;
  int JOB_UNIT_HOUR   = 70;
  int JOB_UNIT_SECOND = 90;
  int JOB_UNIT_MINUTE = 80;


  /** 初始化, 尚未运行过 */
  int JOB_STATUS_INIT     = 0;
  /** 运行的任务正在请求 api 但未返回 */
  int JOB_STATUS_RUNNING  = 1;
  /** 运行的任务休眠中 */
  int JOB_STATUS_STOP     = 3;
  /** 系统错误, 网络不通等 */
  int JOB_STATUS_ERR      = 2;
  /** 达到结束时间 */
  int JOB_STATUS_TIMEUP   = 4;
  /** 达到运行次数 */
  int JOB_STATUS_MAXCOUNT = 5;
  /** api 返回了一些东西 */
  int JOB_STATUS_LOG      = 6;
  /** 任务已经删除 */
  int JOB_STATUS_DEL      = 7;

  /** 异常类型(ZR.0024) */
  String ERR_TYPE_API     = "API";
  String ERR_TYPE_NONE    = "NONE";
}
