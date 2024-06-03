////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-16 上午9:39
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/Schedule.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.app.AppContext;
import com.xboson.auth.IAResource;
import com.xboson.auth.PermissionSystem;
import com.xboson.auth.impl.LicenseAuthorizationRating;
import com.xboson.been.*;
import com.xboson.db.ConnectConfig;
import com.xboson.db.IDict;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.event.timer.TimeFactory;
import com.xboson.j2ee.container.XResponse;
import com.xboson.j2ee.emu.BufScrvletOutputStream;
import com.xboson.j2ee.emu.EmuServletRequest;
import com.xboson.j2ee.emu.EmuServletResponse;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.rpc.*;
import com.xboson.util.DateParserFactory;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import okhttp3.*;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;


/**
 * 算法: 将每个任务单独作为一个 RPC 服务导出, 操作时首先在集群中搜索任务.
 */
public class Schedule extends RuntimeUnitImpl implements IAResource {

  private static final String LOG_FILE = "insert-scheduler-log.sql";
  public static final String RPC_PREFIX = "XB.rpc.Schedule.";

  private final String nodeID;
  private final Log log;
  private OkHttpClient hc;
  private ConnectConfig db;


  public Schedule() {
    super(null);
    this.log    = LogFactory.create();
    this.nodeID = ClusterManager.me().localNodeID();
    this.db     = SysConfig.me().readConfig().db;
  }


  @Override
  public String description() {
    return "app.module.schedule.functions()";
  }


  public void start(String id, Map<String, Object> config) throws Exception {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);

    ITask exist = findTask(id);
    if (exist != null) {
      throw new XBosonException(
              "Schedule Task "+ exist.name() +"("+ id +") is running");
    }

