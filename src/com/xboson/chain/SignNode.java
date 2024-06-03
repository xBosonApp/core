////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-13 下午4:28
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/SignNode.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import java.io.Serializable;


/**
 * 签名链表, 使用 Map 或 List 在持久化时占用内存,
 * 随着 java 版本的升高 Map/List 可能二进制不兼容.
 */
public class SignNode implements Serializable {

  /** 内部签名使用块类型作为 id, 共识者签名使用共识者 id */
  public final String id;

  /** 签名, 签名本身不包含其他签名 */
  public final byte[] sign;

  /** 如果没有下一节点则为 null */
  public SignNode next;


  public SignNode(byte[] sign, String id) {
    this.sign = sign;
    this.id = id;
  }


  public SignNode(byte[] sign, int id) {
    this(sign, ""+id);
  }

}
