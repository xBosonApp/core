////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月1日 上午11:08:21
// 原始文件路径: xBoson/src/com/xboson/test/Test.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.been.Config;
import com.xboson.been.JsonHelper;
import com.xboson.init.Touch;
import com.xboson.j2ee.container.Processes;
import com.xboson.j2ee.emu.EmuJeeContext;
import com.xboson.j2ee.emu.EmuServletContext;
import com.xboson.j2ee.emu.EmuServletContextEvent;
import com.xboson.log.Level;
import com.xboson.log.LogFactory;
import com.xboson.log.writer.TestOut;
import com.xboson.sleep.ISleepwalker;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;

import java.io.*;
import java.util.*;


/**
 * 通过实现该类, 导入通用测试框架,
 * 这里的方法都没有考虑性能, 不要在非测试环境中使用.
 *
 * 子类可以实现一个 main() 以允许测试用例单独运行.
 * 测试用例将节点 ID 改为 `18`
 *
 * @see Test#CLUSTER_NODE_ID
 */
public class Test implements IConstant {
	public static final String line =
">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";

	public static short CLUSTER_NODE_ID = 18;

	private static int failcount = 0;
  private static int unitCount = 0;
  private static int subCount = 0;
  private static int warnCount = 0;
	private static long time = 0;
	private static String unitname;
	private static String subname;
	private static boolean centralized = false;
	private static List<Warn> warnList = new ArrayList<>();

	

	public static void main(String[] args) throws Throwable {
	  new Test(true);
	}


  public Test() {
	  if (centralized) return;
    Config cf = SysConfig.me().readConfig();
    cf.clusterNodeID = CLUSTER_NODE_ID;
	  _test(new Test[] { this });
  }


  /**
   * 感知并运行 com.boson.test 包下所有测试用例
   * @param test_all
   * @throws Throwable
   */
  private Test(boolean test_all) throws Throwable {
    centralized = true;

    Set<Class> allclass = Tool.findPackage(Test.class);
    Iterator<Class> it = allclass.iterator();
    Test[] test = new Test[allclass.size()];
    int i = 0;

    while (it.hasNext()) {
      Class c = it.next();
      try {
        test[i] = (Test) c.newInstance();
        ++i;
      } catch(ClassCastException skip) {
        msg("SKIP Test", c);
      }
    }
    _test(test);
  }

	
	/**
	 * 子类重写该方法, 并且不要调用
	 */
	public void test() throws Throwable {
  }


	@SuppressWarnings("rawtypes")
	private final void _test(Test[] cl) {
    Processes.me().init(new EmuServletContext());
    Touch.me();
    LogFactory.me().setWriter(new TestOut());
    LogFactory.setLevel(Level.ALL);

		StringBufferOutputStream strerr = new StringBufferOutputStream();
		PrintStream buf = new PrintStream(strerr);

    EmuJeeContext jc = new EmuJeeContext();
    jc.appContext(() -> runAllTest(cl, buf));

		// 通知系统进入销毁流程
		Touch.exit();

		// 打印出积累的错误消息
		System.out.println("\u001b[;31m" + strerr + "\u001b[m");

		// 打印警告
    if (warnList.size() > 0) {
      for (Warn w : warnList)
        System.out.println("\u001b[90;47m"+ w +"\u001b[m");
      warnList.clear();
    }

		// 打印结果
		if (failcount > 0) {
			System.out.print("\n\u001b[;31m>>>>>>>>>> Over, Get "+ failcount +" fail");
		} else {
			System.out.print("\n\u001b[;32m>>>>>>>>>> Over, All Passed");
		}
		if (warnCount > 0) {
		  System.out.print(", Has "+ warnCount +" warning");
    }
    System.out.println("\u001b[m");
    printRunningThread();
	}


	private void runAllTest(Test[] cl, PrintStream buf) {
    for (int i=0; i<cl.length; ++i) {
      if (cl[i] == null) continue;
      try {
        unit(cl[i].getClass().getName());
        Test t = cl[i];
        t.test();
        success();
      } catch(Throwable e) {
        fail(cl[i].getClass().getName());
        buf.println("\n" + line);
        buf.println("####\t" + cl[i].getClass().getName());
        buf.println(line);
        e.printStackTrace(buf);
      }
    }
  }


	public static void success() {
	  success(unitname);
  }

	
	public static void success(Object ...o) {
		System.out.println("\u001b[;32m  Success: " + _string(o) + "\u001b[m");
	}


  /**
   * 提示失败消息, 并使失败计数 +1;
   */
	public static void fail(Object ...o) {
		red("  Fail: " + _string(o));
		++failcount;
	}


	public static void red(Object ...o) {
    System.out.println("\u001b[;31m" + _string(o) + "\u001b[m");
  }


  public static void warn(Object ...o) {
	  Warn w = new Warn(_string(o));
    warnList.add(w);
    System.out.println("\u001b[;33m" + w.msg + "\u001b[m");
    ++warnCount;
  }


  public static void dark(Object ...o) {
    System.out.println("\u001b[;90m" + _string(o) + "\u001b[m");
  }


	/**
	 * 开始一条测试用例, 原先是 public, 现在使用 sub 来替换.
	 */
	private static void unit(String name) {
    ++unitCount;
		System.out.print("\u001b[90;103m\n ["+ unitCount
            +"]    ############ [ Test "
            + name + " ] ############\n\n\u001b[m");
    unitname = name;
    subCount = 0;
	}


  /**
   * 一个测试用例中的子项测试
   * @param msg
   */
	public static void sub(Object ...msg) {
    ++subCount;
    subname = _string(msg);
    System.out.print("\n\u001b[7;35m  ("+ unitCount +"-"+ subCount
            +") " + subname + "\n\u001b[m");
  }


