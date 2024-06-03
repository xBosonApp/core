////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-13 下午6:31
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/fix/ISState.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.fix;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


/**
 * 前向状态机接口
 */
public interface ISState extends IStateOperator {

  /**
   * 读取一个字符, 返回操作码
   * @see IStateOperator 操作码枚举接口
   */
  int read(byte ch) throws IOException;


  /**
   * 状态机处理好的中间数据, 压入 strarr 中
   * @param strarr
   */
  void setData(String[] strarr);


  /**
   * 状态机将最终代码输出到 out.
   * @param out
   */
  void setOutput(OutputStream out);

}