    ExportTask task = new ExportTask(id, config);
    RpcFactory.me().bind(task, task.rpcName);
    log.debug("Start", task.rpcName);
  }


  public boolean stop(String id) throws RemoteException {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    try {
      ITask task = findTask(id);
      if (task != null) {
        task.stop();
        return true;
      }
    } catch(Exception e) {
      log.warn("stop", id, e);
    }
    return false;
  }


  public Object info(String id) {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    return findTask(id);
  }


  private ITask findTask(String id) {
    getStr(id, "id");
    RpcFactory fact = RpcFactory.me();
    ClusterManager cm = ClusterManager.me();
    String rpcName = rpcName(id);

    for (String node : cm.list()) {
      try {
        ITask task = (ITask) fact.lookup(node, rpcName);
        return task;
      } catch (Exception e) {}
    }
    return null;
  }


  private String rpcName(String id) {
    return RPC_PREFIX + AppContext.me().originalOrg() +'.'+ id;
  }


  public interface ITask extends IXRemote, IPing {
    int state() throws RemoteException;
    void stop() throws RemoteException;
    String name() throws RemoteException;
    Date nextDate() throws RemoteException;
    String nodeID() throws RemoteException;
  }


  public class ExportTask extends Ping implements ITask {
    private Task real;
    private String rpcName;

    private ExportTask(String id, Map<String, Object> config) {
      real = new Task(id, config);
      rpcName = rpcName(id);
    }

    @Override
    public int state() {
      return real.state;
    }

    @Override
    public void stop() {
      real.stop();
      RpcFactory.me().unbind(rpcName);
    }

    @Override
    public Date nextDate() {
      return real.start_time;
    }

    @Override
    public String nodeID() throws RemoteException {
      return nodeID;
    }

    @Override
    public String name() {
      return real.name();
    }
  }


  private class Task implements IDict {
    private String  schedulenm;         // 名称
    private Date    start_time;         // 开始时间
    private Date    run_end_time;       // 结束时间
    private int     schedule_interval;  // 间隔时间
    private int     schedule_cycle;     // 间隔单位
    private int     run_times;          // 运行次数, -1 不限制
    private String  task_api;           // 任务 api
    private String  id;
    private String  userid;
    private boolean inner_api;
    public  int     state;

    private ConnectConfig db;
    private TimerTask task;
    private Runnable callExecutor;
    

    private Task(String id, Map<String, Object> config)
    {
      schedulenm        = getStr(config, "schedulenm");
      schedule_cycle    = getInt(config, "schedule_cycle");
      run_times         = getInt(config, "run_times");
      task_api          = getStr(config, "task_api");
      schedule_interval = getInt(config, "schedule_interval");
      inner_api         = getInt(config, "inner_api") != 0;
      userid            = getStr(config, "userid");
      run_end_time      = parseDate(config, "run_end_time");
      start_time        = parseDate(config, "start_time");

      this.db           = SysConfig.me().readConfig().db;
      this.state        = JOB_STATUS_INIT;
      this.id           = id;

      if (start_time.getTime() < System.currentTimeMillis())
        start_time = nextDate();

      if (inner_api) {
        callExecutor = new InnerApiCall();
      } else {
        callExecutor = new HttpCall();
      }

      TimeFactory.me().schedule(new TaskExecutor(), start_time);
    }


    private class TaskExecutor extends TimerTask {
      private TaskExecutor() {
        task = this;
      }

      @Override
      public void run() {
        try {
          state = JOB_STATUS_RUNNING;
          callExecutor.run();
        } catch (Exception e) {
          log.error(e);
        }

        if (run_times > 0) {
          if (--run_times == 0) {
            state = JOB_STATUS_MAXCOUNT;
            stop();
            return;
          }
        }

        if (schedule_interval <= 0) {
          state = JOB_STATUS_DEL;
          stop();
          return;
        }

        Date next = nextDate();
        if (next == null
                || (run_end_time != null
                && next.compareTo(run_end_time) >= 0)) {
          state = JOB_STATUS_TIMEUP;
          stop();
          return;
        }

        start_time = next;
        TimeFactory.me().schedule(new TaskExecutor(), start_time);
      }
    }


    public void stop() {
      task.cancel();
    }


    public String name() {
      return schedulenm;
    }


    public Date nextDate() {
      Calendar c = Calendar.getInstance();
      c.setTime(start_time);

      switch (schedule_cycle) {
        case JOB_UNIT_YEAR:
          c.add(Calendar.YEAR, schedule_interval);
          break;
        case JOB_UNIT_MONTH:
          c.add(Calendar.MONTH, schedule_interval);
          break;
        case JOB_UNIT_WEEK:
          c.add(Calendar.WEEK_OF_YEAR, schedule_interval);
          break;
        case JOB_UNIT_DAY:
        case JOB_UNIT_DAY2:
          c.add(Calendar.DAY_OF_YEAR, schedule_interval);
          break;
        case JOB_UNIT_HOUR:
          c.add(Calendar.HOUR, schedule_interval);
          break;
        case JOB_UNIT_SECOND:
          c.add(Calendar.SECOND, schedule_interval);
          break;
        case JOB_UNIT_MINUTE:
          c.add(Calendar.MINUTE, schedule_interval);
          break;
        default:
          return new Date();
      }
      return c.getTime();
    }


    private void scheduleLog(Object content) {
      Object[] parm = new Object[] { id, new Date(), content, task_api };
      try (SqlResult sr = SqlReader.query(LOG_FILE, db, parm)) {
        sr.getUpdateCount();
      }
    }


    /**
     * 用来调用外部接口
     */
    private class HttpCall implements Runnable {

      @Override
      public void run() {
        log.debug("Call Http", task_api);
        HttpUrl.Builder url = HttpUrl.parse(task_api).newBuilder();
        Request.Builder req = new Request.Builder();
        req.url(url.build());

        try {
          Response resp = openClient().newCall(req.build()).execute();
          ResponseBody body = resp.body();
          scheduleLog(body.byteStream());
          state = JOB_STATUS_STOP;
        } catch (Exception e) {
          state = JOB_STATUS_ERR;
          scheduleLog(e.toString());
        }
      }
    }


    /**
     * 用来直接调用平台内部接口
     */
    private class InnerApiCall implements Runnable {
      private final static String ID = "schedule-session";
      private SessionData sd;

      @Override
      public void run() {
        log.debug("Call Inner", task_api);
        try {
          HttpUrl url = HttpUrl.parse(task_api);
          List<String> ps = url.pathSegments();
          String context = ps.get(0);
          String reqUri = url.encodedPath();

          ApiCall ac = new ApiCall(ps.get(2), ps.get(3), ps.get(4), ps.get(5));
          ac.exparam = new HashMap<>();

          Map<String, String> parameters = new HashMap<>();
          for (int i = 0, size = url.querySize(); i < size; ++i) {
            parameters.put(url.queryParameterName(i),
                           url.queryParameterValue(i));
          }

          EmuServletRequest req = new EmuServletRequest(parameters);
          req.context = context;
          req.requestUriWithoutContext = reqUri.substring(context.length());
          BufScrvletOutputStream stream = new BufScrvletOutputStream();
          EmuServletResponse resp = new EmuServletResponse(stream);

          XResponse xr = new XResponse(req, resp);
          if (sd == null) login();
          req.setAttribute(SessionData.ATTRNAME, sd);
          ac.call = new CallData(req, resp);

          AppContext.me().call(ac);

          scheduleLog(stream.out.toString());
          state = JOB_STATUS_STOP;
        } catch (Exception e) {
          state = JOB_STATUS_ERR;
          scheduleLog(e.toString());
        }
      }

      private void login() throws SQLException {
        sd = new SessionData();
        sd.login_user = LoginUser.fromDb(userid, db);

        if (sd.login_user == null) {
          throw new XBosonException("用户不存在", 1014);
        }
        if (! ZR001_ENABLE.equals(sd.login_user.status)) {
          throw new XBosonException("用户已锁定", 1004);
        }
        sd.login_user.bindUserRoles(db);
        sd.login_user.password = null;
        sd.loginTime = System.currentTimeMillis();
        sd.endTime = Long.MAX_VALUE;
        sd.id = ID;
      }
    }
  }


  private OkHttpClient openClient() {
    if (hc == null) {
      //
      // 这个对象可能很昂贵
      //
      hc = new OkHttpClient();
    }
    return hc;
  }


  private String getStr(String i, String name) {
    if (Tool.isNulStr(i))
      throw new XBosonException.BadParameter(
              "String " + name, "not null");
    return i;
  }


  private String getStr(Map<String, Object> map, String name) {
    return getStr(map.get(name).toString(), name);
  }


  private int getInt(Map<String, Object> map, String name) {
    try {
      Object o = map.get(name);
      if (o instanceof String) {
        return Integer.parseInt((String) o);
      }
      if (o instanceof BigDecimal) {
        return ((BigDecimal) o).intValue();
      }
      if (o instanceof Number) {
        return (int) o;
      }
      return Integer.parseInt(o.toString());
    } catch (Exception e) {
      log.debug("Parameter", name, e.toString());
      return 0;
    }
  }


  private Date parseDate(Map<String, Object> map, String name) {
    Object o = map.get(name);
    if (o instanceof Date) {
      return (Date) o;
    }
    if (o == null) {
      return new Date();
    }

    try (DateParserFactory.DateParser
                 p = DateParserFactory.get(Tool.COMM_DATE_FORMAT)) {
      return p.parse(o.toString());
    } catch (Exception e) {
      log.debug("Parameter", name, e.toString());
      return new Date();
    }
  }
}
