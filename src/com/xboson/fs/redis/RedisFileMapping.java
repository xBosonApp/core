////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 下午8:29
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/ui/RedisFileMapping.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.redis;

import com.xboson.been.XBosonException;
import com.xboson.fs.basic.AbsFileSystemUtil;
import com.xboson.script.IVisitByScript;
import com.xboson.util.Tool;

import java.util.Set;


/**
 * redis 文件模式.
 *
 * 读取: 从 redis 缓存中读取文件, 如果文件不存在则返回 404, 即使本地文件存在.
 * 写入: 判断 redis 中保存的文件修改时间, 条件允许则保存文件; 写入结束后,
 *      将修改记录加入消息队列
 */
public abstract class RedisFileMapping extends AbsFileSystemUtil<RedisFileAttr>
        implements IRedisFileSystemProvider, IVisitByScript {

  public static final int ID = 1;

  private RedisBase rb;


  public RedisFileMapping(RedisBase rb) {
    this.rb  = rb;
  }


  @Override
  public byte[] readFile(String path) {
    RedisFileAttr fs = readAttribute(path);

    if (fs == null)
      throw new XBosonException.NotFound(path);

    readFileContent(fs);
    return fs.getFileContent();
  }


  @Override
  public void readFileContent(RedisFileAttr fs) {
    if (fs.isDir())
      throw new XBosonException.ISDirectory(fs.path);

    rb.getContent(fs);
  }


  @Override
  public long modifyTime(String path) {
    return rb.getStruct(path).lastModify;
  }


  @Override
  public RedisFileAttr readAttribute(String path) {
    return rb.getStruct(normalize(path));
  }


  @Override
  public void makeDir(String path) {
    makeDir(path, true);
  }


  public void makeDir(String path, boolean notice) {
    try (RedisBase.JedisSession jsession = rb.openSession()) {
      path = normalize(path);
      RedisFileAttr dir = rb.getStruct(path);

      if (dir == null) {
        dir = RedisFileAttr.createDir(path);
        makeDir(dir, notice);
      }
    }
  }


  public void makeDir(RedisFileAttr dir, boolean notice) {
    try (RedisBase.JedisSession jsession = rb.openSession()) {
      makeStructUntilRoot(dir);
      if (notice) {
        rb.sendCreateDirNotice(dir.path);
      }
    }
  }


  @Override
  public void writeFile(String path, byte[] bytes) {
    writeFile(path, bytes, System.currentTimeMillis(), true);
  }


  public void writeFile(String path, byte[] bytes, long modify, boolean notice) {
    try (RedisBase.JedisSession jsession = rb.openSession()) {
      path = normalize(path);
      RedisFileAttr file = RedisFileAttr.createFile(path, modify, bytes);
      writeFile(file, notice);
    }
  }


  public void writeFile(RedisFileAttr file, boolean notice) {
    try (RedisBase.JedisSession jsession = rb.openSession()) {
      makeStructUntilRoot(file);
      rb.setContent(file);

      if (notice) {
        rb.sendModifyNotice(file.path);
      }
    }
  }


  protected void removeAndUpdateParent(RedisFileAttr fs) {
    rb.removeStruct(fs);
    if (fs.isFile()) {
      rb.delContent(fs);
    }
    String parentPath = fs.parentPath();
    if (parentPath != null) {
      RedisFileAttr parent = rb.getStruct(parentPath);
      parent.removeChild(fs.path);
      rb.saveStruct(parent);
    }
  }


  @Override
  public void delete(String file) {
    delete(file, true);
  }


  public void delete(String file, boolean notice) {
    try (RedisBase.JedisSession jsession = rb.openSession()) {
      deleteFile(file);

      if (notice) {
        rb.sendDeleteNotice(file);
      }
    }
  }


  @Override
  public void move(String src, String to) {
    move(src, to, true);
  }


  public void move(String src, String to, boolean notice) {
    try (RedisBase.JedisSession jsession = rb.openSession()) {
      super.moveTo(src, to);

      if (notice) {
        rb.sendMoveNotice(src, to);
      }
    }
  }


  @Override
  public Set<RedisFileAttr> readDir(String path) {
    try (RedisBase.JedisSession jsession = rb.openSession()) {
      return readDirContain(path);
    }
  }


  @Override
  public FinderResult findPath(String pathName) {
    return rb.findPath(pathName);
  }


  @Override
  public FinderResult findContent(String basePath, String content, boolean cs) {
    return rb.findContent(basePath, content, cs);
  }


  @Override
  protected RedisFileAttr createDirNode(String path) {
    return RedisFileAttr.createDir(path);
  }


  @Override
  protected void addTo(RedisFileAttr dir, String path) {
    dir.addChildStruct(path);
  }


  @Override
  protected void saveNode(RedisFileAttr a) {
    rb.saveStruct(a);
  }


  @Override
  protected void moveFile(RedisFileAttr src, String to) {
    rb.getContent(src);
    RedisFileAttr tofs = src.cloneWithName(to);
    writeFile(tofs, false);
    deleteFile(src.path);
  }


  @Override
  protected Set<String> containFiles(RedisFileAttr dir) {
    return dir.containFiles();
  }


  @Override
  protected RedisFileAttr cloneBaseName(RedisFileAttr a) {
    return a.cloneBaseName();
  }
}
