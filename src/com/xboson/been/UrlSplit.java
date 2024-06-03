////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午3:00:14
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/UrlSplit.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import javax.servlet.http.HttpServletRequest;

/**
 * 将请求 url 分解, 用于路由映射.
 * 当入参为 /a/b/c
 * 	 name = /a
 *   last = /b/c
 */
public class UrlSplit implements IBean {

  private static final String DEFAULT_ERR
          = "url parse is complete, no more parameters.";
	
	/** url 中的首个路径 */
	private String name;
	/** url 中的后续路径 */
	private String last;
	private String errMsg = DEFAULT_ERR;
	private boolean withoutSlash = false;


  /**
   * 从请求 url 中初始化.
   */
	public UrlSplit(HttpServletRequest req) {
		String cp = req.getContextPath();
		String rq = req.getRequestURI();
    split( rq.substring(cp.length(), rq.length()) );
	}
	
	
	public UrlSplit(String url) {
    split(url);
	}


	private UrlSplit() {}


  public void setErrorMessage(String errMsg) {
	  this.errMsg = errMsg;
  }


  public UrlSplit clone() {
	  UrlSplit _clone = new UrlSplit();
    _clone.name = name;
    _clone.last = last;
    _clone.errMsg = errMsg;
    _clone.withoutSlash = withoutSlash;
	  return _clone;
  }


  /**
   * 返回的 name 中不含有开头的 '/' 符号则设置为 true
   * @param isit
   */
  public void withoutSlash(final boolean isit) {
	  if (isit != withoutSlash) {
	    if (withoutSlash) {
	      name = "/" + name;
	      if (last != null) last = "/" + last;
      } else {
	      name = name.substring(1);
        if (last != null) last = last.substring(1);
      }
    }
    withoutSlash = isit;
  }


	/**
   * 使用最靠近左面的 '/' 切分 s, 前部分放入 name, 后部分放入 last
	 * 这会改变自身的数据, 并返回 name
   *
   * @throws URLParseException
	 */
	String split(String s) {
		if (s == null) 
			throw new URLParseException(errMsg);

		try {
		  //
		  // 一段时间之后肯定看不懂...
      //
      int a = 0, w = 0;
      int w2 = withoutSlash ? 1 : 0;

      if (s.charAt(0) == '/') {
        a = 1;
        w = w2;
      } else {
        w = 0;
      }
      a = s.indexOf('/', a);

      if (a >= 0) {
        name = s.substring(w, a);
        last = s.substring(a+w2, s.length());
      } else {
        name = s;
        last = null;
      }
      return name;
    } catch(Exception e) {
		  throw new URLParseException(errMsg, e);
    }
	}


  /**
   * 将 name 清除, 拆分 last 为新的 name/last,
   * 这会改变自身数据.
   */
	public String next() {
	  return split(last);
  }
	
	
	public String toString() {
		return "[" + name + " + " + last + "]";
	}
	
	
	public String getName() {
		return name;
	}
	
	
	public String getLast() {
		return last;
	}


  /**
   * 当路径无法继续拆分, 会抛出这个异常
   */
	public class URLParseException extends XBosonException {
    public URLParseException(String s) {
      super(s);
    }
    public URLParseException(String s, Throwable throwable) {
      super(s, throwable);
    }
  }
}
