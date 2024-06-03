////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-27 上午7:23
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/event/timer/EarlyMorning.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.event.timer;

import com.xboson.log.StaticLogProvider;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;


/**
 * 每天凌晨都会执行, 准确时间是在明天 00:01:00 执行.
 */
public class EarlyMorning extends StaticLogProvider {

  public static final Date first;
  public static final long hour24 = 24 * 60 * 60 * 1000;


  static {
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DATE, 1);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 1);
    c.set(Calendar.SECOND, 0);
    first = c.getTime();
  }


  private static void __add(final TimerTask task) {
    TimeFactory.me().scheduleAtFixedRate(task, first, hour24);
  }


  /**
   * 注册到凌晨事件中, 每天凌晨触发同步
   * @param task
   */
  public static void add(final TimerTask task) {
    __add(task);
    openLog(EarlyMorning.class).info("DO", task, "Tomorrow");
  }


  /**
   * 注册到凌晨事件中, 每天凌晨触发同步
   * @param task
   */
  public static void add(final Runnable task) {
    __add(new RunnableWrap(task));
    openLog(EarlyMorning.class).info("DO", task, "Tomorrow");
  }


}
