////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月5日 上午11:47:37
// 原始文件路径: xBoson/src/com/xboson/script/JSObject.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script;


import com.xboson.util.IConversion;
import com.xboson.util.Tool;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.objects.NativeArrayBuffer;
import jdk.nashorn.internal.objects.NativeRangeError;
import jdk.nashorn.internal.objects.NativeReferenceError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


/**
 * 用于辅助 js 对象与 java 对象桥接和转换.
 */
public abstract class JSObject implements IJSObject, IVisitByScript {


	@Override
	public boolean freeze() {
		return true;
	}


	@Override
	public void init() {
	}


	@Override
	public void destory() {
	}


	@Override
	public String env_name() {
		return null;
	}


  /**
   * 返回本类的方法列表, 给 js 调用
   */
	public String[] getFunctionNames() {
	  Method[] m = getClass().getDeclaredMethods();
	  String[] names = new String[m.length];
	  for (int i=0; i<m.length; ++i) {
	    names[i] = m[i].getName();
    }
	  return names;
  }


///////////////////////////////////////////////////////////////////////////////
////-- 静态 函数/属性
////-- 这些算法涉及到 jdk.nashorn 的内部实现
///////////////////////////////////////////////////////////////////////////////

  public final static JSToJava default_convert = new JSToJava();
  static {
    default_convert.add(new PrimitiveConvert(
            Integer.class,    Integer.TYPE));
    default_convert.add(new PrimitiveConvert(
            Long.class,       Long.TYPE));
    default_convert.add(new PrimitiveConvert(
            Boolean.class,    Boolean.TYPE));
    default_convert.add(new PrimitiveConvert(
            Character.class,  Character.TYPE));
    default_convert.add(new PrimitiveConvert(
            Byte.class,       Byte.TYPE));
    default_convert.add(new PrimitiveConvert(
            Short.class,      Short.TYPE));
    default_convert.add(new PrimitiveConvert(
            Float.class,      Float.TYPE));
    default_convert.add(new PrimitiveConvert(
            Double.class,     Double.TYPE));

    default_convert.add(new _JSArray());
  }


  /**
   * 提取 js 传入的 ArrayBuffer 对象的底层存储缓冲区,
   * 反射并调用了 private 方法, 不同的 jdk 版本会不兼容.
   *
   * 测试的 jdk 版本:
   *    build 1.8.0_66-b17
   *
   * @param jsArrayBuffer ArrayBuffer 对象, 或 Uint16Array.buffer 属性
   * @return ByteBuffer 类型对象
   *
   * @throws NoSuchMethodException - 版本不兼容可能抛出
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  public static ByteBuffer getUnderlyingBuffer(Object jsArrayBuffer)
          throws NoSuchMethodException, InvocationTargetException,
          IllegalAccessException {
    ScriptObjectMirror sobj = (ScriptObjectMirror) jsArrayBuffer;

    if (!sobj.getClassName().equals("ArrayBuffer"))
        throw new ClassCastException("is not ArrayBuffer object");

    Method getScriptObject = sobj.getClass()
            .getDeclaredMethod("getScriptObject");
    getScriptObject.setAccessible(true);
    NativeArrayBuffer nad
            = (NativeArrayBuffer) getScriptObject.invoke(sobj);

    Method cloneBuffer = nad.getClass()
            .getDeclaredMethod("getNioBuffer");
    cloneBuffer.setAccessible(true);
    ByteBuffer buf = (ByteBuffer) cloneBuffer.invoke(nad);

    return buf;
  }


  /**
   * 创建 js 的 Array 对象, 可以提供多个参数作为初始值
   * @param arg 初始值
   * @return
   */
  public static Object createJSArray(Object ...arg) {
    return NativeArray.construct(true, null, arg);
  }


  /**
   * 抛出 js 原生 RangeError 错误
   */
  public static void throwJSRangeError(String msg) {
    NativeRangeError err =
            NativeRangeError.constructor(true, null, msg);

    if (err.nashornException instanceof RuntimeException) {
      throw (RuntimeException) err.nashornException;
    } else {
      throw new RuntimeException(msg);
    }
  }


  /**
   * 转换为 js 原声 RangeError 错误
   */
  public static void throwJSRangeError(IndexOutOfBoundsException e) {
    String msg = e.getMessage();
    if (msg == null)
      msg = "Index out of range";

    throwJSRangeError(msg);
  }


