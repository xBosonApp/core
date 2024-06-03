////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 上午10:17:30
// 原始文件路径: xBoson/src/com/xboson/test/TestSession.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.been.NameCache;
import com.xboson.been.ResponseRoot;
import com.xboson.util.AES;
import com.xboson.util.SessionID;

import javax.swing.*;

public class TestWebContainer extends Test {


	public void test() throws Throwable {
    test_session();
    test_response_root();
	}


  public void test_response_root() throws Throwable {
	  sub("Test response root");

    eq(NameCache.toNoneJavaName("TestWebContainer"),
            "test_web_container", "bad");

    msg("formatClassName xBoson --", NameCache.formatClassName(this.getClass()));
    msg("formatClassName Java   --", NameCache.formatClassName(String.class));
    msg("formatClassName Swing  --", NameCache.formatClassName(JFrame.class));
    msg("toNoneJavaName         --", NameCache.toNoneJavaName("TestWebContainer"));
  }


	public void test_session() throws Throwable {
		sub("Test session");
		byte[] ps = AES.aesKey("abc");
		msg("getLength:" + ps.length);
		
		String sessionid = SessionID.generateSessionId(ps);
		msg("Session ID: " + sessionid);
		
		if (!SessionID.checkSessionId(ps, sessionid)) {
			throw new Exception("wrong");
		}
		
		if (SessionID.checkSessionId(ps, "f"+sessionid)) {
			throw new Exception("Fail: not checked bad sessionid");
		}
	}


	public static void main(String[] a) {
	  new TestWebContainer();
  }
}
