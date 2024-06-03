////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-17 上午11:14
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/IPeer.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.been.ChainEvent;
import com.xboson.rpc.IXRemote;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.KeyPair;
import java.security.PublicKey;


/**
 * 区块链高级接口, 与区块链网络中的节点连接进行操作.
 */
public interface IPeer extends Serializable, IXRemote {

  int NEW_CHANNEL   = 1;
  int NEW_BLOCK     = 2;


  /**
   * 发送区块到链
   * @param chainName 链名
   * @param channelName 通道名, 通道必须已经存在, 否则抛出异常
   * @param b 区块数据
   * @return 新区块的主键
   */
  byte[] sendBlock(String chainName, String channelName, BlockBasic b)
          throws RemoteException;


  /**
   *
   * 创建新通道
   * @param chainName 链名, 链会自动创建
   * @param channelName 通道名, 如果通道已经存在会抛出异常
   * @param userid 用户id
   * @param consensusExp 共识表达式
   * @param keys 系统密钥对数组
   * @throws RemoteException
   */
  void createChannel(String chainName, String channelName,
                     String userid, String consensusExp,
                     KeyPair[] keys)
          throws RemoteException;


  /**
   * 查询链上的一个块, 找不到区块返回 null, 如果验证失败抛出 VerifyException.
   */
  Block search(String chainName, String channelName, byte[] key)
          throws RemoteException;


  /**
   * 返回世界状态, 如果返回空数组说明是创世区块
   */
  byte[] worldState(String chainName, String channelName)
          throws RemoteException;


  /**
   * 返回最后区块的 key, 如果返回空数组说明是创世区块
   */
  byte[] lastBlockKey(String chainName, String channelName)
          throws RemoteException;


  /**
   * 返回创世区块的 key
   */
  byte[] genesisKey(String chain, String channel)
          throws RemoteException;


  /**
   * 返回系统中所有的区块链名称
   */
  String[] allChainNames() throws RemoteException;


  /**
   * 返回链上的所有通道名字
   */
  String[] allChannelNames(String chain) throws RemoteException;


  /**
   * 返回所有区块链上的配置
   */
  ChainEvent[] allChainSetting() throws RemoteException;


  /**
   * 链/通道已经存在返回 true
   */
  boolean channelExists(String chain, String channel) throws RemoteException;


  /**
   * 返回链码块的 key, 如果不存在返回 null
   */
  byte[] getChaincodeKey(String chain, String channel, String path, String hash)
          throws RemoteException;


  /**
   * 返回区块链长度
   */
  int size(String chain, String channel) throws RemoteException;


  /**
   * 返回链的见证者公钥, 如果见证者不用于链的签名(不存在于共识表达式中) 返回 null.
   */
  PublicKey getWitnessPublicKey(String chain, String channel, String wid)
          throws RemoteException;

}
