////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午3:22:04
// 原始文件路径: xBoson/src/com/xboson/j2ee/container/XService.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.container;

import com.xboson.been.CallData;
import com.xboson.been.UrlSplit;
import com.xboson.been.XBosonException;
import com.xboson.log.ILogName;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * 可以多线程重入服务实现, 默认总是需要验证.
 *
 * @see #subService 方便制作子服务
 */
public abstract class XService implements ILogName {

  private static final Class[] PARMS_TYPE = new Class[] { CallData.class };


  /**
   * 输出日志
   */
  protected final Log log = LogFactory.create(this);


  /**
   * 子类实现该方法, 当服务被调用, 进入该方法中
   */
  public abstract void service(CallData data) throws Exception;


  /**
   * 子类重写该方法, 当服务器终止时调用
   */
  public void destroy() {
    log.info("default destory().");
  }


  /**
   * 需要登录验证返回 true
   */
  public boolean needLogin() {
    return true;
  }


  /**
   * 调用检查登录状态, 无登录则抛出异常
   */
  public void checkLoging(CallData cd) {
    if (cd.sess.login_user == null) {
      throw new XBosonException("please login", 1000);
    }
    if (cd.sess.login_user.pid == null) {
      throw new XBosonException("invaild login state", 1006);
    }
  }


  /**
   * 将本类的函数映射为子服务.
   *
   * 调用该方法, 将请求路径再次拆分, 后一级路径作为函数名, 并使用 data 调用这个函数,
   * 函数必须是 public 且函数签名和 service 一致.
   *
   * @param msg 拆分路径错误抛出的消息
   * @throws Exception
   * @throws XBosonException.NoService
   */
  protected void subService(CallData data, String msg) throws Exception {
    UrlSplit sp = data.url.clone();
    sp.setErrorMessage(msg);
    sp.withoutSlash(true);
    String sub = sp.next();

    try {
      Method sub_service = this.getClass().getMethod(sub, PARMS_TYPE);
      sub_service.invoke(this, data);

    } catch(NoSuchMethodException e) {
      throw new XBosonException.NoService(sub);

    } catch(InvocationTargetException e) {
      throw (Exception) e.getCause();
    }
  }
}
