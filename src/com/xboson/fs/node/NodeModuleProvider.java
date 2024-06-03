////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-22 下午4:18
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/node/NodeModuleProvider.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.node;

import com.xboson.been.Module;
import com.xboson.been.PackageInf;
import com.xboson.been.XBosonException;
import com.xboson.event.GlobalEventBus;
import com.xboson.event.OnFileChangeHandle;
import com.xboson.fs.redis.RedisFileAttr;
import com.xboson.fs.redis.IFileSystemConfig;
import com.xboson.fs.redis.IRedisFileSystemProvider;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.script.*;
import com.xboson.script.lib.Path;
import com.xboson.util.Tool;
import okio.BufferedSource;
import okio.Okio;

import javax.script.ScriptException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * 按照 nodejs 的规则来加载脚本文件.
 */
public class NodeModuleProvider extends AbsModules implements IModuleProvider {

  /**
   * node 目录必须有 package.json 文件, 且 name 为该名称.
   */
  public static final String PROJECT_NAME = "xboson-node-modules";
  public static final String PACKAGE_FILE = "/package.json";
  public static final String DEF_MAIN     = "/index.js";
  public static final String FILE_TYPE    = "node-modules";

  private ICodeRunner runner;
  private IRedisFileSystemProvider fs;
  private IModuleProvider parent;
  private FileChange fc;
  private Log log;


  NodeModuleProvider(IRedisFileSystemProvider fs,
          IFileSystemConfig config, IModuleProvider parent) {
    this.fs     = fs;
    this.parent = parent;
    this.log    = LogFactory.create();
    this.fc     = new FileChange();
    check();
  }


  private void check() {
    PackageInf pkgInf = readPackage("/");

    if (! PROJECT_NAME.equalsIgnoreCase(pkgInf.name)) {
      throw new XBosonException("bad package name: " + pkgInf.name);
    }
    log.debug("Node modules:", pkgInf, "in", "/");
  }


  @Override
  public Module getModule(String name, Module apply) {
    Module mod = parent.getModule(name, apply);

    if (mod != null)
      return mod;

    boolean hasExt = Path.me.extname(name).length() > 1;

    for (int i=0; i<apply.paths.length && mod == null; ++i) {
      mod = findModule(apply.paths[i], name, apply);
      if (mod == null && hasExt == false) {
        mod = findModule(apply.paths[i], name + ".js", apply);
      }
    }

    return mod;
  }


  private Module findModule(String dir, String name, Module apply) {
    RedisFileAttr attr = fs.readAttribute(dir +'/'+ name);
    Module mod = null;

    if (attr != null) {
      if (attr.isDir()) {
        PackageInf pkg = readPackage(attr.path);
        if (! Tool.isNulStr(pkg.main)) {
          mod = loadModule(attr.path +'/'+ pkg.main, name);
        } else {
          mod = loadModule(attr.path + DEF_MAIN, name);
        }
      } else {
        mod = loadModule(attr.path, name);
      }
    }
    return mod;
  }


  private Module loadModule(String script, String mod_name) {
    RedisFileAttr attr = fs.readAttribute(script);
    if (attr == null)
      throw new XBosonException.NotFound(script);

    fs.readFileContent(attr);
    ByteBuffer buf = ByteBuffer.wrap(attr.getFileContent());
    AbsWrapScript wrap = new WrapJavaScript(buf, mod_name);

    Module mod = wrap.getModule();
    mod.loaderid = LOADER_ID_NODE_MODULE;
    mod.paths = get_module_paths(Path.me.dirname(script));

    runner.run(wrap);
    fc.lookup(script);

    log.debug("Load node module '"+ script +"'");
    return mod;
  }


  private PackageInf readPackage(String dir) {
    String file = dir + PACKAGE_FILE;
    RedisFileAttr attr = fs.readAttribute(file);
    if (attr == null)
      throw new XBosonException.NotFound(file);

    try {
      fs.readFileContent(attr);
      byte[] content = attr.getFileContent();

      //
      // 构造输入流可以避免内存复制
      //
      BufferedSource r = Okio.buffer(
              Okio.source(new ByteArrayInputStream(content)));

      return Tool.getAdapter(PackageInf.class).fromJson(r);
    } catch (IOException e) {
      throw new XBosonException.IOError(e);
    }
  }


  @Override
  public void config(Sandbox box, ICodeRunner runner) throws ScriptException {
    try {
      if (parent instanceof IConfigSandbox) {
        ((IConfigSandbox) parent).config(box, runner);
      }

      box.getGlobalInvocable().invokeFunction(
              "__set_sys_module_provider", this);

      this.runner = runner;

    } catch(NoSuchMethodException e) {
      throw new ScriptException(e);
    }
  }


  private class FileChange extends OnFileChangeHandle {

    @Override
    protected void onFileChange(String file_name) {
      runner.changed(file_name);
    }

    public void lookup(String file_name) {
      String eventName = getEventName(file_name);
      GlobalEventBus.me().on(eventName, this);
    }
  }
}