  public static void throwJSReferenceError(String msg) {
    NativeReferenceError err =
            NativeReferenceError.constructor(true, null, msg);

    if (err.nashornException instanceof RuntimeException) {
      throw (RuntimeException) err.nashornException;
    } else {
      throw new RuntimeException(msg);
    }
  }


  public static void throwJSReferenceError(Throwable t) {
    throwJSReferenceError(
            "[" + t.getClass().getSimpleName()
            + "] " + t.getMessage());
  }


///////////////////////////////////////////////////////////////////////////////
////-- 辅助 接口/类
///////////////////////////////////////////////////////////////////////////////

  /**
   * Helper 配置器
   */
	public interface IConfig {
    /**
     * 对 Helper 对象进行配置
     * @param target
     */
	  void config(Helper target);
  }


  /**
   * 转换器描述
   */
  public interface IConversionDesc extends IConversion<Object,Object> {
    Class<?> valueClass();
  }


  /**
   * 可配置基类, 封装 js 对象特性
   */
	static public class Helper extends AbstractJSObject {
    private Map<String, AbstractJSObject>
            functions = new HashMap<>();
    private Map<String, ExportsAttribute>
            attributes = new HashMap<>();

    /**
     * 添加js函数属性
     */
    protected void addFunction(String name, AbstractJSObject value) {
      functions.put(name, value);
    }

    /**
     * 添加的属性映射到 java 的 setter/getter 方法
     */
    protected void addDynAttr(String name, ExportsAttribute v) {
      attributes.put(name, v);
    }

    /**
     * 指定的属性绑定了函数/动态属性则返回 false
     */
    @Override
    public boolean hasMember(String name) {
      return functions.containsKey(name)
              || attributes.containsKey(name);
    }

    /**
     * 如果重写需要调用这里
     */
    @Override
    public Object getMember(String name) {
      Object ret = functions.get(name);
      if (ret != null) {
        return ret;
      }
      ExportsAttribute v = attributes.get(name);
      if (v != null) {
        return v.get();
      }
      return null;
    }

    @Override
    public void setMember(String name, Object value) {
      ExportsAttribute v = attributes.get(name);
      if (v != null) {
        v.set(value);
      }
    }

    /**
     * 应用一个配置器
     * @param iconfig - 配置器的 class 必须有无参构造.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public boolean config(Class<? extends IConfig> iconfig) {
      try {
        return config(iconfig.newInstance());
      } catch(Exception e) {
        return false;
      }
    }

    /**
     * 应用一个配置器
     */
    public boolean config(IConfig c) {
      c.config(this);
      return true;
    }
  }


  /**
   * 配置 JSObjectHelper, 使导出的公共方法可以被 js 代码调用.
   * 导出方法符合: public, 非 static, 非继承的.
   */
	static public class ExportsFunction implements IConfig {
    @Override
    public void config(Helper target) {
      Class<?> myself = target.getClass();
      Method[] methods = myself.getDeclaredMethods();

      for (int i=0; i<methods.length; ++i) {
        Method m = methods[i];
        final String name = m.getName();

        int mod = m.getModifiers();
        if (Modifier.isPublic(mod) == false || Modifier.isStatic(mod)) {
          // System.out.println("Skip Export " + name +"(..) on " + myself);
          continue;
        }

        if (target.hasMember(name))
          continue;

        AbstractJSObject inv = new FunctionProxy(myself, name);
        target.addFunction(name, inv);
      }
    }
  }


  /**
   * 包装一个函数作为 js 对象的属性
   */
  static public class FunctionProxy extends AbstractJSObject {
	  private Method method;      // 可以空, 用来确定函数对象
	  private Object thiz;        // 可以空, 用来保存闭包中的 this
	  private Class<?> clazz;     // 必须设置
	  private String methodname;  // 必须设置

    /**
     * 固定函数代理
     */
    public FunctionProxy(Object thiz, Method method) {
      this.thiz = thiz;
      this.method = method;
      this.clazz = thiz.getClass();
      this.methodname = method.getName();
    }

