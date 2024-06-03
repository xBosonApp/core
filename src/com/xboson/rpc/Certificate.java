////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-30 下午3:20
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/rpc/Certificate.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.rpc;

import com.xboson.auth.PermissionException;
import com.xboson.util.AES;
import com.xboson.util.Tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;


public class Certificate {

  private InputStream i;
  private OutputStream o;
  private Socket sock;
  private byte[] ps;


  Certificate(Socket sock, byte[] ps) throws IOException {
    this.sock = sock;
    this.ps   = ps;
    this.o    = sock.getOutputStream();
    this.i    = sock.getInputStream();
  }


  /**
   * 发送一个认证数据包
   *
   * @param size 数据包长度
   * @return
   * @throws IOException
   * @throws PermissionException 如果认真失败抛出异常
   */
  public void send(int size) throws IOException, PermissionException {
    byte[] wbuf = Tool.randomBytes(size);
    o.write(wbuf);

    wbuf = AES.Encode(wbuf, ps);
    byte[] rbuf = new byte[wbuf.length];

    if (i.read(rbuf) != rbuf.length)
      throw new PermissionException("bad certificate");

    if (! Arrays.equals(wbuf, rbuf))
      throw new PermissionException("bad certificate");
  }


  /**
   * 接收一个认证包
   *
   * @param size 数据包长度
   * @return
   * @throws IOException
   * @throws PermissionException
   */
  public void recv(int size) throws IOException, PermissionException {
    byte[] rbuf = new byte[size];

    if (i.read(rbuf) != size)
      throw new PermissionException("bad certificate");

    byte[] wbuf = AES.Encode(rbuf, ps);
    o.write(wbuf);
  }


  /**
   * 该方法用来关闭底层 socket, 认证失败后调用
   */
  public void close() {
    Tool.close(sock);
    sock = null;
  }
}
