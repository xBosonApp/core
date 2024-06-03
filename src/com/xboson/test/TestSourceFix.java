////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-8 下午2:11
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestSourceFix.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.app.fix.SourceFix;
import com.xboson.util.CodeFormater;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.Tool;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestSourceFix extends Test {

  @Override
  public void test() throws Throwable {
    test_file("js/need-fix.js", false);
    test_file("js/strict-mode.js", true);
    test_file("js/multi-line-str.js", false);
    not_modify("js/fix-not-change.js");
  }


  //
  // js 重写后应该与源代码相同 (没有特殊语法需要被重写)
  //
  private void not_modify(String js_file_path) throws Exception {
    Fix s = test_file(js_file_path);
    eq(s.src, s.fix, "don't change");
    msg("OK not change", js_file_path);
  }


  private Fix test_file(String js_file_path) throws Exception {
    return test_file(js_file_path, false);
  }


  private Fix test_file(String js_file_path, boolean isStrict) throws IOException {
    sub("Fix Js file:", js_file_path, "strict:", isStrict);
    StringBufferOutputStream buf =
            Tool.readFileFromResource(this.getClass(), js_file_path);

    byte[] src = buf.toBytes();
    boolean filemode = SourceFix.isStrictMode(src);
    eq(isStrict, filemode, "strict mode");

    if (isStrict) {
      String source = new String(src);
      test_fix_framework(source);
      return new Fix(source, null);
    } else {
      byte[] fix = SourceFix.fixFor(src);
      fix = SourceFix.fixJavaCall(fix);
      fix = SourceFix.fixVirtualAttr(fix);
      fix = SourceFix.multiLineString(fix);

      String fixed_source = new String(fix);
      test_fix_framework(fixed_source);
      // msg(js_file_path, line, "\n"+source);
      return new Fix(new String(src), fixed_source);
    }
  }


  /**
   * 在代码中写入正则表达式来验证转换结果.
   * 测试框架语法, 在注释中写入<br/>
   * <code>fix`正则表达式`>></code><br/>
   * 不可有多余空格, 将测试表达式写在被测代码附近.
   * 单行模式匹配, 注意不要让表达式匹配自己
   * (表达式含有 '/' 符号可以避免匹配自身).
   * @param source
   */
  private void test_fix_framework(final String source) throws IOException {
    List<Pattern> re = new ArrayList<>();
    final String s1 = "fix`";
    final String s2 = "`>>";
    final int s1len = s1.length();

    int i = 0, e = 0;
    for (;;) {
      i = source.indexOf(s1, e);
      if (i >= 0) {
        e = source.indexOf(s2, i + s1len);
        if (e > i) {
          String r = source.substring(i + s1len, e);
          Pattern p = Pattern.compile(r, Pattern.MULTILINE);
          re.add(p);
          continue;
        }
      }
      break;
    }

    for (i = 0; i<re.size(); ++i) {
      Pattern p = re.get(i);
      Matcher m = p.matcher(source);
      if (m.find()) {
        msg("OK:", "\u001b[;34m"+ p +"\u001b[m",
                "---MATCHER---\n       ", m.group());
      } else {
        fail("\u001b[;34m"+ p +"\u001b[m", "not found.");
        printCode(p, source);
      }
    }
  }


  private void printCode(final Pattern p, final String source) throws IOException {
    String code = p.toString();
    int i = source.indexOf(code);
    int begin = Math.max(0, i-100);
    int end = Math.min(i+code.length()+200, source.length());

    if (i > 0) {
      String subcode = source.substring(begin, end);
      CodeFormater cf = new CodeFormater(new StringReader(subcode));
      StringBuilder out = new StringBuilder();
      cf.printCode(out);
      msg("\n"+ line, "CODE:", "\n"+ out);
    }
  }


  public static void main(String [] a) {
    new TestSourceFix();
  }


  static class Fix {
    String src;
    String fix;
    Fix(String s, String f) {
      src = s;
      fix = f;
    }
  }
}
