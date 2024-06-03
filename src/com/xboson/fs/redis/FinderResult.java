////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-19 上午9:56
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/ui/FinderResult.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.redis;

import com.xboson.util.JavaConverter;

import java.util.Collections;
import java.util.List;


/**
 * 内容查询结果集, 数据不可变.
 *
 * 结果集通常有查询结果数量限制, 当到达该限制多出的数据被忽略, 并且没有方法将忽略的
 * 数据再次查询出来, 唯一的方法就是增加查询表达式的复杂度来收缩结果集数量, 这样的设
 * 计符合前端对于大量数据无意义的策略.
 */
public class FinderResult {

  /** 包含搜索内容的文件名列表(完整路径) */
  public final List<String> files;

  /** redis 中的键名 */
  public final String baseDir;

  /** 搜索内容字符串, 可能已经被重新为匹配表达式 */
  public final String find;

  /** 开启大小写敏感 */
  public final boolean caseSensitive;

  /** 还有更多结果没有返回 (需要增加搜索内容减小搜索范围) */
  public final boolean hasMore;


  /**
   * 来自 lua 结果集构造一个查询结果对象
   * @param arr lua 中的 list.
   */
  public FinderResult(Object arr) {
    List info     = (List) arr;
    files         = Collections.unmodifiableList((List) info.get(0));
    baseDir       = (String) info.get(1);
    find          = (String) info.get(2);
    caseSensitive = JavaConverter.toBool(info.get(3));
    hasMore       = JavaConverter.toBool(info.get(4));
  }


  public FinderResult(List<String> files, String baseDir,
                      String find, boolean caseSensitive, boolean hasMore) {
    this.files          = files;
    this.baseDir        = baseDir;
    this.find           = find;
    this.caseSensitive  = caseSensitive;
    this.hasMore        = hasMore;
  }


  @Override
  public String toString() {
    return "[ Find: '"+ find +"' "+
            (caseSensitive ? "Case sensitive" : "Case insensitive") +
            ", On '"+ baseDir +"', "+ files.size() +" files, "+
            (hasMore ? "And more files" : "No more") + " ]";
  }
}
