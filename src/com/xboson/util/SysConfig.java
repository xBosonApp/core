////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月3日 下午12:44:17
// 原始文件路径: xBoson/src/com/xboson/util/SysConfig.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.squareup.moshi.JsonWriter;
import com.xboson.been.Config;
import com.xboson.event.GlobalEventBus;
import com.xboson.event.Names;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.script.lib.Path;
import com.xboson.util.config.DefaultConfig;
import com.xboson.util.config.IConfigSerialization;
import com.xboson.util.config.SerialFactory;
import okio.Buffer;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;


public class SysConfig {

  private static final String UPDATE = "update-config.yaml";
	private static SysConfig instance;

	private Log log = LogFactory.create("SysConfig");
	private String homepath;
	private Config config;
	private boolean readed;
	private IConfigSerialization serial;

	
	private SysConfig() {
		initHomePath();
		serial = SerialFactory.get();
		readed = false;
		config = new Config(homepath);

    DefaultConfig.setto(config);
		checkConfigFiles();
		readConfig(config.configFile);

		if (!readed) {
			// 日志处于不可用状态
			System.out.println("System Config Fail, exit() !");
			System.exit(1);
		}

		GlobalEventBus.me().emit(Names.config, config);

		log.info("Initialization Success");
	}
	
	
	public synchronized static SysConfig me() {
		if (instance == null) {
			instance = new SysConfig();
		}
		return instance;
	}
	
	
	private void initHomePath() {
		homepath = System.getenv("HOME");
		if (homepath != null) return;
    homepath = System.getProperty("user.home");
    if (homepath != null) return;
		homepath = System.getenv("HOMEPATH");
		if (homepath != null) return;
		homepath = System.getenv("APPDATA");
		if (homepath != null) return;
		homepath = System.getenv("LOCALAPPDATA");
		if (homepath != null) return;
		homepath = System.getenv("ALLUSERSPROFILE");
		
		if (homepath == null) {
			System.err.println(System.getProperties());
			throw new RuntimeException("Cannot init HOME path");
		}
	}
	
	
	public String getHomePath() {
		return homepath;
	}
	
	
	public Config readConfig(String config_file) {
		if (!readed) {
			String str = null;
			try {
				str = Tool.readFromFile(config_file).toString();
				setConfigUseString(str);
				log.info("Read Config from", config_file);

			} catch(Exception e) {
        System.err.println("Config file context: " + str);
				System.err.println("Read LoginUser Config:" + e);
			}
		}
		return config;
	}


	public Config readConfig() {
		return readConfig(config.configFile);
	}


	private void setConfigUseString(String str) throws IOException {
		Config run = serial.convert(str);

		if (configNeedUpdate(run)) {
		  log.warn("Configuration files need to be upgraded",
              run.configVersion, "<", config.configVersion);

		  String configFile = run.configFile;
		  run.configFile    = Path.me.join(run.configPath, UPDATE);
		  run.configVersion = Config.VERSION;
		  generateDefaultConfigFile(run);
		  run.configFile    = configFile;
    }

		run.setHome(config.home);
		config = run;
		readed = true;
	}


  /**
   * 如果配置文件中的配置版本比较低返回 true
   * @param nowcfg 从配置文件读取的配置
   */
	private boolean configNeedUpdate(Config nowcfg) {
	  try {
	    // 这里会抛出一大坨错误
      int a = Integer.parseInt(config.configVersion.split(".")[2]);
      int b = Integer.parseInt(nowcfg.configVersion.split(".")[2]);
      return b < a;
    } catch (Exception e) {
	    return true;
    }
  }

	
	public void checkConfigFiles() {
		mkdirNotexists(config.configPath);
		mkdirNotexists(config.logPath);
		File cfile = new File(config.configFile);
		
		if (!cfile.exists()) {
			generateDefaultConfigFile(config);
		}
	}


	public void generateDefaultConfigFile(Config config) {
		try {
			File cfile = new File(config.configFile);
			log.info("Generate config file", cfile);

			String json = serial.convert(config);

			try (FileWriter out = new FileWriter(cfile)) {
				out.write(json);
			}

			// 如果配置文件是程序生成的, 则认为配置已经读取完成.
			readed = true;
		} catch(Exception e) {
			log.error(e.getMessage());
		}
	}
	
	
	private void mkdirNotexists(String dirname) {
		File dir = new File(dirname);
		if (!dir.exists()) {
			dir.mkdirs();
			log.info("Make dir", dirname);
		}
	}


	/**
	 * 基于并发来设计的缓存池, 过多的对象利用不上.
	 */
	public static GenericObjectPoolConfig defaultPoolConfig() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(-1);
		config.setMaxIdle(1);
		config.setEvictorShutdownTimeoutMillis(100);
		config.setBlockWhenExhausted(false);
		return config;
	}


	/**
	 * @see #defaultPoolConfig()
	 */
	public static GenericKeyedObjectPoolConfig defaultKeyPoolConfig() {
		GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
		config.setMaxTotalPerKey(-1);
		config.setMaxIdlePerKey(1);
		config.setEvictorShutdownTimeoutMillis(1);
		config.setBlockWhenExhausted(false);
		return config;
	}
}
