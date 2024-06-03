////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 下午8:29
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/ui/LocalFileMapping.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.redis;

import com.xboson.been.XBosonException;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.script.IVisitByScript;
import com.xboson.util.Tool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Set;


/**
 * 本地文件模式, 集群中除了一个本地模式, 其他节点都是 redis 模式;
 * 本地文件模式负责 redis 与本地文件的同步
 *
 * 启动: 将最新的文件同步到 redis, 读取 redis 上的修改通知队列同步到本地.
 * 读取: 比较本地文件和 redis 文件修改日期, 相同返回 redis, 否则做同步后返回最新文件.
 * 写入: 更新本地文件后更新 redis 文件, 不发送任何通知.
 */
public abstract class LocalFileMapping implements
        IRedisFileSystemProvider, IFileChangeListener, IVisitByScript {

  public static final int ID = 2;
  public static final int RETRY = 3;
  public static final int RETRY_DELAY = 1000;

  private FileModifyHandle fileModifer;
  private RedisFileMapping rfm;
  private RedisBase rb;
  private String basepath;
  private Log log;


  /**
   * 本地模式需要同步远程数据, 用到了 RedisFileMapping 里的方法
   */
  public LocalFileMapping(RedisFileMapping rfm, RedisBase rb) {
    IFileSystemConfig config = rb.getConfig();

    this.log          = LogFactory.create();
    this.basepath     = config.configLocalPath();
    this.rb           = rb;
    this.rfm          = rfm;
    this.fileModifer  = new FileModifyHandle(this, config);
  }


  /**
   * 保证返回的文件一定在 basepath 的子目录中.
   * 返回文件的本地目录
   */
  public Path normalize(String path) {
    return Paths.get(basepath, path);
  }


  @Override
  public byte[] readFile(String path) {
    try (RedisBase.JedisSession close = rb.openSession()) {
      RedisFileAttr fs = readAttribute(path);
      if (fs == null)
        throw new XBosonException.NotFound(path);

      readFileContent(fs);
      return fs.getFileContent();
    }
  }


  /**
   * 在读取文件时会尝试同步本地和 redis 上的文件内容.
   */
  @Override
  public void readFileContent(RedisFileAttr fs) {
    if (fs.isDir())
      throw new XBosonException.ISDirectory(fs.path);

    Path local_file = normalize(fs.path);

    if (fs.mappingID() == ID) {
      fs.setFileContent(readLocalFile(local_file));

      if (fs.needSynchronization()) {
        rfm.writeFile(fs, false);
      }
    } else {
      rfm.readFileContent(fs);

      if (fs.needSynchronization()) {
        writeLocalFile(local_file, fs.getFileContent(), fs.lastModify);
      }
    }
  }


  private byte[] readLocalFile(Path file) {
    try {
      return Files.readAllBytes(file);
    } catch (IOException e) {
      throw new XBosonException.IOError(e);
    }
  }


  private void writeLocalFile(Path file, byte[] content, long modify) {
    try {
      Files.createDirectories(file.getParent());
      Files.write(file, content);
      Files.setLastModifiedTime(file, FileTime.fromMillis(modify));
      rb.clearContentFinderCache();
    } catch (IOException e) {
      throw new XBosonException.IOError(e);
    }
  }


  /**
   * 返回本地文件属性, 文件不存在或出错返回 null
   * Path.toString 会将目录转换格式, 所以保留 path 为原始路径字符串.
   */
  private LocalFileStruct readLocalAttr(Path local_file, String path) {
    File f = local_file.toFile();

    if (f.exists()) {
      try {
        RedisFileAttr fs;

        if (f.isDirectory()) {
          fs = RedisFileAttr.createDir(path);
        } else {
          long local_t = f.lastModified();
          fs = RedisFileAttr.createFile(path, local_t, null);
        }

        return new LocalFileStruct(fs, true);

      } catch (Exception e) {
        log.warn("readLocalAttr()", local_file, path, e);
      }
    }
    return null;
  }


  @Override
  public RedisFileAttr readAttribute(String path) {
    try (RedisBase.JedisSession close = rb.openSession()) {
      path = Tool.normalize(path);
      Path local_file = normalize(path);

      RedisFileAttr localfs = readLocalAttr(local_file, path);
      RedisFileAttr redisfs = rfm.readAttribute(path);

      if (localfs == null) {
        if (redisfs != null)
          redisfs.setSynchronization(true);

        return redisfs;
      } else if (redisfs == null) {
        return localfs;
      }

      //
      // redisfs AND localfs Both NOT NULL
      //
      if (localfs.lastModify > redisfs.lastModify) {
        return localfs;
      }
      else if (localfs.lastModify == redisfs.lastModify) {
        redisfs.setSynchronization(false);
      }
      else /* local < redis */ {
        redisfs.setSynchronization(true);
      }

      return redisfs;
    }
  }


  @Override
  public long modifyTime(String path) {
    RedisFileAttr fs = readAttribute(path);
    if (fs == null)
      return -1;

    return fs.lastModify;
  }


  @Override
  public void makeDir(String path) {
    noticeMakeDir(path);
    rfm.makeDir(path, false);
  }


  @Override
  public void noticeMakeDir(String path) {
    try {
      Path file = normalize(path);
      Files.createDirectories(file);
    } catch (Exception e) {
      log.error("Make dir", path, e);
    }
  }


  @Override
  public void noticeDelete(String file) {
    Path local_file = normalize(file);
    try {
      Files.deleteIfExists(local_file);
    } catch (IOException e) {
      log.error("Delete", e);
    }
  }


  @Override
  public void noticeMove(String src, String to) {
    Path srcp = normalize(src);
    Path top = normalize(to);
    try {
      Files.move(srcp, top);
    } catch (IOException e) {
      throw new XBosonException.IOError(e);
    }
  }


  @Override
  public void delete(String file) {
    noticeDelete(file);
    rfm.delete(file, false);
  }


  @Override
  public void move(String src, String to) {
    noticeMove(src, to);
    rfm.move(src, to, false);
  }


  @Override
  public Set<RedisFileAttr> readDir(String path) {
    Path local_file = normalize(path);
    File f = local_file.toFile();

    if (! f.exists())
      throw new XBosonException.NotFound(path);

    if (! f.isDirectory())
      throw new XBosonException.IOError("Is not dir: " + path);

    File[] files = f.listFiles();
    Set<RedisFileAttr> ret = new HashSet<>(files.length);
    RedisFileAttr fs;

    for (int i=0; i<files.length; ++i) {
      f = files[i];
      if (f.isFile()) {
        fs = RedisFileAttr.createFile(f.getName(), f.lastModified(), null);
        ret.add(fs);
      } else if (f.isDirectory()) {
        fs = RedisFileAttr.createDir(f.getName());
        ret.add(fs);
      } else {
        log.warn("Skip file is not file or dir:", f);
      }
    }
    return ret;
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
  public void writeFile(String path, byte[] bytes) {
    Path local_file = normalize(path);
    long modified_time = System.currentTimeMillis();

    writeLocalFile(local_file, bytes, modified_time);
    rfm.writeFile(path, bytes, modified_time, false);
  }


  /**
   * 尝试多次延迟读取文件内容, 因为可能有网络延迟或集群延迟.
   * 只在同步消息中使用, 不可在实时系统中调用.
   */
  private RedisFileAttr getContentTryManyTimes(String file) {
    RedisFileAttr fs = null;

    for (int i=RETRY; i>=0; --i) {
      fs = rb.getStruct(file);
      if (fs != null) break;
      Tool.sleep(RETRY_DELAY);
      log.debug("Retry open", file, RETRY - i);
    }

    if (fs == null) {
      throw new XBosonException.NotFound(file);
    }
    return fs;
  }


  @Override
  public void noticeModifyContent(String file) {
    try (RedisBase.JedisSession close = rb.openSession()) {
      rb.clearContentFinderCache();
      RedisFileAttr fs = getContentTryManyTimes(file);
      rb.getContent(fs);

      long mt = fs.lastModify;
      byte[] content = fs.getFileContent();

      Path local_file = normalize(file);
      writeLocalFile(local_file, content, mt);
    } catch(Exception e) {
      log.error("Received modification:", file, e);
    }
  }


}
