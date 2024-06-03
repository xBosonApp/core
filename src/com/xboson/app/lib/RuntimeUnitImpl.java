////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-23 上午11:44
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/RuntimeUnitImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.been.CallData;
import com.xboson.been.XBosonException;
import com.xboson.script.lib.Bytes;
import com.xboson.util.Hex;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;
import com.xboson.util.converter.ScriptObjectMirrorJsonConverter;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.objects.NativeJSON;
import jdk.nashorn.internal.objects.NativeRegExp;
import jdk.nashorn.internal.runtime.Context;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * js 运行时抽象基类, 该类中定义的公共方法将被导出到 js 环境中.
 */
public abstract class RuntimeUnitImpl implements IApiConstant {

  /**
   * 请求数据包装对象, 子类直接使用
   */
  protected final CallData cd;


  public RuntimeUnitImpl(CallData cd) {
    this.cd = cd;
  }


  /**
   * 创建 js 环境中的 Array 对象
   * @param length 列表初始长度
   * @return
   */
  protected ScriptObjectMirror createJSList(int length) {
    NativeArray na = NativeArray.construct(true, null, length);
    ScriptObjectMirror list = (ScriptObjectMirror)
            ScriptObjectMirror.wrap(na, Context.getGlobal());
    return list;
  }


  /**
   * @see #createJSList(int)
   */
  protected ScriptObjectMirror createJSList() {
    return createJSList(0);
  }


  /**
   * js 传来的对象, 包装后才能在 java 中操作
   * @param obj 一个 ScriptObject 的子类
   * @return
   */
  protected ScriptObjectMirror wrap(Object obj) {
    Object ret = ScriptObjectMirror.wrap(obj, Context.getGlobal());
    if (ret instanceof ScriptObjectMirror) {
      return (ScriptObjectMirror) ret;
    }
    else if (ret instanceof ScriptObjectMirrorJsonConverter.Warp) {
      return ((ScriptObjectMirrorJsonConverter.Warp) ret).getWarpedObject();
    }
    else if (obj == null) {
      throw new XBosonException.NullParamException("object");
    }
    else {
      throw new XBosonException.BadParameter(
              obj.getClass().getName(), "not js object");
    }
  }


  /**
   * @see #createJSObject(Map)
   */
  protected ScriptObjectMirror createJSObject() {
    return (ScriptObjectMirror) ScriptObjectMirror.wrap(
            Context.getGlobal().newObject(), Context.getGlobal());
  }


  /**
   * 使用 map 初始化创建的 js 对象的属性
   */
  protected ScriptObjectMirror createJSObject(
          Map<? extends String, ? extends Object> init) {
    ScriptObjectMirror js = createJSObject();
    js.putAll(init);
    return js;
  }


  /**
   * 包装 java byte 数组, 返回 js 数组类型对象.
   *
   * [实现细节] 使用 ArrayData.allocate(ByteBuffer.wrap(b)) 创建的数组对象,
   * 虽然不需要复制内存, 但是该数组对象无法再次通过实参调用 java 函数, 如果调用抛出
   * UnsupportedOperationException 异常.
   */
  protected Object wrapBytes(byte[] b) {
    ScriptObjectMirror js = createJSList(b.length);
    for (int i=0; i<b.length; ++i) {
      js.setSlot(i, b[i]);
    }
    return js;
  }


  /**
   * 创建对象数组的 key 集合
   * @param objs js 对象数组
   * @param attrName 每个对象使用该属性的值作为 set 的 key
   */
  protected Set<String> array2Set(ScriptObjectMirror objs, String attrName) {
    if (Tool.isNulStr(attrName))
      throw new XBosonException.NullParamException("String attrName");

    Set<String> ret = new HashSet<>();

    for (int i=0; i<objs.size(); ++i) {
      ScriptObjectMirror cobj = wrap(objs.getSlot(i));
      if (cobj.hasMember(attrName)) {
        ret.add( String.valueOf(cobj.getMember(attrName)) );
      }
    }
    return ret;
  }