    /**
     * 动态函数代理, 在调用时, 通过函数参数来确定对应的重载函数
     */
    public FunctionProxy(Class<?> clazz, String methodname) {
      this.thiz = null;
      this.method = null;
      this.clazz = clazz;
      this.methodname = methodname;
    }

    @Override
    public Object call(Object thiz, Object... args) {
      try {
        Method _m = method;
        if (_m == null) {
          _m = clazz.getMethod(methodname,
                  Tool.getClasses(args, default_convert));
        }
        if (thiz == null) {
          thiz = this.thiz;
        }
        return _m.invoke(thiz, args);

      } catch(InvocationTargetException e) {
        // 函数抛出的原始被 InvocationTargetException 包装
        Throwable cause = e.getCause();

        if (cause instanceof IndexOutOfBoundsException) {
          throwJSRangeError((IndexOutOfBoundsException) cause);
        } else if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        } else if (cause == null) {
          cause = e;
        }

        cause.addSuppressed(new Exception(
                clazz.getName() + "." + methodname + "(..)"));
        throwJSReferenceError(cause);

      } catch(NoSuchMethodException | IllegalAccessException e) {
        throwJSReferenceError(e);
      }
      return null;
    }

    public boolean isFunction() {
      return true;
    }
  }


  /**
   * 在 js 对象上绑定 attrname 属性, 对属性的赋值调用 java 中的
   * setter 方法, 获取 attrname 属性的值调用 java 中的 getter 方法,
   * java 中的这个属性是虚拟的, getter/setter 必须完整,
   * 数据类型通过 getter 方法的返回值来确定;
   * 每个 ExportsAttribute 只负责一个属性.
   */
  static public class ExportsAttribute implements IConfig {
    private Method getter;
    private Method setter;
    private String attrname;
    private Object thiz;

    /**
     * 绑定 attrname 属性
     */
    public ExportsAttribute(String attrname) {
      this.attrname = attrname;
    }

    @Override
    public void config(Helper target) {
      try {
        Class<?> who = target.getClass();

        String uf = Tool.upperFirst(attrname);
        getter = who.getMethod("get" + uf);

        Class<?>[] setArgs =
                new Class<?>[]{getter.getReturnType()};
        setter = who.getMethod("set" + uf, setArgs);

        target.addDynAttr(attrname, this);
        thiz = target;
      } catch(Exception e) {
        throw new RuntimeException(e);
      }
    }

    public void set(Object o) {
      try {
        setter.invoke(thiz, o);
      } catch (Exception e) {
        throw new RuntimeException(
                thiz.getClass() + "." + setter.getName() , e);
      }
    }

    public Object get() {
      try {
        return getter.invoke(thiz);
      } catch (Exception e) {
        throw new RuntimeException(
                thiz.getClass() + "." + getter.getName() , e);
      }
    }
  }


  /**
   * 转换器的容器
   */
  static public class JSToJava implements IConversion<Object, Object> {
	  private Map<Class, IConversion<Object,Object>> map;

    JSToJava() {
      map = new HashMap<>();
    }

    public Object value(Object obj) {
      IConversion<Object,Object> c = map.get(obj.getClass());
      if (c != null) {
        return c.value(obj);
      }
      return obj;
    }

    public Class<?> type(Class<?> _class) {
      IConversion<Object,Object> c = map.get(_class);
      if (c != null) {
        return c.type(_class);
      }
      return _class;
    }

    public void add(IConversionDesc desc) {
      map.put(desc.valueClass(), desc);
    }
  }


  /**
   * 将原始类型的打包类型, 映射为原始类型
   */
  static public class PrimitiveConvert implements IConversionDesc {
    private Class<?> from;
    private Class<?> to;

    public PrimitiveConvert(Class<?> from, Class<?> to) {
      this.from = from;
      this.to   = to;
    }
    public Class<?> valueClass() {
      return from;
    }
    public Object value(Object obj) {
      return obj;
    }
    public Class<?> type(Class<?> c) {
      return to;
    }
  }


  /**
   * 将 js 数组转换为 java 对象数组
   */
  static public class _JSArray implements IConversionDesc {
    public Class<?> valueClass() {
      return NativeArray.class;
    }
    public Object value(Object obj) {
      NativeArray na = (NativeArray) obj;
      return na.asObjectArray();
    }
    public Class<?> type(Class<?> _class) {
      return Object[].class;
    }
  }


}
