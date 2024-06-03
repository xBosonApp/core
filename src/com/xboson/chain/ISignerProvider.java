////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-17 上午11:07
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/ISignerProvider.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import java.security.KeyPair;


public interface ISignerProvider {

  /**
   * 返回通道的签名器, 该方法会创建一个全新的签名器
   * @see com.xboson.chain.witness.ConsensusParser 共识表达式解析
   * @param chainName 链名
   * @param channelName 通道名
   * @param consensusExp 共识表达式, 可以空
   * @param kp 系统密钥对数组
   */
  ISigner getSigner(String chainName, String channelName,
                    String consensusExp, KeyPair[] kp);

}
