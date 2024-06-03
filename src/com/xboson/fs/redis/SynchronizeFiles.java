////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-20 上午9:07
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/j2ee/ui/SynchronizeFiles.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.redis;

import com.xboson.event.EventLoop;
import com.xboson.event.timer.EarlyMorning;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.Tool;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;


/**
 * 同步 redis 和本地文件系统中的所有文件
 */
public final class SynchronizeFiles implements Runnable, FileVisitor<Path> {

  private RedisFileMapping rfm;
  private RedisBase rb;
  private Log log;
  private Path base;
  private long begin_time;
  private int files = 0;
  private int dirs  = 0;
  private int d     = 50;


  /**
   * 创建一个文件同步器, 同步本地文件到 redis;
   * 文件同步器创建后不会启动, 需要放入其他的任务管理器中
   *
   * @param rb 基础方法
   * @param rfm 扩展方法
   * @see EventLoop 全局单线程任务管理
   * @see EarlyMorning 午夜任务管理器
   */
  public SynchronizeFiles(RedisBase rb, RedisFileMapping rfm) {
    this.log  = LogFactory.create();
    this.rb   = rb;
    this.rfm  = rfm;
    this.base = Paths.get(rb.getConfig().configLocalPath());
  }


  @Override
  public void run() {
    try (RedisBase.JedisSession close = rb.openSession()) {

      log.info("Start At:", base);
      begin_time = System.currentTimeMillis();

      log.info("Local TO Redis");
      Files.walkFileTree(base, this);

      //
      // Redis to Local
      // 这个机能借助于文件修改消息队列来实现, 程序启动后将收到 redis 修改历史
      // 将历史合并到本机目录中.
      //
    } catch (IOException e) {
      log.error(e);
    } finally {
      long used = System.currentTimeMillis() - begin_time;
      log.info("Sync Over,", files,
               "files and", dirs, "directorys, use", used, "ms");
    }
  }


  /**
   * 转换为 redis 上的虚拟路径
   * @param local
   */
  private String getVirtualPath(Path local) {
    return Tool.normalize("/"+ base.relativize(local));
  }


  @Override
  public FileVisitResult preVisitDirectory(
          Path local_path, BasicFileAttributes basicFileAttributes)
          throws IOException {
    String vpath = getVirtualPath(local_path);
    rfm.makeDir(vpath, false);
    ++dirs;

    return FileVisitResult.CONTINUE;
  }


  @Override
  public FileVisitResult visitFile(
          Path local_path, BasicFileAttributes basicFileAttributes)
          throws IOException {

    String vpath = getVirtualPath(local_path);

    final long local_t = Files.getLastModifiedTime(local_path).toMillis();

    RedisFileAttr redis_file = rb.getStruct(vpath);
    final long redis_t = redis_file != null ? redis_file.lastModify : -1;

    if (local_t > redis_t) {
      byte[] body = Files.readAllBytes(local_path);
      redis_file = RedisFileAttr.createFile(vpath, local_t, body);
      rfm.writeFile(redis_file, false);
    }
    else if (local_t < redis_t) {
      rb.getContent(redis_file);
      byte[] body = redis_file.getFileContent();
      Files.write(local_path, body);
      Files.setLastModifiedTime(local_path, FileTime.fromMillis(redis_t));
    }

    if (++files > d) {
      d = files * 2;
      log.debug("Process", files, ", use",
              System.currentTimeMillis()-begin_time, "ms", "...");
    }
    return FileVisitResult.CONTINUE;
  }


  @Override
  public FileVisitResult visitFileFailed(
          Path path, IOException e) throws IOException {
    if (e != null) {
      log.error(e);
    }
    return FileVisitResult.CONTINUE;
  }


  @Override
  public FileVisitResult postVisitDirectory(
          Path path, IOException e) throws IOException {
    if (e != null) {
      log.error(e);
    }
    return FileVisitResult.CONTINUE;
  }
}
