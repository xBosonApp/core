////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-12 上午10:12
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/witness/IConsensusUnit.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain.witness;

import com.xboson.chain.Block;
import com.xboson.db.analyze.ParseException;

import java.io.Serializable;


/**
 * 共识表达式解析后的单元, 该对象需要能序列化, 不要存储复杂的对象.
 */
public interface IConsensusUnit extends Serializable {


  /**
   * 添加子表达式
   */
  void addAction(IConsensusUnit subAct);


  /**
   * 执行当前单元, 如果返回 false 或抛出异常说明执行失败
   */
  boolean doAction(IConsensusContext d, Block b) throws ParseException;


  /**
   * 检查当前单元是否有效, 无效的单元配置将抛出异常, 默认什么都不做
   */
  default void check() throws ParseException {}

}
