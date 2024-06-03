////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-8 下午12:19
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/SourceFix.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.fix;

import com.xboson.app.fix.state.*;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.Tool;


/**
 * 代码修正.
 */
public class SourceFix {

  public static final char[] STRICT_MODE = "\"use strict\"".toCharArray();


  /**
   * 自动给代码打补丁, 该方法返回的代码适合给机器运行.
   * 返回后参数 content 会被改动但不是最终代码, 应该丢弃.
   *
   * 补丁规则:
   *    1. 去掉首位 "<%...%>"
   *    2. 是严格模式则不做补丁.
   *    3. 不是严格模式, 修正脚本中的方言.
   */
  public static byte[] autoPatch(byte[] content) {
    if (SourceFix.fixBeginEnd(content)) {
      if (SourceFix.isStrictMode(content) == false) {
        content = SourceFix.fixFor(content);
        content = SourceFix.fixJavaCall(content);
        content = SourceFix.fixVirtualAttr(content);
      }
    }
    content = multiLineString(content);
    return content;
  }


  /**
   * 去掉脚本的前后特殊符号 "<%...%>",
   * 如果执行了修正操作返回 true;
   *
   * 该方法仅适合替换运行时的代码, 不加入换行另出错后的行数正确,
   * 但是输出到 ide 后多出的空格, 让开发者以为代码被改过.
   */
  public static boolean fixBeginEnd(byte[] content) {
    int len = content.length - 1;
    if (isDrag(content)) {
      content[0    ] = 32; // 13 = CR (carriage return) 回车键
      content[1    ] = 32; // 32 = (space) 空格
      content[len-1] = 13; // 10 = LF 换行
      content[len  ] = 10;
      return true;
    }
    return false;
  }


  /**
   * 带有前后 "<%...%>" 符号返回 true
   */
  public static boolean isDrag(byte[] content) {
    if (content == null || content.length < 4)
      return false;

    int len = content.length - 1;
    return (content[0] == 60
            && content[1] == 37
            && content[len-1] == 37
            && content[len] == 62);
  }


  /**
   * 如果代码使用了严格模式, 则返回 true,
   * 严格模式必须在第一行声明字符串 "use strict"
   */
  public static boolean isStrictMode(byte[] content) {
    int i = 0, g = 0;
    while (i < content.length && content[i] != '\n') {
      if (content[i] == STRICT_MODE[g]) {
        if (++g >= STRICT_MODE.length)
          return true;
      } else {
        g = 0;
      }
      ++i;
    }
    return false;
  }


  /**
   * 修正 beetl 中 for 循环与 js 不兼容.
   * @see com.xboson.app.fix.state.S_For_Output
   */
  public static byte[] fixFor(byte[] content) {
    int size = (int) (content.length * 1.7);
    StringBufferOutputStream buf = new StringBufferOutputStream(size);

    SState[] all_state = new SState[] {
            new S_for(),
            new S_Space(),
            new S_BeginBrackets(),
            new S_Space(),
            new S_KeyVar(),
            new S_Space(),
            new S_Symbol(0),
            new S_Space(),
            new S_KeyIN(),
            new S_Space(),
            new S_Expression(1),
            new S_EndBrackets(),
            new S_SpaceEnter(),
            new S_BeginScope(),
            new S_For_Output(0, 1),
    };

    JsParser.rewrite(content, buf, all_state, 2);
    return buf.toBytes();
  }


  /**
   * 修正 beetl 中对象的 java 函数调用语法,
   * 语法: @object.func(args0, args1)
   * 重写为: __inner_call("func-name:func", object, args0, args1)
   */
  public static byte[] fixJavaCall(byte[] content) {
    int size = (int) (content.length * 1.7);
    StringBufferOutputStream buf = new StringBufferOutputStream(size);

    SState[] all_state = new SState[] {
            new S_BeginNotation('@'),
            new S_Symbol(0),
            new S_Notation('.'),
            new S_Symbol(1),
            new S_BeginBrackets(),
            new S_DynArgument(2),
            new S_EndBrackets(),
            new S_JavaCallOutput(0, 1, 2),
    };

    JsParser.rewrite(content, buf, all_state, 3);
    return buf.toBytes();
  }


  /**
   * 修正 beetl 中虚拟属性
   * 语法: object.~size
   * 重写为: __virtual_attr(object, "attr_name:size")
   */
  public static byte[] fixVirtualAttr(byte[] content) {
    int size = (int) (content.length * 1.7);
    StringBufferOutputStream buf = new StringBufferOutputStream(size);

    SState[] all_state = new SState[] {
            new S_BeginSymbol(0),
            new S_Notation('.'),
            new S_Notation('~'),
            new S_Symbol(1),
            new S_VirtualAttr(0, 1),
    };

    JsParser.rewrite(content, buf, all_state, 2);
    return buf.toBytes();
  }


  /**
   * 支持 ES6 多行字符串简化语法, '`' 符号作为开始, '`' 作为结束,
   * 不支持动态变量 ${varName}.
   */
  public static byte[] multiLineString(byte[] content) {
    int size = (int) (content.length * 1.7);
    StringBufferOutputStream buf = new StringBufferOutputStream(size);

    S_BeginMultiLineString multiLine = new S_BeginMultiLineString();

    SState[] all_state = new SState[] {
            multiLine.createBegin(),
            multiLine,
    };

    JsParser.rewrite(content, buf, all_state, 2);
    return buf.toBytes();
  }
}
