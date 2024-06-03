////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-13 下午4:45
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/XBosonException.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import com.xboson.script.JScriptException;
import com.xboson.util.CodeFormater;
import com.xboson.util.c0nst.IConstant;
import jdk.nashorn.internal.runtime.ECMAErrors;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;


/**
 * 异常基础类, 不需要特别捕获
 */
public class XBosonException extends RuntimeException
        implements IBean, IXBosonException, IConstant {

  public static final String PREFIX = ENTER + SPSP;
  public static final String PASS   = PREFIX + "...";
  public static final String CAUSE  = ENTER + "Cause BY ";

  protected static final Set<String> repeated = new HashSet<>();
  protected static final int repeated_max = 10000;

  protected int code = 500;


  public XBosonException() {
    super();
  }

  public XBosonException(String s) {
    super(s);
  }

  public XBosonException(String s, int code) {
    super(s);
    setCode(code);
  }

  public XBosonException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public XBosonException(String s, XBosonException throwable) {
    super(s, throwable.getCause());
    setCode(throwable.code);
  }

  public XBosonException(Throwable throwable) {
    super(throwable.getMessage(), throwable);
  }

  public XBosonException(XBosonException o) {
    super(o.getMessage(), o.getCause());
    setCode(o.code);
  }

  protected XBosonException(String s, Throwable throwable, boolean b, boolean b1) {
    super(s, throwable, b, b1);
  }

  public int getCode() {
    return code;
  }

  void setCode(int c) {
    code = c;
  }


  /**
   * 如果 t 错误已经通过该方法检查过, 则返回 true.
   * 解决有大量重复异常被抛出后日志爆炸的情况.
   */
  public static boolean isChecked(Throwable t) {
    if (t == null) return false;
    String s = t.toString();
    if (repeated.size() > repeated_max) {
      repeated.clear();
    }
    return repeated.add(s);
  }


  /**
   * 堆栈只保留 xboson 对象, 和脚本消息, Cause 中的消息也会被包含
   */
  public static void filterStack(Throwable e, StringBuilder out) {
    StackTraceElement[] st = e.getStackTrace();
    out.append(e.toString());

    if (e instanceof CodeFormater.JSSource
            || e instanceof JScriptException) {
      for (int i = 0; i < st.length; ++i) {
        out.append(PREFIX);
        out.append(st[i].toString());
      }
    } else {
      filterStack(st, out);
    }

    Throwable c = e.getCause();
    if (c != null) {
      out.append(CAUSE);
      filterStack(c, out);
    }
  }


  public static void filterStack(StackTraceElement[] st, StringBuilder out) {
    boolean bypass = false;

    for (int i = 0; i < st.length; ++i) {
      StackTraceElement t = st[i];

      if (t.getClassName().startsWith("com.xboson") ||
              ECMAErrors.isScriptFrame(t))
      {
        out.append(PREFIX);
        out.append(t.toString());
        bypass = false;
      }
      else if (!bypass) {
        out.append(PASS);
        bypass = true;
      }
    }
  }


  /**
   * 参数为空则抛出异常
   */
  static public class NullParamException extends XBosonException {
    /**
     * @param paramName 参数的完成名字, 如: "String paramName"
     */
    public NullParamException(String paramName) {
      super("The Function parameter \"" + paramName + "\" can not be NULL");
      setCode(1);
    }

    public static void check(Object value, String paramName) {
      if (value == null)
        throw new NullParamException(paramName);
    }
  }


  /**
   * 方法暂时没有实现, 抛出这个异常
   */
  static public class NotImplements extends XBosonException {
    public NotImplements() {
      super("The Function/Method is not implemented yet");
      setCode(4);
    }
    /**
     * @param fname 函数的完整名字
     */
    public NotImplements(String fname) {
      this(fname, NULL_STR);
      setCode(4);
    }
    /**
     * @param fname 函数的完整名字
     * @param why 未实现的原因
     */
    public NotImplements(String fname, String why) {
      super("The " + fname + "() is not implemented yet; " + why);
      setCode(4);
    }
  }


  /**
   * sql 执行错误抛出的异常
   */
  static public class XSqlException extends XBosonException {
    public XSqlException(SQLException sqle) {
      super(sqle);
    }
    /**
     * @param whatDoing 正在做什么导致的错误
     */
    public XSqlException(String whatDoing, SQLException sqle) {
      super(whatDoing, sqle);
    }
    public XSqlException(String whatDoing, Throwable sqle) {
      super(whatDoing, sqle);
    }
  }


  /**
   * 当寻找的资源/对象不存在时抛出的异常
   */
  static public class NotExist extends XBosonException {
    /**
     * @param whatThing 什么东西找不到
     */
    public NotExist(String whatThing, SQLException sqle) {
      super(whatThing, sqle);
    }
    /**
     * @param whatThing 什么东西找不到
     */
    public NotExist(String whatThing) {
      super(whatThing);
    }
  }


  /**
   * 当参数无效时抛出这个异常
   */
  static public class BadParameter extends XBosonException {
    public BadParameter(String pname, String cause) {
      super("Parameter: '" + pname + "' invalid, " + cause);
      setCode(2);
    }
  }


  /**
   * 找不到服务错误
   */
  static public class NoService extends XBosonException {
    private String serviceName = null;
    public NoService(String serviceName) {
      super("Not found sub Service: " + serviceName);
      this.serviceName = serviceName;
      super.code = 4;
    }
    public String getServiceName() {
      return serviceName;
    }
  }


  /**
   * 尝试访问已经关闭的资源抛出异常
   */
  static public class Closed extends XBosonException {
    public Closed(String whoClosed) {
      super(whoClosed + " is Closed or Already Quit");
    }
  }


  /**
   * 系统已经关闭后, 掉用子系统功能抛出的异常
   */
  static public class Shutdown extends Closed {
    public Shutdown() {
      super("System");
    }
  }


  static public class IOError extends XBosonException {
    private String path;

    public IOError(String why) {
      super(why);
      this.path = null;
    }

    public IOError(IOException e) {
      super(e);
      this.path = null;
    }

    public IOError(String why, Path path) {
      this(why, path.toString());
    }

    public IOError(String why, String path) {
      super("Target file: \""+ path +"\", "+ why);
      this.path = path;
    }

    public String getPath() {
      return path;
    }
  }


  static public class NotFound extends IOError {
    public NotFound(String fileName) {
      super("Not Found", fileName);
    }
  }


  /**
   * 调用打开文件的函数的路径是一个目录时抛出异常
   */
  static public class ISDirectory extends IOError {
    public ISDirectory(Path path) {
      super("Is Directory", path);
    }
    public ISDirectory(String path) {
      super("Is Directory", path);
    }
  }


  static public class Killed extends XBosonException {
    public Killed(String msg) {
      super(msg);
    }
  }


  static public class Remote extends XBosonException {
    public Remote(Throwable throwable) {
      super(throwable);
    }
  }


  static public class TokenTimeout extends XBosonException {
    public TokenTimeout() {
      setCode(21327);
    }
  }


  static public class SyntaxError extends XBosonException {
    /**
     * 创建语法错误异常
     * @param lang 语言名称
     * @param code 出错代码
     * @param th 底层错误
     */
    public SyntaxError(String lang, String code, Throwable th) {
      super(th +"; <BEGIN-"+ lang +"::>"+
              code + "<::END-"+ lang +">");
    }
  }
}
