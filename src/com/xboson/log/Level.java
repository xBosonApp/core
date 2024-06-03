////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午5:19:38
// 原始文件路径: xBoson/src/com/xboson/log/Level.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.log;


/**
 * 枚举的局限太多, 麻烦,
 * 该对象的实例可以直接比较 '=='
 */
public class Level {

	public static final Level DEBUG	= new Level("DEBUG",  5);
	public static final Level INFO	= new Level(" INFO", 10);
	public static final Level WARN  = new Level(" WARN", 15);
	public static final Level ERR		= new Level("ERROR", 20);
	public static final Level FATAL	= new Level("FATAL", 25);
	
	public static final Level ALL		= new Level("ALL",    0);
	public static final Level OFF		= new Level("OFF", 9999);

	/** 当 Log 的等级设置为继承, 则使用全局配置 */
	public static final Level INHERIT = new Level("INHERIT", -1);

	
	private Level(String l, int n) {
		str = l;
		num = n;
	}
	
	
	public String toString() {
		return str;
	}


	public String getName() {
		return str;
	}
	
	
	/**
	 * this - 当前日志级别
	 * @param l - 要检测的日志级别
	 * @return 阻止在当前日志级别显示 l 的日志返回 true
	 */
	public boolean blocking(Level l) {
		return num > l.num;
	}
	
	
	public void checknull() {
	}
	
	
	public static Level find(String name) {
		if (name != null) {
			switch(name.toUpperCase()) {
				case "ON": 
				case "ALL": 	  return ALL;
				case "CLOSE":
				case "OFF":     return OFF;
				case "INFO":	  return INFO;
				case "DEBUG":	  return DEBUG;
				case "WARN":    return WARN;
				case "ERR":
				case "ERROR":   return ERR;
				case "FATAL":   return FATAL;
        case "INHERIT": return INHERIT;
			}
		}
		return ALL;
	}
	
	
	private int num;
	private String str;
}
