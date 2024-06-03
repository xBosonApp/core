////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-16 上午10:18
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/reader/AbsReadScript.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.reader;

import com.xboson.app.ApiEncryption;
import com.xboson.app.XjOrg;
import com.xboson.app.fix.SourceFix;
import com.xboson.app.lib.IApiConstant;
import com.xboson.fs.script.ScriptAttr;
import com.xboson.log.ILogName;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.c0nst.IConstant;


/**
 * 读取脚本文件, 该对象是多线程可重入的.
 */
public abstract class AbsReadScript
        implements IConstant, IApiConstant, ILogName {

  protected final Log log;


  public AbsReadScript() {
    log = LogFactory.create(this);
  }


  /**
   * 必须实现该方法, 返回已经打过补丁的脚本源代码.
   * 读取不到脚本必须抛出异常.
   */
  public abstract ScriptFile read(XjOrg org, String app, String mod, String api);


  /**
   * 为代码打补丁, 返回脚本文件
   */
  protected ScriptFile makeFile(ScriptAttr attr, String original_code) {
    byte[] original_byte = ApiEncryption.decryptApi(original_code);
    byte[] content = SourceFix.autoPatch(original_byte);
    return new ScriptFile(content, original_code, attr);
  }
}
