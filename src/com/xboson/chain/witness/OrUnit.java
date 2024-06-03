////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-12 上午10:17
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/witness/OrUnit.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain.witness;


import com.xboson.chain.Block;
import com.xboson.db.analyze.ParseException;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;


public class OrUnit implements IConsensusUnit {
  private TwiceUnit root;
  private transient TwiceUnit next;
  private transient Log _log;


  public OrUnit() {
    next = root = new TwiceUnit();
  }


  @Override
  public void addAction(IConsensusUnit subAct) {
    next.current = subAct;
    next.next = new TwiceUnit();
    next = next.next;
  }


  @Override
  public boolean doAction(IConsensusContext d, Block b) {
    TwiceUnit next = root;

    while (next.current != null) {
      try {
        if (next.current.doAction(d, b)) {
          return true;
        } else {
          next = next.next;
        }
      } catch (Exception e) {
        openLog().warn(e);
        next = next.next;
      }
    }
    return false;
  }


  private Log openLog() {
    if (_log == null) {
      _log = LogFactory.create("consesus-or");
    }
    return _log;
  }


  /**
   * 至少有一个参数, 否则抛出异常
   */
  @Override
  public void check() throws ParseException {
    if (root.current == null) {
      throw new ParseException("No parameter");
    }
  }

}
