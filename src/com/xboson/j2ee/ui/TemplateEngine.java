////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-20 上午9:14
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/j2ee/ui/TemplateEngine.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.ui;

import com.xboson.been.Module;
import com.xboson.been.XBosonException;
import com.xboson.event.GLHandle;
import com.xboson.event.GlobalEventBus;
import com.xboson.fs.node.NodeFileFactory;
import com.xboson.fs.redis.IRedisFileSystemProvider;
import com.xboson.fs.script.IScriptFileSystem;
import com.xboson.fs.script.ScriptAttr;
import com.xboson.fs.ui.UIFileFactory;
import com.xboson.init.Startup;
import com.xboson.script.*;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.Tool;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.naming.event.NamingEvent;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


/**
 * 面向 servlet 的模板渲染引擎,
 * 该对象创建独立的脚本环境来运行模板引擎.
 */
public class TemplateEngine extends GLHandle
        implements IConfigSandbox, IScriptFileSystem {

  public final static String RELOAD_TAGS = "xboson.template.engine.reloadtag";
  public final static String fileName = "./masquerade.js";
  public final static String ROOT = "/face";
  public final static String EXT = ".htm";

  private IRedisFileSystemProvider uifs;
  private Application app;
  private Module masquerade;
  private ScriptObjectMirror service;
  private ScriptObjectMirror reload_tags;
  private HelperModule helper;
  private Map<String, Object> config;


  public TemplateEngine(IRedisFileSystemProvider uifs) {
    try {
      this.uifs = uifs;
      this.helper = new HelperModule();
      this.config = makeConfig();

      SysModules sysmod = EnvironmentFactory.createDefaultSysModules();
      sysmod.regLib("helper", helper);

      IConfigurableModuleProvider nodejs_mod =
              NodeFileFactory.openNodeModuleProvider(sysmod);

      BasicEnvironment env = EnvironmentFactory.createEmptyBasic();
      env.insertConfiger(nodejs_mod);
      app = new Application(env, this);
      app.config(this);

      GlobalEventBus.me().on(RELOAD_TAGS, this);
    } catch (Exception e) {
      throw new XBosonException(e);
    }
  }


  @Override
  public void config(Sandbox box, ICodeRunner runner) throws ScriptException {
    StringBufferOutputStream buf =
            Tool.readFileFromResource(UIFileFactory.class, fileName);

    WrapJavaScript js = box.warp(fileName, buf.toString());
    js.compile(box);
    js.initModule(runner);

    masquerade = js.getModule();
    ScriptObjectMirror exports = (ScriptObjectMirror) masquerade.exports;
    ScriptObjectMirror init = (ScriptObjectMirror) exports.get("init");
    service = (ScriptObjectMirror) init.call(null, ROOT, config, true, uifs);
    reload_tags = (ScriptObjectMirror) service.get("reload_tags");
  }


  private Map<String, Object> makeConfig() {
    Map<String, Object> cfg = new HashMap<>();
    cfg.put("public",   "/");
    cfg.put("private",  "/masquerade");
    cfg.put("extname",  "htm");
    cfg.put("encoding", "utf8");

    ServletContext sc = Startup.getServletContext();
    Map runtime_cfg = new HashMap();
    cfg.put("runtime_cfg", runtime_cfg);
    runtime_cfg.put("contextPath", sc.getContextPath());
    runtime_cfg.put("serverInfo",  sc.getServerInfo());
    runtime_cfg.put("contextName", sc.getServletContextName());

    return cfg;
  }


  public void service(HttpServletRequest req, HttpServletResponse resp) {
    service.call(null, req, resp);
  }


  @Override
  public ByteBuffer readFile(String path) throws IOException {
    return null; // 不能读取额外的文件
  }


  @Override
  public ScriptAttr readAttribute(String path) throws IOException {
    return null; // 不能读取额外的文件
  }


  @Override
  public String getID() {
    return "TemplateEngine";
  }


  @Override
  public String getType() {
    return getID();
  }


  /**
   * 全局事件, 通知所有模板引擎重新加载标签库
   */
  public static void reloadAllTags() {
    GlobalEventBus.me().emit(RELOAD_TAGS);
  }


  public static void fileChange(String path, String type) {
    GlobalEventBus.me().emit(RELOAD_TAGS, path, NamingEvent.OBJECT_CHANGED, type);
  }


  @Override
  public void objectChanged(NamingEvent namingEvent) {
    Object path = namingEvent.getNewBinding().getObject();
    Object type = namingEvent.getChangeInfo();
    reload_tags.call(null, path, type);
  }
}
