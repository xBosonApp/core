////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-8 上午8:22
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/Page.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import com.xboson.script.IVisitByScript;


public class Page implements IVisitByScript {

  public final static int PAGE_DEFAULT_COUNT = -1;


  /** 从 0 开始的页码 */
  public int pageNum;
  /** 一页行数 */
  public int pageSize;
  /** 总行数 */
  public int totalCount;
  /** 从 0 开始的偏移 */
  public int offset;


  /**
   * 分页数据
   * @param pageNum 从 1 开始的页码
   * @param pageSize
   * @param totalCount
   */
  public Page(int pageNum, int pageSize, int totalCount) {
    if (pageNum < 1)
      throw new XBosonException.BadParameter(
              "int pageNum", "Should be greater than 0");

    if (pageSize < 1)
      throw new XBosonException.BadParameter(
              "int pageSize", "Should be greater than 0");

    if (totalCount > 0) {
      this.totalCount = totalCount;
    } else {
      this.totalCount = -1;
    }

    this.pageNum = pageNum - 1;
    this.pageSize = pageSize;
    this.offset = this.pageNum * this.pageSize;
  }
}
