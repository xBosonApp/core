////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-16 上午8:09
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestAllApi.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.app.ApiEncryption;
import com.xboson.app.ApiPath;
import com.xboson.app.ServiceScriptWrapper;
import com.xboson.app.fix.SourceFix;
import com.xboson.db.ConnectConfig;
import com.xboson.db.DbmsFactory;
import com.xboson.db.SqlResult;
import com.xboson.fs.script.ScriptAttr;
import com.xboson.fs.script.IScriptFileSystem;
import com.xboson.log.Level;
import com.xboson.log.LogFactory;
import com.xboson.script.Application;
import com.xboson.util.CodeFormater;
import com.xboson.util.JavaConverter;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 将平台上所有代码全部编译
 */
public class TestAllApi extends Test implements IScriptFileSystem {

  private final static String all_org_sql =
          "SELECT orgid FROM a297dfacd7a84eab9656675f61750078.mdm_org;";

  private final static String all_api =
          "SELECT \n" +
          "    ap.appid `app`, ap.moduleid `mod`, ap.apiid `api`, ac.content\n" +
          "FROM\n" +
          "    sys_api_content ac,\n" +
          "    sys_apis ap\n" +
          "WHERE ac.contentid = ap.contentid\n";

  private final static String SKIP = "\u001b[;30mSkip";

  /**
   * 即使出错也只是忽略问题, 可能是故意出错, 或是不再使用的 api.
   */
  private final static Set<String> skip_fail = JavaConverter.arr2set(new String[] {
          "/test_double/safe",
          "/user_manager/createschema",
  });


  private Map<String, Code> allcode;


  @Override
  public void test() throws Throwable {
    allcode = new HashMap<>();
    LogFactory.setLevel(Level.INFO);

    beginTime();
    readAllCode();
    endTime("Load all code");

    beginTime();
    compile();
    endTime("All done");
    memuse();

    LogFactory.setLevel(Level.ALL);
  }


  // 需要模拟一个 servlet 上下文, 否则测试错误
  public void compile() throws Exception {
    sub("Compile all code");
    ServiceScriptWrapper ssw = new ServiceScriptWrapper();
    Application runtime = new Application(ssw.getEnvironment(), this);
    int success = 0, total = 0, fail = 0, skip = 0;

    for (Map.Entry<String, Code> s : allcode.entrySet()) {
      Code code = s.getValue();
      ++total;
      try {
        if (code.code == null) {
          msg(SKIP, code.path, ".. NULL code");
          ++skip;
          continue;
        }
        code.wrapd = ssw.wrap(code.code);
        runtime.run(code.path);
        msg("Compile", code.path, ".. success");
        ++success;
      } catch (Exception e) {
        if (skip_fail.contains(code.path)) {
          msg(SKIP, "fail", e.getMessage());
          ++skip;
          continue;
        }

        ++fail;
        fail("Compile", code.path, "..", Tool.allStack(e));

        StringBuilder out = new StringBuilder();
        CodeFormater cf = new CodeFormater(code.wrapd);
        cf.printCode(out);
        msg(line, "CODE:", "\n"+ out);
      }
    }

    msg(".");
    msg("Total", total, "script,", "success:", success,
            ", fail:", fail, ", skip:", skip);
  }


  public void readAllCode() throws SQLException {
    sub("Load all code from all org.");
    ConnectConfig db = SysConfig.me().readConfig().db;

    try (Connection conn = DbmsFactory.me().open(db)) {
      SqlResult sr = SqlResult.query(conn, all_org_sql);
      List<Map<String, Object>> orgs = sr.resultToList();

      for (Map<String, Object> org : orgs) {
        final Object orgid = org.get("orgid");
        msg("From org:", orgid);
        SqlResult.query(conn, "Use " + orgid);

        SqlResult apis = SqlResult.query(conn, all_api);
        ResultSet rows = apis.getResult();
        int api_count = 0;

        while (rows.next()) {
          Code c = new Code();
          c.path = ApiPath.toFile(rows.getString("mod"), rows.getString("api"));

          String original_code = rows.getString("content");
          if (! Tool.isNulStr(original_code)) {
            byte[] original_byte = ApiEncryption.decryptApi(original_code);
            c.code = SourceFix.autoPatch(original_byte);
          }
          allcode.put(c.path, c);
          ++api_count;
        }
        msg(SPSP, "has", api_count, "api");
      }
    }
    msg(".");
    msg("Load", allcode.size(), "scripts");
  }


  public static void main(String[] a) {
    new TestAllApi();
  }


  @Override
  public ByteBuffer readFile(String path) throws IOException {
    Code c = allcode.get(path);
    return c.wrapd;
  }


  @Override
  public ScriptAttr readAttribute(String path) throws IOException {
    return new ScriptAttr();
  }


  @Override
  public String getID() {
    return "test";
  }


  @Override
  public String getType() {
    return "test";
  }


  public class Code {
    public byte[] code;
    public String path;
    public ByteBuffer wrapd;
  }
}