	/**
	 * 显示消息
	 */
	public static void msg(Object ...o) {
		System.out.println("\u001b[;36m    " + _string(o) + "\u001b[m");
	}


  public static String _string(Object [] arr) {
    if (arr == null) return "";
    if (arr.length == 1) return String.valueOf(arr[0]);

    StringBuilder out = new StringBuilder();
    for (int i=0; i<arr.length; ++i) {
      out.append(arr[i]);
      out.append(' ');
    }
    return out.toString();
  }


	/**
	 * 如果 o == false 则抛出异常
	 */
	public static void ok(boolean o, String msg) {
		if (!o) {
			throw new RuntimeException(msg);
		} else {
		  msg("OK " + msg);
    }
	}


	/**
	 * 如果 a, b 不相同则抛出异常
	 */
	public static void eq(Object a, Object b, String msg) {
	  if (a == b || a.equals(b)) {
      return;
    }

	  throw new AssertionError(msg + " not equals\n\tObject 1: '" + a +
            "'\n\tObject 2: '" + b + "'");
  }


  /**
   * 显示内存状态
   */
  public static void memuse() {
		int mb = 1024*1024;
		Runtime runtime = Runtime.getRuntime();
		msg("##### Heap utilization statistics [MB] #####");
		msg("  Used Memory:" 
			+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
		msg("  Free Memory:" + runtime.freeMemory() / mb);
		msg("  Total Memory:" + runtime.totalMemory() / mb);
		msg("  Max Memory:" + runtime.maxMemory() / mb);
	}


  /**
   * 设定开始时间
   */
	public static long beginTime() {
		return time = new Date().getTime();
	}


  /**
   * 用设定的开始时间和调用此函数的结束时间, 计算使用时间
   */
	public static long endTime(Object ...msg) {
		long u = (new Date().getTime() - time); 
		sub(_string(msg), "Used Time", u, "ms");
		return u;
	}


	public static String randomString(int byteLength) {
	  return Tool.randomString(byteLength);
	}


	public static byte[] randomBytes(int byteLength) {
	  return Tool.randomBytes(byteLength);
  }


  public static void printArr(byte [] arr) {
	  msg(Arrays.toString(arr));
  }


  public static void printCode(String code) throws IOException {
    Reader in = new StringReader(code);
    BufferedReader br = new BufferedReader(in);
    String line;
    int count = 1;
    do {
      line = br.readLine();
      if (line != null) {
        String c = (count<1000 ? (count < 100 ? (count < 10?
                "   " :"  ") :" ") :"")+ count;

        msg("/* ", c, " */   ", line);
        ++count;
      }
    } while(line != null);
  }


  public static void Throws(Class<? extends Throwable> _throws, TRun r) {
	  new Throws(_throws) {
      public void run() throws Throwable {
        r.run();
      }
    };
  }



  static public abstract class TData extends JsonHelper
					implements ISleepwalker, Serializable {
    public int a = 0;
    public int b = 0;
    public long c = 0;
    public String d = "not_init";
    public String id = "not_init_id";

    public void change() {
      a = (int) (Math.random() * 100);
      b = (int) (Math.random() * 1000 + 100);
      c = (int) (Math.random() * 10000 + 1000);
      d = Test.randomString(100);
    }

    public boolean equals(Object _o) {
      if (_o instanceof TData) {
        TData o = (TData) _o;
        return a == o.a && b == o.b && c == o.c
                && d.equals(o.d);
      }
      return false;
    }

    public String toString() {
      return "[ a=" + a + " b=" + b + " c=" + c + " d=" + d + " ]";
    }
  }


  /**
   * 专门用来测试 JSON 和序列化的数据对象
   */
  static public class TestData extends TData {
    @Override
    public String getid() {
      return "null";
    }
  }


  /**
   * 打印非守护线程的堆栈
   */
  static void printRunningThread() {
    Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
    Iterator<Thread> it = all.keySet().iterator();
    Thread myself = Thread.currentThread();
    int activeCount = 0;

    StringBufferOutputStream strbuf = new StringBufferOutputStream();
    PrintStream buf = new PrintStream(strbuf);

    while (it.hasNext()) {
      Thread t = it.next();
      buf.println("\u001b[;33m\nThread: " + t + "\u001b[m");

      if (t.isDaemon() == false && t != myself) {
        StackTraceElement[] ste = all.get(t);
        for (int i = 0; i < ste.length; ++i) {
          buf.println("\u001b[;31m\t" + ste[i] + "\u001b[m");
          ++activeCount;
        }
      } else{
        buf.println("\tSystem or Daemon Thread.");
      }
    }

    if (activeCount > 0) {
      System.out.println("Running Thread: " + activeCount);
      System.out.println(strbuf);
    }
  }


  /**
   * 抛出异常才认为是正确的行为, 继承该类实现 run()
   */
  static public abstract class Throws implements TRun {
    /**
     * 正确运行时抛出 _throws 类型的异常
     */
    public Throws(Class<? extends Throwable>  _throws) {
      try {
        run();
      } catch(Throwable t) {
        if (_throws.isAssignableFrom(t.getClass())
                || t.getClass() == _throws) {
          msg("OK, Catch Error:", t);
          return;
        }
      }
      throw new RuntimeException("cannot throw Throwable: " + _throws);
    }
  }


  interface TRun {
    void run() throws Throwable;
  }


  static private class Warn {
    String unit;
    String sub;
    Date time;
    String msg;

    Warn(String msg) {
      this.unit = unitname;
      this.sub  = subname;
      this.time = new Date();
      this.msg  = msg;
    }

    public String toString() {
      return String.format(
              "[ Warning AT [%s] - [%s] ON %tT ]\n\t%s\n",
              unit, sub, time, msg);
    }
  }
}
