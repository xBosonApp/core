////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-13 下午4:43
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/script/lib/Checker.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script.lib;

import com.xboson.been.XBosonException;
import com.xboson.script.JSObject;

import java.util.HashSet;
import java.util.Set;

/**
 * 参数检查器, 即可用在脚本中, 也可用在 java 服务里
 * 所有检查失败的情况都会抛出 CheckException 异常,
 *
 * @see com.xboson.been.XBosonException
 * @see Checker.CheckException
 */
public class Checker extends JSObject {

  public static final Checker me = new Checker();

  private final static char[] letters = new char[] {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
    'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
    'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
    'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
  };

  private final static char[] digitals = new char[] {
          '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  };

  private final static char[] hexs = new char[] {
          'A', 'B', 'C', 'D', 'E', 'F',
          'a', 'b', 'c', 'd', 'e', 'f',
  };

  private final static String NULLSTR = "";

  private final static Set<Character> _uuid = new HashSet<>();
  private final static Set<Character> _symbol = new HashSet<>();
  private final static Set<Character> _base64 = new HashSet<>();


  static {
    // uuid
    copy(hexs, _uuid); _uuid.add('-');
    copy(digitals, _uuid);

    // _symbol
    copy(letters, _symbol);
    copy(digitals, _symbol);
    _symbol.add('_'); _symbol.add('$');

    // _base64
    copy(letters, _base64);
    copy(digitals, _base64);
    _base64.add('_');
    _base64.add('-');
  }


  /**
   * 字符串必须非空, 必须是 uuid 的完整形式,
   * 当检测出异常, 将使用 errmsg 作为错误消息抛出.
   */
  public void uuid(String arg, String errmsg) {
    check(arg, _uuid, errmsg);
  }


  /**
   * 非空, 且符合变量名规范, 以数字字母下划线开头的变量名
   */
  public void symbol(String arg, String errmsg) {
    check(arg, _symbol, errmsg);
  }


  /**
   * 非空, 且是 url 安全的 base64 字符串序列
   */
  public void base64url(String arg, String errmsg) {
    check(arg, _base64, errmsg);
  }


  /**
   * 安全的文件路径, 没有 /../ 溢出
   */
  public void safepath(String arg, String errmsg) {
    if (arg.indexOf("/../") >= 0 ||
            arg.indexOf("/..\\") >= 0 ||
            arg.indexOf("\\..\\") >= 0 ||
            arg.indexOf("\\../") >= 0) {
      throw new CheckException(errmsg, arg);
    }
  }


  public void check(String arg, Set<Character> table, String errmsg) {
    if (arg == null || arg.trim().equals(NULLSTR)) {
      throw new CheckException(errmsg, arg + " cannot be NULL");
    }

    final int len = arg.length();
    for (int i=0; i<len; ++i) {
      char c = arg.charAt(i);
      if (!table.contains(c)) {
        throw new CheckException(errmsg, arg, i, c);
      }
    }
  }


  static void copy(char[] from, Set<Character> to) {
    for (int i=0; i<from.length; ++i) {
      to.add(from[i]);
    }
  }


  /**
   * 抛出的消息格式:
   *
   *    "消息字符串: 变量值"
   */
  static public class CheckException extends XBosonException {
    public CheckException(String errmsg, Object arg, int pos, char c) {
      super(errmsg + ": " + arg + " [Invaild '" + c + "' at " + pos + "]");
    }
    public CheckException(String errmsg, Object arg) {
      super(errmsg + ": " + arg);
    }
  }
}
