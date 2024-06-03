////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 22017年11月2日 下午12:28:55
// 原始文件路径: xBoson/src/com/xboson/util/SessionID.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


public class SessionID {

	private static final Random r = new SecureRandom();
	private static final byte[] sign = "J.yanming".getBytes();
	private static final int sessionLength = 164 + sign.length;
	
	
	/**
	 * 在请求中寻找指定名称的 cookie
	 */
	public static Cookie getCookie(String name, HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		
		for (int i=0; i<cookies.length; ++i) {
			if (cookies[i].getName().equalsIgnoreCase(name)) {
				return cookies[i];
			}
		}
		return null;
	}
	
	
	public static String generateSessionId(final byte[] password)
					throws ServletException {
		byte[] data = new byte[sessionLength];
		r.nextBytes(data);
		
		int begin = data.length - sign.length;
		for (int i=begin; i<data.length; ++i) {
			data[i] = sign[i - begin];
		}
		
		data = AES.Encode(data, password);
		String ret = Base64.getEncoder().encodeToString(data);
		return ret;
	}
	
	
	/**
	 * 检查 session 是否安全, 错误的 sid 会抛出异常
	 * @throws ServletException
	 */
	public static boolean checkSessionId(byte[] ps, String sid)
					throws ServletException {
		try {
			byte[] data = Base64.getDecoder().decode(sid);
			data = AES.Decode(data, ps);
			
			int begin = data.length - sign.length;
			for (int i=begin; i<data.length; ++i) {
				if (data[i] != sign[i - begin])
					return false;
			}
			
			return true;
		} catch(Exception e) {
			return false;
		}
	}
}