  /**
   * 针对 js 内部对象字符串化
   */
  protected String jsonStringify(Object o) {
    if (o != null) {
      o = ScriptUtils.unwrap(o);
      o = NativeJSON.stringify(this, o, null, null);
    }
    return String.valueOf(o);
  }


  /**
   * 解析 json 字符串转换为 js 内部对象;
   * str 为空字符串或 null 则返回 null;
   * 如果 json 字符串有语法错误抛出异常;
   */
  protected Object jsonParse(String str) {
    if (Tool.isNulStr(str))
      return null;

    try {
      return NativeJSON.parse(this, str, null);
    } catch(Exception e) {
      throw new XBosonException.SyntaxError("JSON", str, e);
    }
  }


  /**
   * ScriptObjectMirror 不应该直接传到 js 环境, 而是传递底层 js 对象,
   * 通过该方法获取 ScriptObjectMirror 包装的底层对象.
   */
  protected Object unwrap(Object mirror) {
    return ScriptObjectMirror.unwrap(mirror, Context.getGlobal());
  }


  /**
   * 返回非空字符串, 否则会抛出异常
   */
  protected String getNNStringAttr(ScriptObjectMirror mir, String attrName) {
    String s = (String) mir.getMember(attrName);
    if (Tool.isNulStr(s)) {
      throw new XBosonException.NullParamException("String " + attrName);
    }
    return s;
  }


  /**
   * 返回字符串, 非字符串类型或 Undefined 返回 null.
   */
  protected String getStringAttr(ScriptObjectMirror mir, String attrName) {
    Object o = mir.getMember(attrName);
    if (o instanceof String) {
      return (String) o;
    }
    return null;
  }


  /**
   * 便捷方法
   * @see QueryImpl#copyToList(RuntimeUnitImpl, ScriptObjectMirror, ResultSet)
   */
  protected int copyToList(ScriptObjectMirror list, ResultSet rs)
          throws SQLException {
    return QueryImpl.copyToList(this, list, rs);
  }


  /**
   * 为 jdbc 查询参数绑定提供对象转换, 这些对象 jdbc 无法处理会抛出异常.
   *
   * @param o 来自 js 的对象.
   * @return jdbc 能处理的对象.
   */
  public static Object getSafeObjectForQuery(Object o) {
    if (isNull(o)) {
      return null;
    }
    if (o instanceof jdk.nashorn.internal.objects.NativeFunction) {
      return null;
    }
    if (o instanceof Exception) {
      return null;
    }
    if (o instanceof jdk.nashorn.internal.runtime.ScriptObject) {
      return o.toString();
    }
    if (o instanceof ScriptObjectMirror) {
      o = ScriptUtils.unwrap(o);
      return o +"";
    }
    return o;
  }


  /**
   * 获取正则表达式的原始字符串
   * @see #isRegexp(Object) 判断是否是正则表达式对象
   * @param exp 必须是正则表达式对象
   */
  public static String getRegExp(Object exp) {
    return (String) NativeRegExp.source(exp);
  }


  public static boolean isNull(Object o) {
    if (o == null)
      return true;
    if (o instanceof jdk.nashorn.internal.runtime.Undefined)
      return true;
    return false;
  }


  /**
   * 严格检查对象必须是 true
   */
  public static boolean isTrue(Object o) {
    if (o == null)
      return false;
    if (o instanceof Boolean)
      return (Boolean) o;
    return false;
  }


  /**
   * 是正则表达式对象返回 true
   * @see #getRegExp(Object) 正则表达式的原始字符串
   */
  public static boolean isRegexp(Object o) {
    if (o == null) return false;
    return o instanceof NativeRegExp;
  }


  /**
   * 通常约定一些值的意义为 true.
   * 当 o==true 或 o!=0 或 o是非空字符串 的情况返回 true
   */
  public static boolean isAboutTrue(Object o) {
    if (o == null)
      return false;
    if (o instanceof Boolean)
      return (Boolean) o;
    if (o instanceof Number)
      return ((int) o) != 0;
    if (o instanceof String)
      return Tool.notNulStr((String)o);
    return false;
  }


