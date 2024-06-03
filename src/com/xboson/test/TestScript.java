////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月3日 上午10:43:25
// 原始文件路径: xBoson/src/com/xboson/test/TestScript.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.app.lib.MapImpl;
import com.xboson.app.lib.MongoImpl;
import com.xboson.been.Module;
import com.xboson.fs.script.FileSystemFactory;
import com.xboson.fs.script.IScriptFileSystem;
import com.xboson.fs.node.NodeFileFactory;
import com.xboson.script.*;
import com.xboson.script.lib.Console;
import com.xboson.util.StringBufferOutputStream;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * https://wiki.openjdk.java.net/display/Nashorn/Nashorn+extensions
 */
public class TestScript extends Test {

  private Application app;


	public void test() throws Exception {
    IScriptFileSystem vfs = createVFS();
    app = createBasicApplication(vfs);

//		independent_sandbox();
//		speedtest();
//		memoryuse();
//		closed();
//    t10000();
		hack();
    fullTest();
		error_format();
    test_functions_apply();
	}


	public void error_format() throws Exception {
    try {
      app.run("/error.js");
    } catch (Exception e) {
      Exception x = new JScriptException(e);
      x.printStackTrace();
    }
  }


  public void test_functions_apply() throws Exception {
	  sub("test_functions_apply");
    String code = "Function.call.apply(t.functions, [t, 1, 2]);" +
            "var fn = Function.bind.apply(t.functions, [t]);\n" +
            "Function.call.apply(fn, [null,2,3]);";

    WrapJavaScript ws = new WrapJavaScript(code, "functions_apply");
    app.run(ws);
  }


  /**
   * 综合测试沙箱的功能和安全性
   * @throws Exception - 测试失败抛出异常
   */
	public void fullTest() throws Exception {
    IScriptFileSystem vfs = createVFS();
    Application app = createBasicApplication(vfs);
    app.run("/index.js");
	}


	public static Application createBasicApplication(IScriptFileSystem vfs)
          throws Exception {
    SysModules sysmod = EnvironmentFactory.createDefaultSysModules();
    sysmod.regClass("mongodb", MongoImpl.class);

		IConfigurableModuleProvider nodejs_mod =
						NodeFileFactory.openNodeModuleProvider(sysmod);

    BasicEnvironment env = EnvironmentFactory.createEmptyBasic();
    env.insertConfiger(nodejs_mod);
    env.setEnvObject(Console.class);
    env.setEnvObject(MapImpl.class);
    env.setEnvObject(JsObj.class);

    Object attr = vfs.readAttribute("/index.js");
    msg("File Attribute:", attr);

    Application app = new Application(env, vfs);
    return app;
  }


  public static IScriptFileSystem createVFS() throws URISyntaxException {
    String fsid = "test";
    URL basepath = TestScript.class.getResource("./js/");
    msg("Base path:", basepath);

    FileSystemFactory fsf = FileSystemFactory.me();
    fsf.addLocalFileSystem(basepath, fsid);
    IScriptFileSystem vfs = fsf.open(fsid);
    return vfs;
  }


	/** 安全检查, 已经移动到 js/check-safe.js */
	public void hack() throws Exception {
		Sandbox sandbox = SandboxFactory.create();
		sandbox.bootstrap();
    IEnvironment env = EnvironmentFactory.createBasic();
		env.config(sandbox, app);
		sandbox.freezeGlobal();
		
		Object o = null;
		try {
      eval("console.log=null; console.a='bad', console.log('hello', console.a)");
      eval("if (console.a) throw new Error('console.a is changed.')");
      success("cannot modify java object");
    } catch(Exception e) {
			fail("console module be modified");
		}
		
		eval("try { test.notcall() } catch(e) { test.log(e.stack); }; }), a=19, (function cc(){ return cc");
		success("hack the Script Wrap");
		
		eval("if (typeof a !='undefined') throw new Error('bad');");
		success("cannot make THIS value");
		
		eval("global.a = 11");
		eval("if (global.a != 11) throw new Error('bad global.a')");
		success("can change global val");
		
		o = eval("return Math.abs(-1)");
		success("Math is done " + o);
		
		try {
			eval("setTimeout(function() { console.log('setTimeout !!') }, 1000)");
			fail("has setTimeout function");
		} catch(Exception e) {
			success("setTimeout not exists, done");
		}
		
		WrapJavaScript ws = sandbox.warp("/test/hello.js",
            "console.log(JSON.stringify({a:1}), module, __dirname, __filename)");
		app.run(ws);
		ws.initModule(app);
    Module m = ws.getModule();
		success("module ok, exports: "+ m.exports.getClass());
	}


