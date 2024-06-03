////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-22 下午2:18
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestApi.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.app.AppContext;
import com.xboson.app.lib.SysImpl;
import com.xboson.been.*;
import com.xboson.fs.script.IScriptFileSystem;
import com.xboson.j2ee.container.XResponse;
import com.xboson.j2ee.emu.EmuServletRequest;
import com.xboson.j2ee.emu.EmuServletResponse;
import com.xboson.j2ee.files.PrimitiveOperation;
import com.xboson.util.c0nst.IConstant;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class TestApi extends Test {

  public void test() throws Exception {
    PrimitiveOperation.me().createCleanTask().run();
    test_pool();
    test_lottery_rate();
  }


  // 需要模拟一个 servlet 上下文, 否则测试错误
  public void test_pool() throws Exception {
    sub("Test app pool");

    RunApi ra = new RunApi();

    ra.run("test_double", "test-sys");
    ra.run("test_double", "test-sql");
    ra.run("test_double", "map0");
    ra.run("test_double", "list1");
    ra.run("test_double", "date0");
    ra.run("test_double", "http0");
    ra.run("test_double", "cache0");
    ra.run("test_double", "se0");
  }


  /**
   * 方便调用系统 api 进行测试
   */
  public void test_sys_api(String app, String mod, String api) throws Exception {
    sub("Test sys api:", app, '/', mod, '/', api);

    RunApi ra = new RunApi(app);
    ra.run(mod, api);
  }


  public static CallData simulationCallData() throws Exception {
    EmuServletRequest req = new EmuServletRequest();
    EmuServletResponse resp = new EmuServletResponse();
    XResponse xr = new XResponse(req, resp);
    SessionData sd = new SessionData();
    sd.login_user = new Admin();
    sd.login_user.pid = "e3e5cf168dd24b44ba4b72775d5fb215";
    sd.login_user.userid = "root";
    req.setAttribute(SessionData.ATTRNAME, sd);
    return new CallData(req, resp);
  }


  public void show_code(IScriptFileSystem fs, String path) throws IOException {
    ByteBuffer buf = fs.readFile(path);
    String code = new String(buf.array());
    printCode(code);
  }


  public void test_lottery_rate() throws Exception {
    sub("Test lottery rate");
    double[] ret = new double[] {0,0,0,0,0};
    double[] list = new double[] {20.5, 10.0, 50.5, 10.0};
    int[] ign = new int[] {1, 3};

    SysImpl sys = new SysImpl();

    for (int i=0; i<100000; ++i) {
      int r = sys.lotteryRate(list, ign);
      ret[r] += 1;
    }
    double sum = 0;
    for (int i=0; i<ret.length; ++i) sum += ret[i];
    for (int i=0; i<ret.length; ++i) ret[i] /= sum / 100;

    msg("运行结果", Arrays.toString(ret));
    msg("正确值应该在 30.00 0.0 60.00 0.0 10.00 上下浮动");

    ok(Math.abs(ret[0] - 30.0) < 1, "arr[0] = 30%");
    ok(ret[1] == 0, "arr[1] = 0%");
    ok(Math.abs(ret[2] - 60.0) < 1, "arr[2] = 60%");
    ok(ret[3] == 0, "arr[3] = 0%");
    ok(Math.abs(ret[4] - 10.0) < 1, "arr[4] = 10%");
  }


  public static void main(String[] a) {
    new TestApi();
  }


  static class RunApi {
    ApiCall ac;

    RunApi() throws Exception {
      this("a297dfacd7a84eab9656675f61750078",
              "a9943b0fb1e141b3a3ce7e886d407f5b");
    }

    public RunApi(String app) throws Exception {
      this(IConstant.SYS_ORG, app);
    }

    public RunApi(String org, String app) throws Exception {
      ac = new ApiCall(org, app, null, null);
    }

    void run(String module_id, String api_id) throws IOException {
      sub("Run Script", module_id, api_id);
      try {
        ac.mod = module_id;
        ac.api = api_id;
        ac.call = simulationCallData();
        AppContext.me().call(ac);
      } catch(Exception e) {
        // show_code(app, XjApp.toFile(module_id, api_id));
        fail(e);
        e.printStackTrace();
        throw new XBosonException(e);
      }
    }
  }


  static class Admin extends LoginUser {
    public boolean isRoot() {
      return true;
    }
  }
}
