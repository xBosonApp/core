////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-31 下午2:32
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/Cluster.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.auth.IAResource;
import com.xboson.auth.PermissionSystem;
import com.xboson.auth.impl.LicenseAuthorizationRating;
import com.xboson.been.ComputeNodeInfo;
import com.xboson.been.XBosonException;
import com.xboson.rpc.ClusterManager;

import java.util.Set;


public class Cluster implements IAResource {


  public Object open() {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);

    boolean runOnSysOrg = (boolean)
            ModuleHandleContext._get("runOnSysOrg");

    if (!runOnSysOrg)
      throw new XBosonException.NotImplements("只能在平台机构中引用");

    return new Local();
  }


  @Override
  public String description() {
    return "app.module.cluster.functions()";
  }


  public class Local {
    private ClusterManager cm;

    private Local() {
      cm = ClusterManager.me();
    }

    public String[] list() {
      Set<String> set = cm.list();
      return set.toArray(new String[set.size()]);
    }

    public ComputeNodeInfo info(String id) {
      return cm.info(id);
    }
  }
}
