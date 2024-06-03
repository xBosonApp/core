////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-30 下午1:38
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/rpc/SafeClientFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.rpc;

import com.xboson.util.AES;
import com.xboson.util.Tool;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;


public class SafeClientFactory implements RMIClientSocketFactory, Serializable {

  public static final int BUF_SIZE = SafeServerFactory.BUF_SIZE;
  public static final int SOCK_TIMEOUT = SafeServerFactory.SOCK_TIMEOUT;

  private byte[] password;


  public SafeClientFactory(String password) {
    this.password = AES.aesKey(password);
  }


  @Override
  public Socket createSocket(String host, int port) throws IOException {
    //Tool.pl("Create Socket", host, port, "--------------------------");
    Socket sock = new Socket(host, port);
    sock.setSoTimeout(SOCK_TIMEOUT);
    Certificate c = new Certificate(sock, password);

    try {
      c.recv(BUF_SIZE);
      c.send(BUF_SIZE);
    } catch (Exception e) {
      c.close();
      throw e;
    }
    return sock;
  }
}
