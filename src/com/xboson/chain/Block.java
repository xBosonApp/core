////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-13 下午12:36
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/Block.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.util.Hash;
import com.xboson.util.IBytesWriter;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.util.Date;


public class Block extends BlockBasic implements ITypes, Serializable {

  private static final Charset CS = IConstant.CHARSET;

  /** 当前块主键 */
  public byte[] key;
  /** 当前块 hash */
  public byte[] hash;
  /** 前导块 hash */
  public byte[] previousHash;
  /** 前导块主键 */
  public byte[] previousKey;
  /** 块生成时间 */
  public Date create;
  /** 数据块签名反向链表, 可以为空, 链表头是系统签名, 后面共识者签名顺序反向 */
  public SignNode sign;


  public Block() {
  }


  /**
   * 最后压入的节点是 sign 属性本身
   */
  public void pushSign(SignNode node) {
    if (sign == null) {
      sign = node;
    } else {
      SignNode tmp = sign;
      sign = node;
      sign.next = tmp;
    }
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    return super.equals(o);
  }


  public byte[] generateKey() {
    return key = Tool.uuid.getBytes(Tool.uuid.v4obj());
  }


  public void computeHash() {
    Hash h = new Hash();
    h.update(key);
    h.update(data);
    h.update(userid);
    h.update(type);
    h.update(create);

    if (chaincodeKey != null) h.update(getChaincodeKey());
    if (apiPath      != null) h.update(apiPath);
    if (apiHash      != null) h.update(apiHash);
    if (sign         != null) h.update(sign);

    if (type != GENESIS) {
      h.update(previousHash);
      h.update(previousKey);
    }
    this.hash = h.digest();
  }


  /**
   * 将区块的全部数据写出到字节输出器, 该方法通常用于生成签名的数据,
   * 根据不同的区块类型有不同的输出组合
   *
   * @param out 输出到字节输出器
   * @param keys 创世区块时必须非空, 否则抛出异常; 其他类型可以空.
   */
  public void writeTo(IBytesWriter out, KeyPair[] keys) {
    out.write(key);
    out.write(data);
    out.write(userid.getBytes(CS));
    out.write(create.getTime());

    if (type != ITypes.GENESIS) {
      out.write(previousKey);
      out.write(previousHash);
    }

    switch (type) {
      case ITypes.CHAINCODE_CONTENT:
        out.write(apiPath.getBytes(CS));
        out.write(apiHash.getBytes(CS));
        break;

      case ITypes.GENESIS:
        if (keys == null)
          throw new NullPointerException("Genesis block need KeyPair[]");

        for (int i=1; i<keys.length; ++i) {
          KeyPair kp = keys[i];
          //
          // 创世区块私钥离线后无法验证
          //
          if (i != ITypes.GENESIS) {
            out.write(kp.getPrivate().getEncoded());
          }
          out.write(kp.getPublic().getEncoded());
        }
        break;

      case ITypes.NORM_DATA:
      case ITypes.ENCRYPTION_DATA:
        out.write(chaincodeKey);
        break;
    }
  }
}
