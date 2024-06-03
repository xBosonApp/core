////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午3:05:53
// 原始文件路径: xBoson/src/com/xboson/test/TestUrl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.been.UrlSplit;

public class TestUrl extends Test {

  final String a = "/xboson";
  final String b = "/app/{app id}/{org id}/{module id}/{api name}";


	public void test() throws Exception {
    test1();
    test2();
	}


	public void test2() {
	  sub("Test url without slash");

    UrlSplit url = new UrlSplit("/a/b/c");
    msg("All:", url);
    url.withoutSlash(true);
    msg("without slash:", url);

    eq(url.getName(), "a", "bad");
    eq(url.next(), "b", "bad");
    eq(url.next(), "c", "bad");

    new Throws(UrlSplit.URLParseException.class) {
      @Override
      public void run() throws Throwable {
        url.next();
      }
    };
  }


	public void test1() {
	  sub("Test url normal");

    UrlSplit url = new UrlSplit(a + b);
    msg("All:", url);

    eq(url.getName(), a, "name bad");
    eq(url.getLast(), b, "last bad");

    String n = url.next();
    eq(n, "/app", "next fail");
    msg("next:", n);

    n = url.next();
    msg("next:", n);

    n = url.next();
    eq(n, "/{org id}", "next fail");
    msg("next:", n);

    n = url.next();
    msg("next:", n);

    n = url.next();
    msg("next:", n);

    new Throws(UrlSplit.URLParseException.class) {
      @Override
      public void run() throws Throwable {
        url.next();
      }
    };
  }


	public static void main(String[] a) {
		new TestUrl();
	}
}
