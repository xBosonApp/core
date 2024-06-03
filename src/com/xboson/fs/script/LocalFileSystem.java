////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-13 下午3:08
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/LocalFileSystem.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.script;

import com.xboson.app.fix.SourceFix;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.script.IVisitByScript;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * 将测试目录文件映射到虚拟目录
 */
public class LocalFileSystem extends FSHelper
        implements IScriptFileSystem, IVisitByScript {

  /** <virtual_filename, code> */
  private final Map<String, ByteBuffer> file_cache;
  private final String id;
  private final String basedir;
  private final Log log;
  private final FileSystem fs;


  public LocalFileSystem(String basedir, String id) {
    this.id = id;
    this.fs = FileSystems.getDefault();
    this.basedir = basedir;
    this.file_cache = new HashMap<>();
    this.log = LogFactory.create();
  }


  /**
   * 设定静态文件内容
   */
  public void putcode(String name, String code) {
    Path p = Paths.get(name);
    ByteBuffer buf = ByteBuffer.wrap(code.getBytes());
    file_cache.put(p.toString(), buf);
  }


  /**
   * 将虚拟路径映射的真实路径 (在 basedir 的基础上)
   */
  public ByteBuffer putfile(String virtual_name, String file) throws IOException {
    Path p = Paths.get(basedir, file);
    if (! Files.exists(p)) return null;
    byte[] content = Files.readAllBytes(p);
    content = SourceFix.autoPatch(content);
    ByteBuffer buf = ByteBuffer.wrap(content);
    file_cache.put(virtual_name, buf);
    // log.debug("PUT", basedir, virtual_name, "=>",
    //        file, new String(content));
    return buf;
  }


  @Override
  public ByteBuffer readFile(String path) throws IOException {
    Path p = Paths.get(path);
    ByteBuffer buf = file_cache.get(p.toString());
    //log.debug("READ", path);
    if (buf == null) {
      return putfile(path, path);
    }
    return buf;
  }


  @Override
  public ScriptAttr readAttribute(String path) throws IOException {
    Path p = Paths.get(basedir, path);
    BasicFileAttributes bfa = Files.readAttributes(p, BasicFileAttributes.class);
    ScriptAttr fa = new ScriptAttr(bfa, p);
    //log.debug("Attribute", path);
    return fa;
  }


  @Override
  public String getID() {
    return id;
  }


  public String getType() {
    return "LocalFileSystem";
  }

}
