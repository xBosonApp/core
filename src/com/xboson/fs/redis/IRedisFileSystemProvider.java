////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 下午8:29
// 原始文件路径: D:/javaee-project/xBoson/src/com/fs/ui/IRedisFileSystemProvider.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.redis;

import com.xboson.been.XBosonException;
import com.xboson.fs.basic.IBlockOperator;
import com.xboson.fs.basic.IFinder;


/**
 * 对 ui 文件的操作, 接口尽可能简单, 每个属性都是分离的.
 * 所有的路径参数已经规范化, 不包含任何 "/./" 和 "/../", 并且使用 unix 分隔符,
 * 路径为 ui 虚拟目录, 跟目录应该包含 'ui' 't' 'web' 'lib' 等目录
 */
public interface IRedisFileSystemProvider extends
        IBlockOperator<RedisFileAttr>, IFinder<FinderResult> {


  /** 结果集最大数量, 超过后的数据被忽略 */
  int MAX_RESULT_COUNT = 30;


  /**
   * 读取文件内容, 目录会抛出异常
   * @param fs
   * @throws XBosonException.IOError
   */
  void readFileContent(RedisFileAttr fs);

}
