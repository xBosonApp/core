////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-13 下午6:31
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/fix/state/S_Symbol.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.fix.state;

import com.xboson.app.fix.SState;


public class S_Symbol extends SState {

  protected int state = 0;
  private StringBuilder symbol;
  private int nameIndex;


  public S_Symbol(int nameIndex) {
    this.nameIndex = nameIndex;
  }


  public int read(byte ch) {
    if (state == 0 && Character.isJavaIdentifierStart(ch)) {
      state = 1;
      symbol = new StringBuilder();
      symbol.append((char)ch);
      return KEEP;
    }
    if (state == 1) {
      if (Character.isJavaIdentifierPart(ch)) {
        symbol.append((char)ch);
        return KEEP;
      } else {
        state = 0;
        data[nameIndex] = symbol.toString();
        return NEXT_AND_BACK;
      }
    }
    state = 0;
    return RESET;
  }
}