  /**
   * 返回 js 语言环境下的 undefined 对象, 对于 java 这是一个非空对象.
   */
  public Object nullObj() {
    return jdk.nashorn.internal.runtime.Undefined.getUndefined();
  }


  /**
   * 创建 js 数组迭代器, 类型必须正确.
   */
  public static <E> Iterable<E> arrayIterator(ScriptObjectMirror arr, Class<E> c) {
    return () -> new ArrayIterator<>(arr);
  }


  /**
   * 方便迭代 js 数组对象
   * @param <E>
   */
  public static class ArrayIterator<E> implements Iterator<E> {
    private ScriptObjectMirror list;
    private int size;
    private int p;


    /**
     * 如果 list 不是数组会抛出异常
     */
    public ArrayIterator(ScriptObjectMirror list) {
      if (! list.isArray())
        throw new XBosonException.BadParameter(
                "ScriptObjectMirror", "not Array");

      this.list = list;
      this.size = list.size();
      this.p    = 0;
    }


    @Override
    public boolean hasNext() {
      return p < size;
    }


    @Override
    public E next() {
      return (E) list.getSlot(p++);
    }
  }


  /**
   * ScriptObjectMirror 的包装, 用来结构化获取数据
   */
  public class Mirror {

    public final ScriptObjectMirror original;


    public Mirror(ScriptObjectMirror original) {
      this.original = original;
    }


    public Mirror(Object obj) {
      this.original = wrap(obj);
    }


    /** name 属性必须是非空字符串, 否则抛出异常 */
    public String string(String name) {
      return (String) original.getMember(name);
    }


    /** name 属性必须是 int, 否则抛出异常 */
    public int integer(String name) {
      return (int) original.getMember(name);
    }


    /** name 属性必须是 long, 否则抛出异常 */
    public long longint(String name) {
      return (long) original.getMember(name);
    }


    /** name 属性必须是 double, 否则抛出异常 */
    public double doublen(String name) {
      return (double) original.getMember(name);
    }


    /** name 属性必须是 float, 否则抛出异常 */
    public float floatn(String name) {
      return (float) original.getMember(name);
    }


    /** name 属性必须是数组, 否则抛出异常 */
    public Mirror list(String name) {
      ScriptObjectMirror check = (ScriptObjectMirror) original.getMember(name);
      if (! check.isArray())
        throw new XBosonException.BadParameter(name, "not array");
      return new Mirror(check);
    }


    /** name 属性必须是 js-object, 否则抛出异常 */
    public Mirror jsobj(String name) {
      return new Mirror((ScriptObjectMirror) original.getMember(name));
    }


    /** name 属性必须是非空对象, 否则抛出异常 */
    public Object get(String name) {
      return original.getMember(name);
    }


    public int size() {
      return original.size();
    }


    /** 当前对象必须是数组, 用于创建迭代器 */
    public <E> Iterable<E> each(Class<E> clazz) {
      return arrayIterator(original, clazz);
    }


    /** 转换为 clazz 类型数组, 类型错误会抛出异常 */
    public <E> E[] toArray(Class<E[]> clazz) {
      Class<E> itemType = (Class<E>) clazz.getComponentType();
      E [] arr = (E[]) Array.newInstance(itemType, size());
      int i = -1;
      for (E item : each(itemType)) {
        arr[++i] = item;
      }
      return arr;
    }
  }


  public Bytes toBytes(String str, String coding) {
    byte[] buf = Hex.decode(coding, str);
    return new Bytes(buf);
  }


  public Bytes toBytes(String javastr) {
    return new Bytes(javastr.getBytes(IConstant.CHARSET));
  }


  public Bytes joinBytes(Bytes...arr) {
    int total = 0;
    for (Bytes b : arr) {
      if (b == null) continue;
      total += b.bin().length;
    }
    byte[] base = new byte[total];
    int begin = 0;
    for (Bytes b : arr) {
      if (b == null) continue;
      byte[] read = b.bin();
      System.arraycopy(read, 0, base, begin, read.length);
      begin += read.length;
    }
    return new Bytes(base);
  }
}
