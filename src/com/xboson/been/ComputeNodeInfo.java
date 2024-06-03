////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-30 上午11:29
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/ComputeNodeInfo.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import com.xboson.util.Network;
import com.xboson.util.Tool;

import java.util.Properties;


public class ComputeNodeInfo extends JsonHelper {

  public long startAt;
  public String nodeID;
  public String[] ip;
  public String javaVersion;
  public String javaVendor;
  public String osName;
  public String osVersion;
  public String osArch;
  public int rpcPort;


  /**
   * 初始化空对象
   */
  public ComputeNodeInfo() {
  }


  /**
   * 初始化完整对象
   */
  public ComputeNodeInfo(String nodeid, int rpcPort) {
    Properties sysattr = System.getProperties();
    this.ip            = Network.toAddressString(Network.netWorkerInterfaces());
    this.javaVersion   = sysattr.getProperty("java.version");
    this.javaVendor    = sysattr.getProperty("java.vendor");
    this.osName        = sysattr.getProperty("os.name");
    this.osVersion     = sysattr.getProperty("os.version");
    this.osArch        = sysattr.getProperty("os.arch");
    this.nodeID        = nodeid;
    this.rpcPort       = rpcPort;
    this.startAt       = System.currentTimeMillis();
  }
}
