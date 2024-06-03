////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-23 上午11:32
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/ListImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.been.XBosonException;
import com.xboson.script.IJSObject;
import com.xboson.util.Tool;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.runtime.Context;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class ListImpl extends RuntimeUnitImpl implements IJSObject {

  public final static String RISE = "0";
  public Object array_sort_implement_js;


  public ListImpl() {
    super(null);
  }


  @Override
  public String env_name() {
    return "list";
  }


  @Override
  public boolean freeze() {
    return false;
  }


  @Override
  public void init() {
  }


  @Override
  public void destory() {
  }


  public Object range(ScriptObjectMirror arr, int begin, int end) {
    return arr.callMember("slice", begin, end);
  }


  public Object removeAt(ScriptObjectMirror arr, int remove_index) {
    arr.callMember("splice", remove_index, 1);
    return arr;
  }


  public Object add(ScriptObjectMirror arr, Object val) {
    try {
      arr.callMember("push", val);
      return arr;
    } catch(Exception e) {
      e.printStackTrace();
      throw e;
    }
  }


  public Object addAt(ScriptObjectMirror arr, Object val, int index) {
    arr.callMember("splice", index, 0, val);
    return arr;
  }


  public Object addAll(ScriptObjectMirror arr, Object src) {
    ScriptObjectMirror jsarr = wrap(src);
    int end = jsarr.size();
    for (int i=0; i<end; ++i) {
      arr.callMember("push", jsarr.getSlot(i));
    }
    return arr;
  }


  public Object reverse(ScriptObjectMirror arr) {
    arr.callMember("reverse");
    return arr;
  }


  public String toString(ScriptObjectMirror arr, Object sp) {
    return (String) arr.callMember("join", sp);
  }


  public boolean contain(Object oarr, Object compareVal) {
    ScriptObjectMirror arr = wrap(oarr);
    compareVal = ScriptObjectMirror.wrap(compareVal, Context.getGlobal());
    final int end = arr.size();

    for (int i=0; i<end; ++i) {
      Object o = arr.getSlot(i);
      if (_equals(compareVal, o)) {
        return true;
      }
    }
    return false;
  }


  /**
   * 深层比较两个 js 对象, 比较对象中的所有属性都相同返回 true.
   * 如果不是 js 对象, 进行简单比较.
   * 脚本环境中原始对象还是原始对象(int,float,string),
   * 其他复杂对象被包装到 ScriptObjectMirror 中.
   *
   * @param a 尽可能转换为 ScriptObjectMirror 的对象
   * @param b 与 a 比较
   * @return a==b 返回 true
   */
  private boolean _equals(Object a, Object b) {
    //
    // b 不是复杂对象, 执行简单比较
    //
    if (b instanceof ScriptObjectMirror == false) {
      return a.equals(b);
    }
    //
    // b 一定是复杂对象, 而 a 不是, a b,一定不同
    //
    if (a instanceof ScriptObjectMirror == false) {
      return false;
    }

    ScriptObjectMirror x = (ScriptObjectMirror) b;
    ScriptObjectMirror y = (ScriptObjectMirror) a;

    if (x.equals(y))
      return true;

    if (x.size() != y.size())
      return false;

    Set<String> names = new HashSet<>(x.size() << 1);
    names.addAll(x.keySet());
    names.addAll(y.keySet());

    for (String name : names) {
      Object o1 = x.getMember(name);
      Object o2 = y.getMember(name);

      if (o1 == null) {
        if (o2 == null) {
          continue;
        } else {
          return false;
        }
      }

      if (! o1.equals(o2)) {
        return false;
      }
    }
    return true;
  }


  public Object remove(ScriptObjectMirror jsarr, Object removeVal) {
    removeVal = ScriptObjectMirror.wrap(removeVal, Context.getGlobal());

    for (int i=0; i<jsarr.size(); ++i) {
      Object o = jsarr.getSlot(i);
      if (_equals(removeVal, o)) {
        jsarr.callMember("splice", i, 1);
        continue;
      }
    }
    return jsarr;
  }


  public Object sort(Object arr, String... param) {
    if (array_sort_implement_js == null)
      throw new XBosonException.NotExist("sort function not init");

    ScriptObjectMirror sort = wrap(array_sort_implement_js);
    if (! sort.isFunction() )
      throw new XBosonException.NotExist("sort function fail.");

    sort.call(unwrap(arr), param);
    return arr;
  }

}
