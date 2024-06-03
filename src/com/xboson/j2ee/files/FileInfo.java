////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-26 下午1:32
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/FileInfo.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.files;

import com.xboson.db.SqlResult;
import com.xboson.util.Tool;

import java.io.InputStream;
import java.io.OutputStream;


public class FileInfo implements AutoCloseable {

  public String file_name;
  public String dir_name;

  public transient String type;
  public transient long last_modified;
  private transient SqlResult db_conn;
  public transient InputStream input;
  public transient OutputStream output;


  /**
   * 用这个构造函数创建的对象需要关闭
   */
  public FileInfo(String dir, String file, SqlResult db_conn) {
    this.file_name = file;
    this.dir_name = dir;
    this.db_conn = db_conn;
  }


  public FileInfo(String dir, String file) {
    this.file_name = file;
    this.dir_name = dir;
  }


  @Override
  public void close() throws Exception {
    Tool.close(input);
    Tool.close(db_conn);
    Tool.close(output);
    input = null;
    db_conn = null;
    output = null;
  }


  @Override
  protected void finalize() throws Throwable {
    close();
  }
}
