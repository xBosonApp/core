////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-18 下午12:21
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/script/lib/Vm.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script.lib;

import com.xboson.been.XBosonException;
import com.xboson.script.JSObject;
import com.xboson.script.Sandbox;
import com.xboson.script.SandboxFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.Context;

import javax.script.*;
import java.io.StringReader;


public class Vm extends JSObject {

  public static final String VM_CONTEXT = "xBoson.vm.context";


  public Object Script(String code, ScriptObjectMirror options)
          throws ScriptException {
    return new Script(code, options);
  }


  public Object createContext() {
    return createContext(ScriptUtils.wrap(Context.getGlobal().newObject()));
  }


  public Object createContext(ScriptObjectMirror sandbox) {
    ScriptContext context = new SimpleScriptContext();
    sandbox.setMember(VM_CONTEXT, context);
    context.setBindings(sandbox, ScriptContext.GLOBAL_SCOPE);
    return sandbox;
  }


  public boolean isContext(ScriptObjectMirror o) {
    Object r = o.getMember(VM_CONTEXT);
    if (r != null) {
      return r instanceof ScriptContext;
    }
    return false;
  }


  public Object runInContext(String code, ScriptObjectMirror sandbox)
          throws ScriptException {
    return runInContext(code, sandbox, null);
  }


  public Object runInContext(String code,
                             ScriptObjectMirror sandbox,
                             ScriptObjectMirror options)
          throws ScriptException {
    ScriptContext context = (ScriptContext) sandbox.getMember(VM_CONTEXT);
    if (context == null) {
      throw new XBosonException.BadParameter("sandbox", "is not context");
    }
    if (options != null) {
      setOptions(context, options);
    }
    Sandbox box = SandboxFactory.create();
    return box.eval(code, context);
  }


  private void setOptions(ScriptContext context, ScriptObjectMirror options) {
    Object filename = options.get("filename");
    if (filename != null) {
      context.setAttribute(ScriptEngine.FILENAME,
              filename, ScriptContext.GLOBAL_SCOPE);
    }
  }


  public class Script {
    private Sandbox box;
    private CompiledScript script;
    private ScriptObjectMirror options;

    public Script(String code, ScriptObjectMirror options)
            throws ScriptException {
      this.box = SandboxFactory.create();
      this.script = box.compile(new StringReader(code));
      this.options = options;
    }

    public Object runInContext(ScriptObjectMirror sandbox, ScriptObjectMirror opt)
            throws ScriptException {
      ScriptContext context = (ScriptContext) sandbox.getMember(VM_CONTEXT);
      setOptions(context, options);
      return script.eval(context);
    }
  }
}
