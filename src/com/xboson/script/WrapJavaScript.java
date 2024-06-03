////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月5日 上午9:56:07
// 原始文件路径: xBoson/src/com/xboson/script/WrapJavaScript.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script;

import com.xboson.been.XBosonException;
import com.xboson.util.ReaderSet;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.internal.runtime.ECMAException;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.nio.ByteBuffer;


/**
 * 用来包装 js 源代码
 */
public class WrapJavaScript extends AbsWrapScript {

  private static final String S_WRAP0 = "__warp_main(function(" +
          "require, module, __dirname , __filename, exports, console) {";
  private static final String S_WRAP1 = "\n})";

  private ReaderSet code_reader;
  private CompiledScript cs;
  private ScriptContext context;


  protected WrapJavaScript(String filename) {
    super(filename);
    this.code_reader = new ReaderSet();
  }


  public WrapJavaScript(byte[] code, String filename) {
    this(ByteBuffer.wrap(code), filename);
  }


  public WrapJavaScript(ByteBuffer code, String filename) {
    this(filename);
    code_reader.add(S_WRAP0);
    code_reader.add(code);
    code_reader.add(S_WRAP1);
  }


  public WrapJavaScript(String code, String filename) {
    this(filename);
    code_reader.add(S_WRAP0);
    code_reader.add(code);
    code_reader.add(S_WRAP1);
  }


  public void compile(Sandbox box) {
    try {
      context = box.createContext();
      box.setFilename(filename);
      cs = box.compile(code_reader);

    } catch (ScriptException e) {
      throw new JScriptException(e, code_reader, filename);
    }
  }


  public Object initModule(ICodeRunner crun) {
    try {
      //
      // jso 是在 'bootstrap.js' 脚本中 __warp_main 函数返回的函数.
      //
      AbstractJSObject jso = (AbstractJSObject) cs.eval(context);
      Object warpreturn = jso.call(module, module, crun);
      module.loaded = true;
      return warpreturn;

    } catch (ECMAException ec) {
      throw new JScriptException(ec, code_reader, filename);

    } catch (Exception e) {
      throw new XBosonException(e);
    }
  }

}
