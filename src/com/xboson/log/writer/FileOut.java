////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月3日 下午4:11:40
// 原始文件路径: xBoson/src/com/xboson/log/FileOut.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.log.writer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.xboson.log.ILogWriter;
import com.xboson.log.Level;
import com.xboson.log.OutBase;
import com.xboson.util.c0nst.IConstant;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;


public class FileOut extends OutBase implements ILogWriter {

  private static final String line = "\r\n";
  private static final String logFileNameFormat = "yyyy-MM-dd HH";
  private static final long checkPeriod = 1 * 60 * 1000;
  private static final long resetSize = 10 * 1024 * 1024;

  private File currentFile;
  private Writer writer;
  private Timer checksize;


  public FileOut() throws IOException {
    currentFile = logFile();
    switchOutFile();
    checksize = new Timer(true);
    checksize.schedule(new CheckSize(), checkPeriod, checkPeriod);
  }


  private File logFile() {
    SimpleDateFormat f = new SimpleDateFormat(logFileNameFormat);
    String name = SysConfig.me().readConfig().logPath;
    name += "/" + f.format(new Date()) + "h.log";
    return new File(name);
  }


  @Override
  public synchronized void output(Date d, Level l, String name, Object[] msg) {
    try {
      //
      // 因为没有在 Log 中做线程同步, 写出时, 该对象可能已经关闭而抛出异常,
      // 考虑到性能原因, 接受在切换输出时丢失部分日志.
      //
      format(writer, d, l, name, msg);
      writer.append(line);
    } catch(Exception e) {
      nolog("File Writer Fail: " + e);
    }
  }


  @Override
  public synchronized void destroy(ILogWriter replace) {
    checksize.cancel();
    Tool.close(writer);
  }


  private synchronized void switchOutFile() throws IOException {
    OutputStream o = new FileOutputStream(currentFile, true);
    writer = new OutputStreamWriter(o, IConstant.CHARSET);
  }


  private class CheckSize extends TimerTask {
    private int num = 0;

    public void run() {
      if (currentFile.length() > resetSize) {
        synchronized(FileOut.this) {
          Tool.close(writer);

          File rename;
          do {
            rename = new File(currentFile.getPath() + '.' + num);
            ++num;
          } while (rename.exists());

          Tool.pl("Log output file switch", currentFile, "->", rename);
          currentFile.renameTo(rename);

          try {
            switchOutFile();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
