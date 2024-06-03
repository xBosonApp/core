////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-4-3 上午7:23
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/auth/impl/LicenseAuthorizationRating.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.auth.impl;

import com.xboson.auth.IAResource;
import com.xboson.auth.IAWhere;
import com.xboson.auth.IAWho;
import com.xboson.auth.PermissionException;
import com.xboson.been.License;
import com.xboson.j2ee.container.Processes;
import com.xboson.j2ee.container.Striker;


/**
 * 检查 License 并决定资源是否能访问, 一定不会检查 IAWho 参数.
 */
public class LicenseAuthorizationRating implements IAWhere {

  private Processes plc;


  public LicenseAuthorizationRating() {
    plc = Processes.me();
  }


  @Override
  public boolean apply(IAWho who, IAResource res) {
    String msg = plc.message();

    if (msg != null) {
      throw new NoLicense(msg);
    }

    String api = res.description();
    License lic = plc.message2();

    if (lic.api == null || (! lic.api.contains(api))) {
      throw new NoLicense(api);
    }

    return true;
  }


  @Override
  public boolean passAdmin() {
    return false;
  }


  public String toString() {
    return "接口授权检查";
  }


  public class NoLicense extends PermissionException {
    private NoLicense(String msg) {
      super(Processes.s[8] + msg);
    }
  }
}
