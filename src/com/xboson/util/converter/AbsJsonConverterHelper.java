////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-5-30 下午3:59
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/converter/IConverter.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util.converter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.xboson.been.XBosonException;

import java.io.IOException;
import java.lang.reflect.Constructor;


/**
 * JsonAdapter 的默认实现, 如果 T 类型需要转换为 JSON 字符串则继承该对象,
 * 实现后必须在 ConverterInitialization 中注册方可生效.
 *
 * @see com.xboson.util.ConverterInitialization
 */
public abstract class AbsJsonConverterHelper<T> extends JsonAdapter<T> {


  /**
   * 该方法默认注册 T 类型的适配器
   */
  public void register(Moshi.Builder builder) {
    builder.add(classType(), this);
  }


  /**
   * 返回 T 类型的 Class 对象
   */
  abstract Class<T> classType();


  /**
   * 如果 T 有一个带有 String 参数的构造函数, 则默认实现可以满足需要
   * @see JsonAdapter#fromJson
   */
  @Override
  public T fromJson(JsonReader jsonReader) throws IOException {
    try {
      Constructor<T> creator = classType().getConstructor(String.class);
      T obj = creator.newInstance(jsonReader.nextString());
      return obj;
    } catch (Exception e) {
      throw new XBosonException(e);
    }
  }


  /**
   * 如果 T 的 toString() 方法就是 json 序列化则默认实现可以满足需要.
   * @see JsonAdapter#toJson
   */
  @Override
  public void toJson(JsonWriter jsonWriter, T obj)
          throws IOException {
    jsonWriter.value(obj.toString());
  }

}
