////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-19 下午12:12
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/ChainSignerProvider.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.been.Witness;
import com.xboson.been.XBosonException;
import com.xboson.chain.witness.*;
import com.xboson.util.Hex;
import com.xboson.util.IBytesWriter;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Map;


/**
 * 区块链签名提供商, 使用平台数据库做签名架构.
 * @see com.xboson.chain.Btc 公钥/私钥生成算法
 * @see IConstant#CHAIN_SIGNER_PROVIDER 全局区块签名提供商配置
 */
public class ChainSignerProvider implements
        ISignerProvider, IConsensusPubKeyProvider {


  public ChainSignerProvider() {
  }


  @Override
  public ISigner getSigner(String chainName, String channelName,
                           String exp, KeyPair[] kp) {
    if (Tool.isNulStr(chainName)) throw new XBosonException.BadParameter(
              "String chainName", "is null");
    if (Tool.isNulStr(channelName)) throw new XBosonException.BadParameter(
              "String channelName", "is null");

    ConsensusParser cp = new ConsensusParser(this);
    return new Signer(chainName, channelName, kp,
                      cp.parse(exp), cp.getUsedPublicKeys(), exp);
  }


  @Override
  public PublicKey getKey(String witness_id) {
    Witness wit = WitnessFactory.me().get(witness_id);
    return Btc.publicKey(Hex.Names.BASE64, wit.publicKeyStr);
  }


  public static class Signer implements ISigner {
    private IConsensusUnit consensus;
    private KeyPair[] keys;
    private String signer_algorithm;
    private String chain;
    private String channel;
    private String consensusExp;
    private Map<String, PublicKey> usedKeys;


    private Signer(String chainName, String channelName,
                   KeyPair[] keys, IConsensusUnit consensus,
                   Map<String, PublicKey> usedKeys, String consensusExp) {
      this.signer_algorithm = SignerProxy.SIGNER_ALGORITHM;
      this.chain            = chainName;
      this.channel          = channelName;
      this.keys             = keys;
      this.consensus        = consensus;
      this.usedKeys         = usedKeys;
      this.consensusExp     = consensusExp;
    }


    @Override
    public void sign(Block block) {
      try {
        consensus.doAction(SignerProxy.openConsensusSign(keys), block);
        Signature si = Signature.getInstance(signer_algorithm);
        KeyPair pair = getKeyPair(block.type);
        si.initSign(pair.getPrivate());
        block.writeTo(IBytesWriter.wrap(si), keys);
        block.pushSign(new SignNode(si.sign(), block.type));
      } catch (Exception e) {
        throw new XBosonException(
                "Witness signature block fail, "+ e.getMessage(), e);
      }
    }


    @Override
    public boolean verify(Block block) {
      try {
        Signature si = Signature.getInstance(signer_algorithm);
        KeyPair pair = getKeyPair(block.type);
        si.initVerify(pair.getPublic());
        block.writeTo(IBytesWriter.wrap(si), keys);
        if (si.verify(block.sign.sign)) {
          return SignerProxy.consensusLocalVerify(block, usedKeys, keys);
        }
        return false;
      } catch (Exception e) {
        throw new XBosonException("Verify signature fail, "+ e.getMessage(), e);
      }
    }


    @Override
    public void deliver(Block block) {
      SignerProxy.deliver(usedKeys.keySet(), block, chain, channel);
    }


    @Override
    public PublicKey getWitnessPublicKey(String wid) {
      return usedKeys.get(wid);
    }


    public void removeGenesisPrivateKey() {
      KeyPair g = keys[ITypes.GENESIS];
      KeyPair newg = new KeyPair(g.getPublic(), null);
      keys[ITypes.GENESIS] = newg;
    }


    private KeyPair getKeyPair(int i) {
      KeyPair pair = keys[i];
      if (pair == null) {
        throw new NullPointerException("cannot found KeyPair index:"+ i);
      }
      return pair;
    }


    @Override
    public String getConsensusExp() {
      return consensusExp;
    }


    @Override
    public KeyPair[] getKeyPairs() {
      return keys;
    }
  }
}
