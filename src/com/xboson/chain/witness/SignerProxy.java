////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-30 下午7:56
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/witness/SignerProxy.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain.witness;

import com.xboson.been.XBosonException;
import com.xboson.chain.Block;
import com.xboson.chain.SignNode;
import com.xboson.chain.VerifyException;
import com.xboson.event.EventLoop;
import com.xboson.log.Log;
import com.xboson.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Map;
import java.util.Set;


/**
 * 便捷类, 远程签名者代理, 与一个远程见证者连接;
 * 推荐使用静态方法.
 */
public class SignerProxy {

  public static final String SIGNER_ALGORITHM = "SHA256withECDSA";

  private WitnessConnect wit;
  private PublicKey pk;


  public SignerProxy(PublicKey key, WitnessConnect wit) {
    if (key == null)
      throw new XBosonException.NullParamException("PublicKey");
    if (wit == null)
      throw new XBosonException.NullParamException("WitnessConnect");

    this.pk  = key;
    this.wit = wit;
  }


  /**
   * @see #sign(WitnessConnect, byte[])
   */
  public byte[] sign(byte[] data) throws IOException {
    return sign(wit, data);
  }


  public byte[] sign(InputStream i) throws IOException {
    return sign(wit, i);
  }


  public byte[] sign(Pipe.Context pc) {
    return sign(wit, pc);
  }


  /**
   * 如果没有用带有 PublicKey 参数的构造函数, 该方法不可用
   */
  public boolean verify(byte[] data, byte[] sign) {
    return verify(pk, data, sign);
  }


  /**
   * 使用公钥验证签名正确性
   * @param data 原始数据
   * @param sign 数据的签名
   * @return 验证正确返回 true, 任何加密算法错误都会抛出异常.
   */
  public static boolean verify(PublicKey pk, byte[] data, byte[] sign) {
    try {
      Signature si = Signature.getInstance(SIGNER_ALGORITHM);
      si.initVerify(pk);
      si.update(data);
      return si.verify(sign);
    } catch (Exception e) {
      throw new VerifyException(e.getMessage());
    }
  }


  /**
   * 使用远程节点(私钥)签名数据
   *
   * @param data 待签名数据
   * @return 数据的签名
   * @throws IOException 连接远程节点失败
   */
  public static byte[] sign(WitnessConnect wit, byte[] data) throws IOException {
    return wit.doSign(data);
  }


  public static byte[] sign(WitnessConnect wit, InputStream i) throws IOException {
    return wit.doSign(new StreamRequestBody(i, WitnessConnect.BINARY));
  }


  public static byte[] sign(WitnessConnect wit, Pipe.Context pc) {
    try {
      Pipe p = new Pipe(pc);
      return sign(wit, p.openInputStream());
    } catch (IOException io) {
      throw new XBosonException.IOError(io);
    }
  }


  /**
   * 将区块交付给 witnessIdSet 中所有见证者, 该任务在单独的线程中执行;
   * 可以保证区块的顺序, 但不保证区块一定可以递交给见证者, 如网络错误时.
   */
  public static void deliver(Set<String> witnessIdSet, Block b,
                             String chain, String channel) {
    String json = Tool.getAdapter(Block.class).toJson(b);

    EventLoop.me().add(() -> {
      WitnessFactory wf = WitnessFactory.me();
      Log log = wf.getWitnessLog();

      for (String wid : witnessIdSet) {
        if (wf.isSkipDeliver(wid))
          continue;

        WitnessConnect wc = WitnessFactory.me().openConnection(wid);
        if (! wc.doDeliver(json, chain, channel)) {
          log.warn("Skip Deliver Witness:", wid);
          wf.setSkipDeliver(wid);
        }
      }
    });
  }


  /**
   * 使用本地保存的见证者公钥验证数据块
   */
  public static boolean consensusLocalVerify(
          Block b, Map<String, PublicKey> usedKeys, KeyPair[] keys) {
    // 第一个签名是系统签名, 需要忽略
    SignNode node = b.sign.next;

    StringBufferOutputStream buf = new StringBufferOutputStream();
    b.writeTo(IBytesWriter.wrap(buf), keys);
    byte[] data = buf.toBytes();

    while (node != null) {
      PublicKey key = usedKeys.get(node.id);
      if (key == null) {
        throw new VerifyException("Not found public key "+ node.id);
      }
      if (! verify(key, data, node.sign)) {
        return false;
      }
      node = node.next;
    }
    return true;
  }


  /**
   * 创建一个见证者上下文, 用于签名区块
   */
  public static IConsensusContext openConsensusSign(KeyPair[] keys) {
    return (String witnessId, PublicKey key, Block b) -> {
      WitnessConnect conn = WitnessFactory.me().openConnection(witnessId);

      byte[] signBytes = sign(conn, (OutputStream out) -> {
        try {
          b.writeTo(IBytesWriter.wrap(out), keys);
        } finally {
          Tool.close(out);
        }
      });

      b.pushSign(new SignNode(signBytes, witnessId));
      return true;
    };
  }

}
