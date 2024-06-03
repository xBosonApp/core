////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-1 上午10:22
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/DateParserFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 池化的日期解析器工厂
 */
public class DateParserFactory {

  private static GenericKeyedObjectPool<String, SimpleDateFormat> pool;
  private static GenericObjectPool<Calendar> calendars;

  static {
    pool = new GenericKeyedObjectPool<>(new DateParserPool(),
            SysConfig.defaultKeyPoolConfig());

    calendars = new GenericObjectPool(new CalendarPool(),
            SysConfig.defaultPoolConfig());
  }


  /**
   * 返回一个日期解析器
   */
  public static DateParser get(String format) {
    try {
      SimpleDateFormat f = pool.borrowObject(format);
      return new DateParser(f);
    } catch(Exception e) {
      throw new XBosonException(e);
    }
  }


  /**
   * 创建或从池中取出 Calendar 对象
   */
  public static Calendar getCalendar() {
    try {
      return calendars.borrowObject();
    } catch (Exception e) {
      throw new XBosonException(e);
    }
  }


  /**
   * 使用一个指定的时间, 初始化 Calendar
   */
  public static Calendar getCalendar(Date d) {
    Calendar c = getCalendar();
    c.setTime(d);
    return c;
  }


  /**
   * 将对象还给池, 可以循环利用
   */
  public static void freeCalendar(Calendar c) {
    calendars.returnObject(c);
  }


  /**
   * 包装了 SimpleDateFormat 用于时间转换, 对象已经池化用于优化性能.
   * 当该对象不再使用, 应调用 close 关闭.
   */
  static public class DateParser implements AutoCloseable {
    private SimpleDateFormat formater;

    DateParser(SimpleDateFormat wrap) {
      this.formater = wrap;
    }

    /**
     * 释放资源到资源池
     */
    @Override
    public void close() throws Exception {
      pool.returnObject(formater.toPattern(), formater);
      formater = null;
    }

    /**
     * @see SimpleDateFormat#format(Date)
     */
    public String format(Date d) {
      return formater.format(d);
    }

    /**
     * @see SimpleDateFormat#parse(String)
     */
    public Date parse(String text) {
      try {
        return formater.parse(text);
      } catch (ParseException e) {
        throw new XBosonException(e);
      }
    }

    @Override
    protected void finalize() throws Throwable {
      close();
    }
  }


  /**
   * 持久化 DateParser 工厂, 用于生产实际的对象
   */
  static class DateParserPool extends
          BaseKeyedPooledObjectFactory<String, SimpleDateFormat> {

    @Override
    public SimpleDateFormat create(String format) throws Exception {
      return new SimpleDateFormat(format);
    }

    @Override
    public PooledObject<SimpleDateFormat> wrap(SimpleDateFormat f) {
      return new DefaultPooledObject<>(f);
    }
  }


  static class CalendarPool extends BasePooledObjectFactory<Calendar> {

    @Override
    public Calendar create() throws Exception {
      return Calendar.getInstance();
    }

    @Override
    public PooledObject<Calendar> wrap(Calendar c) {
      return new DefaultPooledObject<>(c);
    }
  }


  /**
   * 对池化的 Calendar 的包装, 简化使用.
   * c++ 风格的资源释放器.
   */
  static public class ScopeCalendar implements AutoCloseable {
    public Calendar c;

    public ScopeCalendar() {
      c = getCalendar();
    }

    public ScopeCalendar(Date d) {
      c = getCalendar(d);
    }

    @Override
    public void close() throws Exception {
      freeCalendar(c);
      c = null;
    }
  }
}
