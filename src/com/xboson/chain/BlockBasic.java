////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-14 上午7:45
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/BlockBasic.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.util.Tool;

import java.io.Serializable;
import java.util.Arrays;


/**
 * 由用户输入的字段
 */
public class BlockBasic implements ITypes, Serializable {

  /** 当前块数据 */
  protected byte[] data;
  /** 生成块的用户 id */
  protected String userid;
  /** 指向链码区块 key; 当 type = CHAINCODE_CONTENT 无效. */
  protected byte[] chaincodeKey;
  /** 链码 完整路径, org/app/mod/api; 当 type = CHAINCODE_CONTENT 有效. */
  protected String apiPath;
  /** 链码 hash; 当 type = CHAINCODE_CONTENT 有效. */
  protected String apiHash;
  /** 块类型 */
  public int type;


  /**
   * 用于链码块的构建
   */
  public BlockBasic(byte[] data, String userid, String apiPath, String apiHash) {
    setData(data);
    setUserid(userid);
    setApiHash(apiHash);
    setApiPath(apiPath);
    type = CHAINCODE_CONTENT;
  }


  /**
   * 用于普通数据块的构建
   */
  public BlockBasic(byte[] data, String userid, byte[] chaincodeKey) {
    setData(data);
    setUserid(userid);
    setChaincodeKey(chaincodeKey);
    type = NORM_DATA;
  }


  public BlockBasic() {
  }


  public Block createBlock() {
    Block b = new Block();
    b.setData(data);
    b.setUserid(userid);
    b.setApiHash(apiHash);
    b.setApiPath(apiPath);
    b.setChaincodeKey(chaincodeKey);
    if (type > 0) {
      b.type = type;
    } else {
      b.type = NORM_DATA;
    }
    return b;
  }


  public void setData(byte[] d) {
    if (d == null || d.length < 1)
      throw new NullPointerException();
    this.data = d;
  }


  public void setUserid(String uid) {
    if (Tool.isNulStr(uid))
      throw new NullPointerException("String uid "+uid);
    this.userid = uid;
  }


  public void setApiPath(String path) {
    this.apiPath = path;
  }


  public void setApiHash(String hash) {
    this.apiHash = hash;
  }


  public void setChaincodeKey(byte[] c) {
    this.chaincodeKey = c;
  }


  public byte[] getData() {
    return Arrays.copyOf(data, data.length);
  }


  public String getApiHash() {
    return apiHash;
  }


  public String getApiPath() {
    return apiPath;
  }


  public String getUserId() {
    return userid;
  }


  public byte[] getChaincodeKey() { return chaincodeKey; }
}
