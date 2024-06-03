////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-13 下午3:12
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/ScriptAttr.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.script;

import com.xboson.been.JsonHelper;
import com.xboson.fs.basic.IFileAttribute;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * 文件属性对象
 */
public class ScriptAttr extends JsonHelper implements
        Serializable, IFileAttribute {

  /** 只有文件名本身 */
  public String fileName;
  public long createTime;
  public long modifyTime;
  public String creatorUID;
  /** 只包含路径 */
  public String pathName;
  /** 路径 + 文件名 */
  public String fullPath;
  /** 文件的字节大小 */
  public long fileSize;


  public ScriptAttr() {}


  public ScriptAttr(BasicFileAttributes basic, Path fullpath) {
    this.fileName   = fullpath.getFileName().toString();
    this.createTime = basic.creationTime().toMillis();
    this.modifyTime = basic.lastModifiedTime().toMillis();
    this.creatorUID = null;
    this.pathName   = fullpath.getParent().toString();
    this.fullPath   = fullpath.toString();
    this.fileSize   = basic.size();
  }


  /**
   * 脚本文件系统没有目录
   */
  @Override
  public int type() {
    return T_FILE;
  }


  @Override
  public String path() {
    return fullPath;
  }
}
