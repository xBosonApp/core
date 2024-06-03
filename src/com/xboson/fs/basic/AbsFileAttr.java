////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-3 下午12:57
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/basic/AbsFileAttr.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.basic;

import com.xboson.been.XBosonException;
import com.xboson.fs.redis.RedisFileAttr;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * 预定义文件属性, 只有三个属性: 路径, 类型, 修改时间
 */
public abstract class AbsFileAttr implements Serializable, IFileAttribute {


  /** 规范化的绝对路径 */
  public final String path;

  /** 文件类型 */
  public final int type;

  /** 修改时间, ms */
  public final long lastModify;

  /** 若是目录, 则存储目录中的元素 */
  public final Set<String> dir_contain;


  protected AbsFileAttr(String path, int type, long lastModify) {
    if (path == null)
      throw new XBosonException.NullParamException("String path");

    this.path = path;
    this.type = type;
    this.lastModify = lastModify;

    if (type == T_DIR) {
      dir_contain = new HashSet<>();
    } else {
      dir_contain = null;
    }
  }


  protected AbsFileAttr(AbsFileAttr other) {
    this(other.path, other.type, other.lastModify);
    if (dir_contain != null) {
      dir_contain.addAll(other.dir_contain);
    }
  }


  /**
   * 将文件或目录加入当前目录中作为子节点,
   * 保存时将去掉当前目录前缀, 只保留子节点名称 (有前置 '/')
   * @param fileOrDir 完整路径
   */
  public void addChildStruct(String fileOrDir) {
    String ch = childPath(fileOrDir);
    dir_contain.add(ch);
  }


  /**
   * 删除当前目录中的子节点
   * @param fileOrDir 完整路径
   */
  public void removeChild(String fileOrDir) {
    String ch = childPath(fileOrDir);
    if (! dir_contain.remove(ch)) {
      throw new BadPath("Not in this dir:" + fileOrDir);
    }
  }


  /**
   * 返回所有子节点路径列表, 路径中不包含当前目录的路径.
   */
  public Set<String> containFiles() {
    if (type != T_DIR) {
      throw new BadPath("Is not directory");
    }
    return dir_contain;
  }


  /**
   * 当前对象必须是目录, full 必须是当前目录子节点路径,
   * 返回去掉当前路径的子路径字符串. 当 full 不符合条件时抛出异常.
   */
  protected String childPath(String full) {
    if (type != T_DIR) {
      throw new BadPath("Is not directory");
    }
    if (! full.startsWith(path)) {
      throw new BadPath("Is not child: '" + full + "'");
    }
    return full.substring(path.length());
  }


  public boolean isDir() {
    return type == T_DIR;
  }


  public boolean isFile() {
    return type == T_FILE;
  }


  @Override
  public int type() {
    return type;
  }


  @Override
  public final String toString() {
    return "[" + path +", "+ (type == T_DIR ? "DIR":"FILE") + "]";
  }


  @Override
  public final int hashCode() {
    return (int)(path.hashCode() + type + lastModify);
  }


  @Override
  public final boolean equals(Object o) {
    if (o == this)
      return true;

    if (o == null || o instanceof RedisFileAttr == false)
      return false;

    AbsFileAttr other = (AbsFileAttr) o;
    return other.path.equals(path)
            && other.type == type
            && other.lastModify == lastModify;
  }


  @Override
  public String path() {
    return path;
  }


  /**
   * 当需要一个目录而给出一个文件, 或相反, 或路径格式错误, 抛出的异常.
   */
  public class BadPath extends XBosonException.IOError {
    public BadPath(String why) {
      super(why, path);
    }
  }
}
