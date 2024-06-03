////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-11 下午12:10
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/sleep/IMesmerizer.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.sleep;

/**
 * 持久化接口; 如果底层连接发生错误, 会抛出运行时异常,
 * 不能保证休眠的数据一定可以唤醒, 可能发生缓存重制或超时.
 */
public interface IMesmerizer {

  /**
   * 持久化数据, 实现需要检查 data 类型来正确处理数据,
   * 当 data 继承了 ITimeout, 超时的对象不会保存, 且被删除
   * @param data
   */
  void sleep(ISleepwalker data);

  /**
   * 唤醒持久化的数据,
   * 当  继承了 ITimeout, 唤醒后发现对象超时则返回null, 且被删除.
   * @param c
   * @param id 如果为空会抛出异常
   * @return 如果找不到 id 对应的缓存对象, 则返回 null
   */
  ISleepwalker wake(Class<? extends ISleepwalker> c, String id);

  /**
   * 删除一条数据
   * @param data
   */
  void remove(ISleepwalker data);

  /**
   * 删除该类型的所有数据
   * @param data
   */
  void removeAll(ISleepwalker data);
}
