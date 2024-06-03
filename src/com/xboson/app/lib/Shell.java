////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-15 下午1:30
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/Shell.java
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
import com.xboson.been.XBosonException;
import com.xboson.db.IDict;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.rpc.IPing;
import com.xboson.rpc.IXRemote;
import com.xboson.rpc.RpcFactory;
import com.xboson.script.lib.Path;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.SysConfig;
import com.xboson.util.c0nst.IConstant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Shell extends RuntimeUnitImpl implements IAResource {

  public static final String RPC_NAME = "XB.rpc.OS.Shell";
  public final static int MAX_RUN_TIME = 30 * 60;

  private final String basePath;
  private final boolean isWindows;
  private final Log log;


  public Shell() {
    super(null);
    basePath = SysConfig.me().readConfig().shellUrl;
    isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
    log = LogFactory.create("script.lib.shell");

    try {
      RpcFactory rpc = RpcFactory.me();
      if (! rpc.isBind(RPC_NAME)) {
        rpc.bind(new ShellImpl(), RPC_NAME);
      }
    } catch (Exception e) {
      log.error(e);
    }
  }


  public IShell open() throws Exception {
    return open(IConstant.DEFAULT_NODE_ID);
  }


  public IShell open(String nodeID) throws Exception {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    boolean runOnSysOrg = (boolean) ModuleHandleContext._get("runOnSysOrg");

    if (!runOnSysOrg) {
      throw new XBosonException.NotImplements("只能在平台机构中引用");
    }

    if (!AppContext.me().who().isRoot()) {
      SysImpl sys = (SysImpl) ModuleHandleContext._get("sys");

      if (IDict.ADMIN_FLAG_ADMIN.equals(sys.getUserAdminFlag())) {
        throw new XBosonException.NotImplements("只有平台管理员可以调用");
      }
    }

    RpcFactory rpc = RpcFactory.me();
    return (IShell) rpc.lookup(nodeID, RPC_NAME);
  }


  public interface IShell extends IXRemote {
    Object execute(String fileName)  throws IOException;
    Object execute(String fileName, String[] args) throws IOException;
    void putEnv(String name, String val) throws RemoteException;
    String getEnv(String name) throws RemoteException;
    void clearEnv() throws RemoteException;
  }


  private class ShellImpl implements IShell, IPing {
    private ThreadLocal<Map<String, String>> envVar;


    private ShellImpl() {
      envVar = new ThreadLocal<>();
    }


    @Override
    public Object execute(String fileName)  throws IOException {
      return execute(fileName, null);
    }


    @Override
    public Object execute(String fileName, String[] args) throws IOException {
      long begin = System.currentTimeMillis();
      fileName = Path.me.normalize(fileName);
      File fullPath = findExeFile(fileName);

      List<String> command = new ArrayList<>();
      command.add(fullPath.getPath());

      if (args != null && args.length > 0) {
        for (int i=0; i<args.length; ++i) {
          command.add(args[i] +"");
        }
      }

      //
      // ProcessBuilder 内部也将数组转换为 List
      //
      ProcessBuilder build = new ProcessBuilder(command);
      build.directory(new File(fullPath.getParent()));
      build.redirectErrorStream(true);
      Map<String, String> env = envVar.get();
      if (env != null) {
        build.environment().putAll(env);
      }

      Process process = build.start();
      try {
        process.waitFor(MAX_RUN_TIME, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        log.warn("Shell", e);
      } finally {
        if (process.isAlive()) {
          process.destroy();
        }
      }

      Map<String, Object> ret = new HashMap<>();
      ret.put("output", toString(process.getInputStream()));
      ret.put("code",   process.exitValue());
      ret.put("path",   fullPath.getPath());
      ret.put("elapsed",System.currentTimeMillis() - begin);
      return ret;
    }


    @Override
    public void putEnv(String name, String val) throws RemoteException {
      env().put(name, val);
    }


    @Override
    public String getEnv(String name) throws RemoteException {
      return env().get(name);
    }


    public void clearEnv() throws RemoteException {
      env().clear();
    }


    private Map<String, String> env() {
      Map<String, String> env = envVar.get();
      if (env == null) {
        env = new HashMap<>();
        envVar.set(env);
      }
      return env;
    }


    private String toString(InputStream in) throws IOException {
      StringBufferOutputStream buf = new StringBufferOutputStream();
      buf.write(in);
      return buf.toString();
    }


    private File findExeFile(String fileName) throws IOException {
      File full = new File(basePath, fileName);
      if (! fileName.contains(".")) {
        if (! full.exists()) {
          if (isWindows) {
            full = new File(basePath, fileName + ".cmd");
            if (! full.exists())
              full = new File(basePath, fileName + ".bat");
            if (! full.exists())
              full = new File(basePath, fileName + ".exe");
          } else {
            full = new File(basePath, fileName + ".sh");
          }

          if (! full.exists()) {
            throw new IOException("not found "+ fileName);
          }
        }
      }
      return full;
    }
  }


  @Override
  public String description() {
    return "app.module.shell.functions()";
  }
}
