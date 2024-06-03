////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-19 下午3:16
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestMasquerade.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.fs.redis.IRedisFileSystemProvider;
import com.xboson.fs.ui.UIFileFactory;
import com.xboson.j2ee.ui.TemplateEngine;
import com.xboson.j2ee.emu.EmuServletRequest;
import com.xboson.j2ee.emu.EmuServletResponse;


public class TestMasquerade extends Test {

  //
  // 这个测试会抛出异常, 因为初始化时 Startup 没有 ServletContext
  //
  @Override
  public void test() throws Throwable {
    page();
  }


  private void page() throws Throwable {
    sub("Request page");
    IRedisFileSystemProvider uifs = UIFileFactory.open();
    TemplateEngine te = new TemplateEngine(uifs);

    String[] pages = new String[] {
            "/face/t/paas/api-doc/index.htm",
            "/face/t/paas/mdms/datadictD/index.htm",
    };

    for (String page : pages) {
      EmuServletResponse resp = new EmuServletResponse();
      EmuServletRequest req = new EmuServletRequest();
      req.requestUriWithoutContext = page;
      te.service(req, resp);
    }
  }


  public static void main(String[] a) {
    new TestMasquerade();
  }

}
