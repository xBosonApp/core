////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-19 下午2:04
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/log/StaticLogProvider.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.log;

import com.xboson.been.XBosonException;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * 为纯静态类提供日志创建服务, 普通类不要用.
 * 该类创建的日志对象在不常使用后会被自动回收.
 * 日志对象仅在调用了 openLog() 后被创建.
 *
 * @see LogFactory#create() 普通类构建日志的方法
 */
public class StaticLogProvider {

  private static Map<String, Log> classMap =
          Collections.synchronizedMap(new WeakHashMap<>());


  /**
   * 打开日志, 不推荐
   *
   * @see #openLog(Class) 推荐
   * @deprecated 会从错误堆栈中取出对象名作为日志名称, 效率低
   */
  protected static Log openLog() {
    Exception e = new Exception();
    StackTraceElement[] t = e.getStackTrace();
    return openLog(t[1].getClassName());
  }


  /**
   * 打开日志, 该方法效率较高
   */
  protected static Log openLog(Class c) {
    return openLog(c.getName());
  }


  /**
   * 打开日志, 该方法效率较高
   */
  protected static Log openLog(String className) {
    if (className == null)
      throw new XBosonException.NullParamException("String className");

    Log log = classMap.get(className);
    if (log == null) {
      synchronized (StaticLogProvider.class) {
        log = classMap.get(className);
        if (log == null) {
          log = LogFactory.create(className);
          classMap.put(className, log);
        }
      }
    }
    return log;
  }
}
