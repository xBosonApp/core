////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-19 下午2:52
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/FsImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.been.XBosonException;
import com.xboson.fs.mongo.IMongoFileSystem;
import com.xboson.fs.mongo.MongoFileAttr;
import com.xboson.fs.mongo.SysMongoFactory;
import com.xboson.fs.node.NodeFileFactory;
import com.xboson.fs.redis.RedisFileAttr;
import com.xboson.fs.redis.FinderResult;
import com.xboson.fs.redis.IRedisFileSystemProvider;
import com.xboson.fs.ui.UIFileFactory;
import com.xboson.script.lib.Checker;
import com.xboson.util.Tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;


public class FsImpl {


  public Object open() {
    return open("ui");
  }


  public Object open(String fsTypeName) {
    boolean runOnSysOrg = (boolean) ModuleHandleContext._get("runOnSysOrg");

    if (!runOnSysOrg)
      throw new XBosonException.NotImplements("只能在平台机构中引用");

    if (fsTypeName == null)
      throw new XBosonException.NullParamException("String fsTypeName");


    switch (fsTypeName) {
      case "ui":
        return new Wrap(UIFileFactory.open());

      case "node":
        return new Wrap(NodeFileFactory.open());

      case "share":
        return new Wrap2(SysMongoFactory.me().openFS());

      default:
        throw new XBosonException.NotFound(
                "File System Type:" + fsTypeName);
    }
  }


  public Object openShare(String diskName) {
    if (Tool.isNulStr(diskName))
      throw new XBosonException.NullParamException("String diskName");

    Checker.me.symbol(diskName, "无效的磁盘名称格式");

    return new Wrap2(SysMongoFactory.me().openFS(diskName));
  }


  public void pipe(InputStream i, OutputStream o) throws IOException {
    Tool.copy(i, o, true);
  }


  private class Wrap2 implements IMongoFileSystem {
    private IMongoFileSystem real;

    private Wrap2(IMongoFileSystem real) {
      this.real = real;
    }


    @Override
    public InputStream openInputStream(String file) {
      InputStream i = real.openInputStream(file);
      ModuleHandleContext.autoClose(i);
      return i;
    }


    @Override
    public OutputStream openOutputStream(String file) {
      OutputStream o = real.openOutputStream(file);
      ModuleHandleContext.autoClose(o);
      return o;
    }


    @Override
    public MongoFileAttr readAttribute(String path) {
      return real.readAttribute(path);
    }


    @Override
    public Set<MongoFileAttr> readDir(String path) {
      return real.readDir(path);
    }


    @Override
    public void move(String src, String to) {
      real.move(src, to);
    }


    @Override
    public void delete(String file) {
      real.delete(file);
    }


    @Override
    public long modifyTime(String path) {
      return real.modifyTime(path);
    }


    @Override
    public void makeDir(String path) {
      real.makeDir(path);
    }
  }


  /**
   * 包装器防止调用不在接口中的方法
   */
  private class Wrap implements IRedisFileSystemProvider {
    private final IRedisFileSystemProvider o;

    private Wrap(IRedisFileSystemProvider o ) {
      this.o = o;
    }

    @Override
    public byte[] readFile(String path) {
      return o.readFile(path);
    }


    @Override
    public void readFileContent(RedisFileAttr fs) {
      o.readFileContent(fs);
    }


    @Override
    public long modifyTime(String path) {
      return o.modifyTime(path);
    }


    @Override
    public RedisFileAttr readAttribute(String path) {
      return o.readAttribute(path);
    }


    @Override
    public void makeDir(String path) {
      o.makeDir(path);
    }


    @Override
    public void writeFile(String path, byte[] bytes) {
      o.writeFile(path, bytes);
    }


    @Override
    public void delete(String file) {
      o.delete(file);
    }


    @Override
    public void move(String src, String to) {
      o.move(src, to);
    }


    @Override
    public Set<RedisFileAttr> readDir(String path) {
      return o.readDir(path);
    }


    @Override
    public FinderResult findPath(String pathName) {
      return o.findPath(pathName);
    }


    @Override
    public FinderResult findContent(String basePath, String content, boolean cs) {
      return o.findContent(basePath, content, cs);
    }
  }
}