	private Object eval(String code) {
    WrapJavaScript ws = new WrapJavaScript(code, "test");
    return app.run(ws);
  }
	
	
	@SuppressWarnings("unused")
	private void notcall() {
		throw new RuntimeException();
	}
	
	
	public void closed() throws ScriptException {
		Sandbox a = SandboxFactory.create();
		Object o = a.eval("(function() { return 1; })");
		msg(o + " " + o.getClass());
		success("closed");
	}
	
	
	/**
	 * 实例数量...内存 MB...运行后内存
	 *   100      26       45
	 *  1000     224      331
	 * 10000    2138     2277
	 */
	public void memoryuse() throws ScriptException {
		memuse();
		Sandbox[] a = new Sandbox[10000];
		for (int i=0; i<a.length; ++i) {
			a[i] = SandboxFactory.create();
		}
		memuse();
		for (int i=0; i<a.length; ++i) {
			a[i].eval("a=1");
		}
		memuse();
	}


  /**
   * 创建应用的完整运行环境(包括初始化和引导), 一个运行环境就可以运行一个应用,
   * 一个应用包含所有接口.
   *
   * 创建到 450 个环境消耗时间  Used Time 39483 ms
   * ##### Heap utilization statistics [MB] #####
   *    Used Memory:1300
   *    Free Memory:744
   *    Total Memory:2045
   *    Max Memory:3620
   */
	public void t10000() throws Exception {
		final int count = 500;
		final int showc = (int)(count / 10);

		msg("创建", count, "个完整运行时环境, 测试内存");

		IEnvironment env = EnvironmentFactory.createBasic();

		String fsid = "test";
		FileSystemFactory fsf = FileSystemFactory.me();
    URL basepath = this.getClass().getResource("./js/");
		fsf.addLocalFileSystem(basepath, fsid);
		IScriptFileSystem vfs = fsf.open(fsid);

		List<Application> appList = new ArrayList<>(count);
    beginTime();

		for (int i=0; i<count; ++i) {
      Application app = new Application(env, vfs);
      app.run("/null.js");
      appList.add(app);

      if (i % showc == 0) {
        endTime("\n创建到", i, "个环境消耗时间");
        memuse();
      }
		}
	}
	
	
	public void independent_sandbox() throws Exception {
		Sandbox a = SandboxFactory.create();
		Sandbox b = SandboxFactory.create();
		String c = "function g() { return a; }";
		a.eval(c);
		b.eval(c);
		a.eval("a = 1;");
		b.eval("a = 2;");
		Integer aa = (Integer) a.getGlobalInvocable().invokeFunction("g");
		Integer bb = (Integer) b.getGlobalInvocable().invokeFunction("g");
		if (aa == 1 && bb == 2) {
			success("independent sandbox");
		} else {
			throw new Exception("sandbox value cross");
		}
	}


  /**
   * 1 秒内调用函数次数, Math.sqrt()
   * NODE js :  9099763.1
   * java js : 36106378.8
   *
   * 1 秒内调用函数次数, Math.sin(Math.random());
   * NODE js :  8331048.8
   * java js : 16194020.9
   *
   * @throws ScriptException
   */
	public void speedtest() throws Exception {
		InputStream script1 = getClass().getResourceAsStream("./test-speed.js");
		Sandbox s = SandboxFactory.create();
		s.getBindings().put("console", this);
    StringBufferOutputStream buf = new StringBufferOutputStream();
    buf.write(script1);
    String code = buf.toString();

    for (int cc=0; cc<10; ++cc) {
      s.eval(code);
    }
	}
	
	
	public void printEngines(ScriptEngineManager engine) {
		Iterator<ScriptEngineFactory> it = engine.getEngineFactories().iterator();
		while (it.hasNext()) {
			msg(it.next());
		}
	}
	
	
	public void log(String... s) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<s.length; ++i) {
			buf.append(s[i]);
			buf.append(' ');
		}
		msg(buf.toString());
	}
	
	
	public static void main(String[] args) throws Throwable {
		new TestScript();
	}


	static public class JsObj implements IJSObject {
    public String env_name() {
      return "t";
    }
    public boolean freeze() {
      return false;
    }
    public void init() {}
    public void destory() {}

    public void functions(Object... a) {
      msg("functions call:", Arrays.toString(a));
    }
  }
}
