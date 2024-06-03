////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午5:39:34
// 原始文件路径: xBoson/src/com/xboson/test/TestLog.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.log.Level;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.log.writer.FileAndConsoleOut;
import com.xboson.log.writer.TestOut;


public class TestLog extends Test {

	public void test() throws Exception {
		LogFactory lf = LogFactory.me();
		msg("This is SHOW: 1,2,3,4,5,6,7");
		
		Log log = LogFactory.create();
		log.setLevel(Level.ALL);
		log.debug("display debug", 1);
		log.info("display info", 2);
		log.error("display error", 3);
		log.warn("display", "warn", 4);
		log.fatal("display", "fatal", 5);

		lf.setWriter(new FileAndConsoleOut());

    log.setLevel(Level.INHERIT);
		LogFactory.setLevel(Level.ERR);
		log.debug("!!! not display", 11);
		log.error("display when set level", 6);
		
		log.setLevel(Level.FATAL);
		log.error("!!! not display", 12);
		log.fatal("display when change level", 7);
		
		LogFactory.setLevel(Level.ALL);

		lf.setWriter(new TestOut());
	}


	public static void main(String[] s) {
	  new TestLog();
  }
}
