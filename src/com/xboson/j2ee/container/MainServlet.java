////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月1日 上午11:26:10
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/j2ee/container/MainServlet.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.container;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xboson.been.CallData;
import com.xboson.been.XBosonException;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;


/**
 * 实现了一个轻量级路由
 *
 * @see com.xboson.init.Startup 配置到容器
 */
public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = 5854315900895352428L;
	private final Log log;


	public MainServlet() {
    log = LogFactory.create("service-route");
  }


  /**
	 * 参数通过 URL 传递
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		CallData cd = new CallData(req, resp);
		XService sv = UrlMapping.getService(cd.url);
		
		if (sv == null) {
			throw new XBosonException.NoService(cd.url.getName());
		}

		if (sv.needLogin()) {
      sv.checkLoging(cd);
    }

    log.debug(cd.url.getName(), sv.getClass().getName());

		try {
      sv.service(cd);

      if (!cd.xres.isResponsed()) {
        cd.xres.responseMsg("unknow return", 999);
      }
    } catch(XBosonException e) {
			throw e;
		} catch(RuntimeException r) {
			throw r;
    } catch(Exception e) {
		  throw new XBosonException(e);
    }
  }


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

}
