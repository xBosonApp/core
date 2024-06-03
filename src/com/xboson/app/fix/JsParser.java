////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-13 下午6:35
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/fix/JsParser.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.fix;

import com.xboson.been.XBosonException;
import com.xboson.util.StringBufferOutputStream;

import java.io.IOException;


/**
 * JavaScript 解析器
 */
public class JsParser implements IStateOperator {


  /**
   * 读取 content, 根据 role 中指定的规则重写数据并输出到 output
   * @param content 输入代码
   * @param output 输出代码
   * @param role 重写规则状态机
   * @param data_count 状态机使用的数据数量
   */
  public static void rewrite(byte[] content,
                             StringBufferOutputStream output,
                             SState[] role,
                             int data_count)
  {
    int size = (int) (content.length * 1.7);
    int step = 0;
    int resetIndex = 0;
    int stateIndex = 0;
    boolean in_quotation = false;
    boolean in_escape = false;
    byte quotation_mark = 0;

    String[] data = new String[data_count];
    ISState currentState = role[0];

    for (int i=0; i<role.length; ++i) {
      role[i].setOutput(output);
      role[i].setData(data);
    }

    try {
      int directive = INIT;

      for (int i = 0; i < content.length; ++i) {
        byte ch = content[i];

        //
        // 0 号状态机具有 js 注释/字符串范围 检测,
        // 其他状态机必须自己处理所有字符.
        //
        if (step == 0) {
          //
          // 当在字符串中, 不做任何额外的处理
          //
          if (in_quotation) {
            //
            // '\' 符号启动转义, 在转义后的任何一个符号都被吃掉
            //
            if (in_escape) {
              in_escape = false;
            }
            else if (ch == '\\') {
              // 一定不再转义符后面
              in_escape = true;
            }
            else if (ch == quotation_mark) {
              // 一定不再转义符后面
              in_quotation = false;
            }

            output.write(ch);
            continue;
          }
          else if (ch == '\'' || ch == '\"') {
            in_quotation = true;
            quotation_mark = ch;
            output.write(ch);
            continue;
          }
          //
          // 跳过单行注释
          //
          if (ch == '/' && i<content.length-1 && content[i+1] == '/') {
            int begin = i;
            while (i < content.length && content[i] != '\n') {
              output.write(content[i]);
              ++i;
            }
            output.write('\n');
            continue;
          }
          //
          // 跳过多行注释
          //
          if (ch == '/' && i<content.length-1 && content[i+1] == '*') {
            int begin = i;
            output.write('/');
            output.write('*');
            i += 2;
            while (i < content.length) {
              output.write(content[i]);
              if (content[i-1] == '*' && content[i] == '/') {
                break;
              }
              ++i;
            }
            continue;
          }
        }

        //
        // 让状态机读取一个字符并指示下一步操作
        //
        directive = currentState.read(ch);

        if (directive == BEGIN) {
          stateIndex = i;
          resetIndex = i;
          continue;
        }
        else if (directive == KEEP) {
          continue;
        }
        else if (directive >= NEXT) {
          if (directive == NEXT_AND_BACK_ALL) {
            i = stateIndex - 1;
          }
          else if (directive == NEXT_AND_BACK) {
            --i;
          } else /* if is NEXT */ {
            stateIndex = i+1;
          }

          if (++step >= role.length) {
            step = 0;
          }
          currentState = role[step];
          continue;
        }
        else if (directive == RESET) {
          while (resetIndex < i) {
            output.write(content[resetIndex]);
            ++resetIndex;
          }
          step = 0;
          currentState = role[step];
        }
        else if (directive == END) {
          stateIndex = i;
          resetIndex = i;
          step = 0;
          currentState = role[step];
        }

        // if: directive == NOTHING or END or RESET
        output.write(ch);
      }

      if (currentState instanceof ILastRunning) {
        currentState.read((byte) 0);
      }
    } catch(IOException e) {
      throw new XBosonException(e);
    }
  }
}
