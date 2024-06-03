////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-17 下午12:52
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/ui/RedisFileAttr.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.redis;

import com.xboson.fs.basic.AbsFileAttr;
import com.xboson.script.lib.Path;
import com.xboson.util.c0nst.IConstant;

import java.io.Serializable;


/**
 * 虽然也是文件属性类, 但是为 UI 存储优化.
 * file_content 可以能为 null.
 */
public class RedisFileAttr extends AbsFileAttr implements Serializable {

  private transient byte[] file_content;
  private transient boolean need_synchronization;


  private RedisFileAttr(String path, int type, long lastModify) {
    super(path, type, lastModify);
  }


  protected RedisFileAttr(RedisFileAttr fs) {
    super(fs);
    this.file_content = fs.file_content;
    this.need_synchronization = fs.need_synchronization;
  }


  public static RedisFileAttr createFile(String path,
                                         long lastModify,
                                         byte[] content)
  {
    RedisFileAttr fs = new RedisFileAttr(path, T_FILE, lastModify);
    fs.file_content = content;
    return fs;
  }


  public static RedisFileAttr createDir(String path) {
    RedisFileAttr fs = new RedisFileAttr(path, T_DIR, 0);
    return fs;
  }


  public byte[] getFileContent() {
    if (type != T_FILE) {
      throw new BadPath("Is not file");
    }
    return file_content;
  }


  public String getContentToString() {
    return new String(file_content, IConstant.CHARSET);
  }


  public void setFileContent(byte[] c) {
    file_content = c;
  }


  /**
   * 返回父路径字符串, 如果已经是父路径返回 null
   */
  public String parentPath() {
    return Path.me.dirname(path);
  }


  /**
   * 复制所有属性, 除了 path 只保留文件名部分,
   * 返回的对象中, 内容属性将指向同一个可变对象.
   */
  public final RedisFileAttr cloneBaseName() {
    String basename = Path.me.basename(path);
    RedisFileAttr fs = new RedisFileAttr(basename, type, lastModify);
    if (dir_contain != null) fs.dir_contain.addAll(dir_contain);
    fs.file_content = file_content;
    fs.need_synchronization = need_synchronization;
    return fs;
  }


  /**
   * 克隆所有属性除了 path 使用 newPath
   */
  public final RedisFileAttr cloneWithName(String newPath) {
    RedisFileAttr fs = new RedisFileAttr(newPath, type, lastModify);
    if (dir_contain != null) fs.dir_contain.addAll(dir_contain);
    fs.file_content = file_content;
    fs.need_synchronization = need_synchronization;
    return fs;
  }


  /**
   * 返回创建该对象的 IRedisFileSystemProvider 实例 id 值, 可由子类重写
   */
  public int mappingID() {
    return RedisFileMapping.ID;
  }


  public boolean needSynchronization() {
    return need_synchronization;
  }


  public void setSynchronization(boolean need) {
    need_synchronization = need;
  }
}
