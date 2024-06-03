////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 上午7:43
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/auth/ILoginContext.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.auth;

/**
 * 登录上下文, 将登入主体保持在程序中, 当检查权限时, 可以不提供主体对象.
 * 上下文被 AuthFactory 管理, 每个线程只有一个登录上下文.
 * 需要保证线程安全, 可重入.
 */
public interface ILoginContext {

  /**
   * 将主体登入当前上下文, 每个上下文只允许一个主体登入
   * @param who
   */
  void login(IAWho who);


  /**
   * 将主体登出线程上下文; 登出的主体必须和当前主体相同.
   * @param who
   */
  void logout(IAWho who);


  /**
   * 返回登录到当前上下文的主体, 如果没有主体登入, 则抛出安全异常
   * @return
   */
  IAWho whois();


  /**
   * 初始化上下文, 不同的实现需要传递不同的对象
   * @param contextData
   */
  void contextIn(Object contextData);


  /**
   * 切出上下文
   */
  void contextOut(Object contextData);


  /**
   * 返回上下文的名字, 作为缓冲池的 key
   */
  String contextName();
}
