////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-4 上午8:26
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/AppContext.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app;

import com.xboson.app.lib.ModuleHandleContext;
import com.xboson.app.reader.ForDevelopment;
import com.xboson.app.reader.ForProduction;
import com.xboson.auth.IAWho;
import com.xboson.been.*;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.distributed.ProcessManager;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.log.slow.RequestApiLog;
import com.xboson.rpc.ClusterManager;
import com.xboson.script.EventFlag;
import com.xboson.script.IScriptEventListener;
import com.xboson.script.IVisitByScript;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;


/**
 * 作为全局脚本的入口, 维护所有运行着的沙箱/应用池
 */
public final class AppContext implements
        IConstant, IScriptEventListener, IVisitByScript {

  /** 在线程超过这个运行时间后, 降低运行优先级, 毫秒 */
  public static final long LOW_CPU_TIME = 2 * 60 * 1000;
  public static final String API_KILL_MSG = "Api Process is Killed";

  private static AppContext instance;
  private AppPool production;
  private AppPool development;
  private Map<String, String> app2org;
  private RequestApiLog apilog;
  private ProcessManager pm;
  private String nodeID;
  private EventFlag seflag;
  private Map<String, ThreadLocalData> crossThread;
  private Log log;


  private AppContext() {
    log         = LogFactory.create("sc-core-context");
    production  = new AppPool(new ForProduction());
    development = new AppPool(new ForDevelopment());
    apilog      = new RequestApiLog();
    pm          = new ProcessManager();
    nodeID      = ClusterManager.me().localNodeID();
    seflag      = EventFlag.me;
    crossThread = new ConcurrentHashMap();
    rebuildAppOrgMapping();
  }


  /**
   * 该方法支持嵌套请求, 前一个请求的参数会被保留在 ThreadLocalData.nestedCall 中.
   * App Mod Api 参数都被转换为小写.
   */
  public void call(ApiCall ac) {
    long begin = System.currentTimeMillis();
    Throwable fail = null;
    String apiPath = ApiPath.getPath(ac);
    log.debug("Call::", apiPath);

    try {
      ThreadLocalData tld = createLocalData(ac);
      make_extend_parameter(tld);

      //
      // 跨机构调用共享 APP 中的 api, 此时 org 可以是另一个机构.
      // 此时 api 以 root 的数据库权限启动, 而参数中的 org 仍然是原先机构的 id.
      // (实现不完整: 当其他机构共享 app 会出现权限不够的问题)
      //
      // 即使 org 是其他机构, 运行的仍然是平台机构中的 api, 所以不会有代码越权访问资源,
      // 必须保证平台机构 api 逻辑安全.
      //
      String orgWithApp = app2org.get(ac.app);
      if (Tool.isNulStr(orgWithApp)) orgWithApp = SYS_ORG;
      if (! orgWithApp.equals(ac.org)) {
        ac.org = orgWithApp;
        tld.replaceOrg = true;
      }

      XjOrg org = chooseAppPool(tld).getOrg(ac.org);
      XjApp app = org.getApp(ac.app);
      app.run(ac.call, ac.mod, ac.api, tld);

    } catch (XBosonException x) {
      fail = x;
      throw x;

    } catch (Exception e) {
      fail = e;
      throw new XBosonException(e);

    } catch (ThreadDeath dead) {
      log.warn(API_KILL_MSG, apiPath, Tool.miniStack(dead, 6));
      ac.makeLastMessage(API_KILL_MSG);
      fail = new TimeoutException(API_KILL_MSG);

    } finally {
      ThreadLocalData current = pm.get();
      exitCrossContext(current);

      if (current.nestedCall != null) {
        pm.start(current.nestedCall);
      } else {
        pm.exit();
        ModuleHandleContext.exitThread();
        apilog.log(ac, System.currentTimeMillis() - begin, fail);
      }
    }
  }


  /**
   * 该方法仅用于测试
   */
  public void call(ApiCall ac, Runnable r) {
    try {
      ThreadLocalData tld = createLocalData(ac);
      make_extend_parameter(tld);
      r.run();
    } finally {
      pm.exit();
    }
  }


  private AppPool chooseAppPool(ThreadLocalData tld) {
    ApiTypes type = ApiTypes.of(tld.ac);
    tld.__dev_mode = type;

    //
    // ApiTypes 中不适合存放 AppPool 的实例.
    //
    switch (type) {
      case Development:
        return development;

      case Production:
        return production;
    }
    throw new XBosonException.NotExist("Unknow type " + type);
  }


  private ThreadLocalData createLocalData(ApiCall ac) {
    ThreadLocalData tld = new ThreadLocalData();
    tld.who     = ac.call.sess.login_user;
    tld.orgid   = ac.org;
    tld.ac      = ac;
    tld.beginAt = System.currentTimeMillis();

    ThreadLocalData previous = pm.getMaybeNull();
    if (previous != null) {
      tld.nestedCall = previous;
    }
    pm.start(tld);
    return tld;
  }


  private void make_extend_parameter(ThreadLocalData tld) {
    Map<String, Object> ex = tld.ac.exparam;
    if (ex == null) {
      tld.ac.exparam = ex = new HashMap<>();
    }
    tld.exparam = ex;
    ex.put("org", tld.ac.org);
    ex.put("app", tld.ac.app);
    ex.put("mod", tld.ac.mod);
    ex.put(REQUEST_ID, Tool.uuid.ds());
  }


  public synchronized void rebuildAppOrgMapping() {
    Config cf = SysConfig.me().readConfig();
    Map<String, String> mapping = new HashMap<>();

    for (int i=0; i<cf.shareAppList.length; ++i) {
      mapping.put(cf.shareAppList[i], SYS_ORG);
    }

    try (SqlResult sr = SqlReader.query("mdm_org", cf.db)) {
      ResultSet orgs = sr.getResult();

      while (orgs.next()) {
        String orgid = orgs.getString(1);
        String selectApp = "SELECT appid FROM `"+ orgid +"`.sys_apps;";
        ResultSet apps = sr.query(selectApp).getResult();

        while (apps.next()) {
          String appid = apps.getString(1);
          //
          // 兼容: 画面中使用小写, 数据库中使用大写
          //
          mapping.put(appid.toLowerCase(), orgid);
          mapping.put(appid.toUpperCase(), orgid);
          mapping.put(appid, orgid);
        }
      }

      log.debug("Rebuild App to Org Mapping.");
    } catch (SQLException e) {
      log.error("rebuildAppOrgMapping", e);
    } finally {
      app2org = mapping;
    }
  }


  /**
   * 返回扩展请求参数
   */
  public Map<String, Object> getExtendParameter() {
    return pm.get().exparam;
  }


  /**
   * 当前运行时类型 (开发/生产)
   */
  public ApiTypes getRuntimeType() {
    return pm.get().__dev_mode;
  }


  /**
   * HTTP 原始请求时的机构参数, 在运行后 HTTP 中的参数可以被替换为机构 orgid
   */
  public String originalOrg() {
    return pm.get().orgid;
  }


  /**
   * 返回当前 api 的抽象文件路径
   */
  public String getCurrentApiPath() {
    return pm.get().getApiPath();
  }


  /**
   * 返回 api 类型
   */
  public ApiTypes getApiModeType() {
    return pm.get().__dev_mode;
  }


  /**
   * 如果在 app 上下文中返回 true;
   * 在上下文中意味着可以安全的调用上下文相关函数而不会抛出异常.
   */
  public boolean isInContext() {
    return pm.getMaybeNull() != null;
  }


  /**
   * 返回当前运行脚本已经加密的代码
   */
  public String getOriginalApiCode() {
    return pm.get().originalCode;
  }


  /**
   * 返回当前运行脚本已经加密的代码的 hash
   */
  public String getOriginalApiHash() {
    return pm.get().originalHash;
  }


  /**
   * 保存 app 上下文到句柄中, 通过这个句柄可以恢复上下文;
   * 当主线程退出会删除所有关联到其他线程的上下文;
   * 当前线程必须在 app 上下文中, 否则抛出 IllegalStateException
   */
  public String saveContext() {
    ThreadLocalData ctx = pm.get();
    String handle;
    do {
      handle = Tool.randomString(24);
    } while (crossThread.containsKey(handle));

    if (ctx.__cross_handle == null) {
      ctx.__cross_handle = new ContextHandle(Thread.currentThread());
    }
    ctx.__cross_handle.handle.add(handle);
    crossThread.put(handle, ctx);
    return handle;
  }


  /**
   * 使用句柄恢复保存的上下文到当前线程中, 当主线程退出会删除所有关联到其他线程的上下文;
   * 如果当前线程已经在上下文中会抛出 IllegalStateException,
   * 如果句柄无效会抛出 IllegalArgumentException.
   */
  public void restoreContext(String handle) {
    if (isInContext()) {
      throw new IllegalStateException(
              "The context already exists and cannot be restored");
    }
    ThreadLocalData ctx = crossThread.get(handle);
    if (ctx == null) {
      throw new IllegalArgumentException("Invalid handle to restore context");
    }
    ctx.__cross_handle.related.add(Thread.currentThread());
    pm.start(ctx);
  }


  /**
   * 当主线程退出时调用, 会删除所有关联到其他线程的上下文引用句柄;
   */
  private void exitCrossContext(ThreadLocalData current) {
    if (current.__cross_handle == null)
      return;

    for (String h : current.__cross_handle.handle) {
      crossThread.remove(h);
    }
    for (Thread t : current.__cross_handle.related) {
      pm.exit(t);
    }
    current.__cross_handle = null;
  }


  /**
   * 返回 true 说明当前脚本是通过 require(..) 引入的,
   * 该函数返回后这个标记被重置, 该标记是线程级别的.
   */
  public boolean isRequired() {
    ThreadLocalData tld = pm.get();
    if (tld != null && tld.__is_required) {
      tld.__is_required = false;
      return true;
    }
    return false;
  }


  @Override
  public void on(ScriptEvent event) {
    if (event.flag == seflag.IN_REQUIRE) {
      ThreadLocalData tld = pm.getMaybeNull();
      if (tld != null) tld.__is_required = true;
    }
    else if (event.flag == seflag.SCRIPT_OUT
            || event.flag == seflag.OUT_REQUIRE
            || event.flag == seflag.SCRIPT_PREPARE) {
      ThreadLocalData tld = pm.getMaybeNull();
      if (tld != null) tld.__is_required = false;
    }
  }


  /**
   * 在任何位置都可以安全调用该方法, 返回当前登录的用户,
   * 如果没有用户登录会抛出异常.
   */
  public IAWho who() {
    IAWho r = pm.get().who;
    if (r == null) {
      throw new XBosonException("not login");
    }
    return r;
  }


  /**
   * 当 org 被替换后, 返回 ture.
   * 替换的 org 通过 originalOrg() 可以得到, 只在 sys 机构时发生, 用于 sys 机构中的
   * api 访问其他机构中的表.
   */
  public boolean isReplaceOrg() {
    return pm.get().replaceOrg;
  }


  /**
   * 调用该方法允许该线程被 kill;
   * 在完全初始化之后才调用, 否则会造成多线程共享对象不一致.
   */
  public void readyForKill() {
    pm.get().readyForKill();
  }


  /**
   * 当前 js 环境是嵌套调用的返回 true.
   */
  public boolean isNestedCall() {
    return pm.get().nestedCall != null;
  }


  /**
   * 返回进程管理器
   */
  public ProcessManager getProcessManager() {
    return pm;
  }


  /**
   * 返回当前应用上下文
   */
  public static AppContext me() {
    if (instance == null) {
      synchronized (AppContext.class) {
        if (instance == null) {
          instance = new AppContext();
        }
      }
    }
    return instance;
  }


  /**
   * 线程变量保存包装器, 构造函数在 createLocalData() 中.
   * 该对象的实例会在多个线程中访问.
   * @see #createLocalData(ApiCall)
   */
  public class ThreadLocalData {
    /** 嵌套调用时将前一个调用的数据保存 */
    private ThreadLocalData nestedCall;

    /** @see #getExtendParameter() */
    private Map<String, Object> exparam;

    /** 当前调用用户 */
    private IAWho who;

    /** @see #originalOrg() */
    private String orgid;

    private ApiCall ac;

    /** true: HTTP 参数机构id 已经被替换 */
    private boolean replaceOrg;

    /** 请求开始的时间, ms */
    private long beginAt;

    /** 关联多个线程的句柄列表 */
    private ContextHandle __cross_handle;

    private String originalCode;
    private String originalHash;

    private String __cache_path;
    private ApiTypes __dev_mode;
    private boolean __is_low_priority;
    private boolean __is_ready_for_kill;
    private boolean __is_required;

    /**
     * 返回未被替换的原始参数.
     */
    String getApiPath() {
      if (__cache_path == null) {
        __cache_path = ApiPath.getPath(exparam, ac.api);
      }
      return __cache_path;
    }

    private ThreadLocalData() {}


    void setOriginalApiCode(String code, String hash) {
      this.originalCode = code;
      this.originalHash = hash;
    }


    /**
     * @see AppContext#readyForKill()
     */
    private void readyForKill() {
      __is_ready_for_kill = true;
    }


    public boolean notReadyForKill() {
      return !__is_ready_for_kill;
    }


    public boolean notLowPriority() {
      return !__is_low_priority;
    }


    public void setLowPriority(Thread t) {
      t.setPriority(Thread.MIN_PRIORITY);
      __is_low_priority = true;

      log.debug("Change Process Min Priority, Thread:", t.getId(),
              '"'+ t.getName() +'"', ", API:", getApiPath());
    }


    public long runningTime() {
      return System.currentTimeMillis() - beginAt;
    }


    public void copyTo(PublicProcessData pd) {
      pd.org = ac.org;
      pd.app = ac.app;
      pd.mod = ac.mod;
      pd.api = ac.api;
      pd.beginAt = beginAt;
      pd.runningTime = System.currentTimeMillis() - beginAt;
      pd.nodeID = nodeID;

      if (who instanceof LoginUser) {
        pd.callUser = ((LoginUser) who).userid;
      } else {
        pd.callUser = who.identification();
      }
    }
  }


}
