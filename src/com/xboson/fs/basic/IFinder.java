////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-4 上午9:33
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/basic/IFinder.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.basic;

/**
 * 文件搜索操作接口
 * @param <Result> 搜索结果集
 */
public interface IFinder<Result> {

  /**
   * 模糊查询符合路径的完整路径集合, 总是大小写敏感的, 自行添加匹配模式.
   */
  Result findPath(String pathName);


  /**
   * 查询文件内容, 返回文件列表
   *
   * @param basePath 开始目录
   * @param content 要搜索的文本
   * @param cs true 则启用大小写敏感
   */
  Result findContent(String basePath, String content, boolean cs);

}
