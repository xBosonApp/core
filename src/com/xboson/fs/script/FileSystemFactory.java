////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-13 下午3:07
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/FileSystemFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.script;

import com.xboson.log.Log;
import com.xboson.log.LogFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.util.HashMap;
import java.util.Map;

/**
 * 这是一个文件系统的汇聚, 可以来自本地磁盘/网络磁盘/DB表/Redis缓存,
 * 凡是注册过的文件系统, 都可以再通过 open 打开.
 */
public class FileSystemFactory {

  private static FileSystemFactory instance;
  public static FileSystemFactory me() {
    if (instance == null) {
      synchronized (FileSystemFactory.class) {
        if (instance == null) {
          instance = new FileSystemFactory();
        }
      }
    }
    return instance;
  }


  private Log log;
  private Map<String, IScriptFileSystem> fss;


  private FileSystemFactory() {
    this.log = LogFactory.create();
    this.fss = new HashMap<>();
  }


  /**
   * 打开文件系统
   */
  public IScriptFileSystem open(String org, String app) {
    return open(org + app);
  }


  public IScriptFileSystem open(String id) {
    return fss.get(id);
  }


  /**
   * 将本地路径映射到一个文件系统中
   */
  public void addLocalFileSystem(String path, String id) {
    LocalFileSystem lfs = new LocalFileSystem(path, id);
    fss.put(id, lfs);
  }


  public void addLocalFileSystem(URL url, String id) throws URISyntaxException {
    File f = new File(url.toURI());
    addLocalFileSystem(f.getPath(), id);
  }
}
