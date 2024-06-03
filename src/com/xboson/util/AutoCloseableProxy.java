////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-14 下午5:53
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/AutoCloseableProxy.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;
import com.xboson.log.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * 代理一个自动关闭对象, 当被代理对象的 close 被调用后, 调用实现的 doClose 方法,
 * 用于数据库连接池/TCP 池等; 当对象被 GC 回收前, 没有调用过 close 方法,
 * 则使用 finalize 机制最后一次调用 doClose().
 * @param <T>
 */
public abstract class AutoCloseableProxy<T extends AutoCloseable>
        implements InvocationHandler {

  public static final String CLOSE_NAME = "close";

  private String classname;
  private T original;


  /**
   * 创建代理句柄
   * @param original 原始对象
   */
  public AutoCloseableProxy(T original) {
    if (original == null) {
      throw new XBosonException.NullParamException("original");
    }
    this.original = original;
    this.classname = original.getClass().getName();
  }


  public T getProxy() {
    Class c = original.getClass();
    Class[] interfaces = c.getInterfaces();
    interfaces = appendInterfaces(interfaces);

    Object obj = Proxy.newProxyInstance(c.getClassLoader(),
            interfaces, this);
    return (T) obj;
  }


  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
          throws Throwable {
    if (original == null) {
      throw new ClosedException(classname);
    }

    if (CLOSE_NAME.equals(method.getName()) && args  == null) {
      callClose(proxy);
      return null;
    }

    return method.invoke(original, args);
  }


  private void callClose(Object proxy) {
    try {
      if (original != null) {
        doClose(original, proxy);
      }
    } catch(Throwable e) {
      LogFactory.create().error("Call doClose() Fail:", Tool.allStack(e));
    } finally {
      original = null;
    }
  }


  @Override
  protected final void finalize() throws Throwable {
    callClose(null);
  }

  @Override
  public String toString() {
    return "Proxy="+ this.hashCode() +","+ original.toString();
  }

  /**
   * 当返回的代理类的 close() 被调用后该方法被触发,
   * 必须立即处理 original, 之后调用生成的代理对象所有方法都会抛出异常
   *
   * @param original 原始对象
   * @param proxy 生成的代理对象, 可以为 null
   * @throws Exception 如果实现抛出异常, 方法返回后对象仍然会进入关闭状态.
   */
  protected abstract void doClose(T original, Object proxy) throws Exception;


  /**
   * 附加新街口给代理类, 默认实现直接返回 interfaces.
   */
  protected Class[] appendInterfaces(Class[] interfaces) {
    return interfaces;
  }


  /**
   * 在已经关闭的对象上调用任何方法都会抛出该异常
   */
  static public class ClosedException extends XBosonException {
    public ClosedException(String classname) {
      super("Object " + classname + " is closed");
    }
  }


  /**
   * what ??
   */
  public static AutoCloseable wrap(AutoCloseable out) {
    return out;
  }
}
