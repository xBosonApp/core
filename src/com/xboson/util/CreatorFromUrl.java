////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-10-20 上午11:57
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/CreatorFromUrl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;

import java.util.HashMap;
import java.util.Map;


/**
 * 使用 url 来创建对象, url 格式: `协议://值`
 * 首先需要注册协议的实现.
 */
public class CreatorFromUrl<T> {

  private Map<String, IProtocol<T>> ps;
  private IProtocol<T> defaultp;


  public CreatorFromUrl() {
    ps = new HashMap<>();
  }


  /**
   * 注册协议及实现, 该方法线程不安全
   * @param isDefault 注册为默认协议, 当 `协议://` 部分不存在时生效
   * @param protocol 协议名称
   * @param p 协议对应的构造方法
   */
  public void reg(boolean isDefault, String protocol, IProtocol<T> p) {
    if (Tool.isNulStr(protocol))
      throw new NullPointerException("Protocol name is null");
    if (p == null)
      throw new NullPointerException("Protocol implement is null");

    ps.put(protocol, p);

    if (isDefault) {
      defaultp = p;
    }
  }


  /**
   * 以非默认协议注册协议
   * @see #reg(boolean, String, IProtocol)
   */
  public void reg(String protocol, IProtocol<T> p) {
    reg(false, protocol, p);
  }


  /**
   * 从 url 创建对象, 线程安全, 不允许 url 为空值;
   * 错误会抛出异常.
   * @see #create(String, Object)
   */
  public T create(String url) {
    return create(url, null);
  }


  /**
   * 从 url 创建对象, 线程安全, 不允许 url 为空值; data 可以为空, 用于附加参数.
   * 错误会抛出异常.
   */
  public T create(String url, Object data) {
    if (Tool.isNulStr(url))
      throw new XBosonException.NullParamException("URL");

    try {
      int i = url.indexOf("://");
      if (i < 0) {
        if (defaultp == null) {
          throw new XBosonException("Invaild format from URL: "+ url);
        }
        return defaultp.create(url, null, url, data);
      }

      String v = url.substring(i+3);
      String p = url.substring(0, i);
      IProtocol<T> ip = ps.get(p);
      if (ip == null) {
        throw new XBosonException("Invaild protocol from URL: "+ url);
      }

      return ip.create(v, p, url, data);
    } catch (XBosonException e) {
      throw e;
    } catch (Exception e) {
      throw new XBosonException(e);
    }
  }


  public interface IProtocol<N> {


    /**
     * 创建对象的实例, 同时附带一些数据
     *
     * @see #create(String, String, String) 默认实现调用该方法
     * @param val 解析后 url 中的值部分
     * @param protocol 解析后 url 中协议部分, 不包含 `://`
     * @param url 完整的 url 字符串
     * @param data 附加数据
     * @return 根据 url 创建的对象
     */
    N create(String val, String protocol, String url, Object data)
            throws Exception;
  }
}
