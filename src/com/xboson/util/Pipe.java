////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-13 下午5:30
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/Pipe.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;
import com.xboson.event.EventLoop;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 很多 api 只接受一个输入流, 程序将数据全部写入缓冲区, 再从缓冲区读取,
 * 这种方法将不必要的占用内存. 该类将启动一个线程并限定一个上下文, 在上下文中将数据写出,
 * 数据做一个小的缓冲之后立即被读取, 写入和读取自动同步, 这将不会产生大量的内存占用.
 */
public class Pipe {

  /**
   * 和 EventLoop 混用容易死锁
   */
  private static final ExecutorService worker
          = Executors.newFixedThreadPool(10);

  private PipedOutputStream outs;
  private PipedInputStream ins;
  private Context context;


  public Pipe(Context c) throws IOException {
    if (c == null)
      throw new XBosonException.NullParamException("Context c");

    this.context = c;
    this.ins     = new PipedInputStream();
    this.outs    = new PipedOutputStream(ins);
  }


  /**
   * 该方法将打开输入流, 输入的数据来自 Context.run() 中的输出,
   * 必须尽可能从 InputStream 读取数据, 不要有另外的锁操作, 否则容易死锁.
   */
  public InputStream openInputStream() {
    worker.execute(() -> {
      try {
        context.run(outs);
      } finally {
        Tool.close(outs);
      }
    });
    return ins;
  }


  /**
   * 写出数据的限定范围
   */
  public interface Context {

    /**
     * 在该方法中写出数据, 实现无需关心线程同步等问题;
     * 该方法将在单独的线程中执行, 方法退出后 out 将被关闭.
     *
     * @param out 写出的数据将被输入流读取, 如果缓冲区已满, Write()方法将被临时阻塞
     */
    void run(OutputStream out);
  }
}
