////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-10 上午10:32
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/analyze/SqlParser.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db.analyze;

import com.xboson.db.analyze.unit.*;

import java.util.Iterator;


/**
 * sql 词法分析器, 不做任何语法检查
 *
 * (不要尝试在遍历 ParsedData 时用 class 直接判断代替 instanceof,
 *  那样反而会降低速度, 并且在继承上容易出错.)
 */
public class SqlParser {

  /**
   * 不是完整的词法解析器实现, 仅用于替换表格名称.
   */
  public static ParsedData parse(String sql) {
    ParsedData pd = new ParsedData();

    int size = sql.length();
    int begin = 0;
    boolean inString = false;

    for (int i=0; i<size; ++i) {
      char ch = sql.charAt(i);
      if (ch == '\r') continue;

      if (ch == '\'' || ch == '\"') {
        inString = !inString;
      }

      if (SqlKeyWords.notation.contains(ch)) {
        if (i > begin) {
          String unit = sql.substring(begin, i).trim();
          if (unit.length() > 0) {
            if (inString) {
              pd.addExp(unit);
            } else {
              pd.add(unit);
            }
          }
        }
        begin = i+1;

        switch (ch) {
          case ' ':
          case '\t':
            pd.add(new Blank(ch));
            break;
          case '(':
            pd.add(new BeginBrackets());
            break;
          case ')':
            pd.add(new EndBrackets());
            break;
          case '\n':
            pd.add(new NewLine());
            break;
          case ',':
            pd.add(new Comma());
            break;
          case '{':
            pd.add(new BeginBigParantheses());
            break;
          case '}':
            pd.add(new EndBigParantheses());
            break;
          case ';':
            pd.add(new Semicolon());
            break;
        }
      }
    }

    if (begin < size) {
      String unit = sql.substring(begin, size).trim();
      if (unit.length() > 0) {
        pd.add(unit);
      }
    }

    pd.lock();
    return pd;
  }


  /**
   * 迭代解析后的 sql 指令, 将所有 '表格名称' 相关的字段发送给 ul
   */
  public static void tableNames(SqlContext ctx, ParsedData pd, IUnitListener ul) {
    Iterator<IUnit> it = pd.getUnits().iterator();
    int name_flag = 0;

    while (it.hasNext()) {
      IUnit un = it.next();

      if (un instanceof Comma || un instanceof KeyWord) {
        name_flag = 0;
        continue;
      }

      if (name_flag == 0
              && un instanceof Expression
              && un.getParent() instanceof TableNames) {
        ul.on(ctx, un);
        ++name_flag;
      }
    }
  }


  /**
   * 如果 sql 中有 order by 返回 null, 否则返回 select 中的第一个列名.
   */
  public static String orderOrName(ParsedData pd) {
    Iterator<IUnit> it = pd.getUnits().iterator();
    String tableName = null;
    int flag = 0;

    while (it.hasNext()) {
      IUnit un = it.next();

      if (un instanceof KeyWord) {
        KeyWord key = (KeyWord) un;
        if (flag == 0 && key.upperKey.equals("SELECT")) {
          flag = 1;
        } else if (key.upperKey.equals("ORDER")) {
          return null;
        } else {
          flag = 3;
        }
        continue;
      }

      if (flag == 1 && un instanceof Expression) {
        tableName = (String) un.getData();
        if (tableName.equals("*")) {
          tableName = null;
        } else {
          flag = 3;
        }
      }
    }

    if (tableName == null) {
      throw new ParseException(
              "Can not find the column used to sort, " +
              "and not limit the results.");
    }
    return tableName;
  }


  /**
   * 删除 select 中的 order
   */
  public static String removeOrder(ParsedData pd) {
    StringBuilder out = new StringBuilder();
    SqlContext ctx = new SqlContext();

    for (IUnit un : pd.getUnits()) {
      if (un instanceof KeyWord) {
        if ( ((KeyWord) un).upperKey.equals("ORDER")) {
          break;
        }
      }
      out.append(un.stringify(ctx));
    }
    return out.toString();
  }


  /**
   * @see #tableNames(SqlContext, ParsedData, IUnitListener)
   */
  public static void tableNames(SqlContext ctx,
                                SqlParserCached.ParsedDataHandle handle,
                                IUnitListener ul) {
    tableNames(ctx, handle.pd, ul);
  }


  /**
   * @see #orderOrName(ParsedData)
   */
  public static String orderOrName(SqlParserCached.ParsedDataHandle handle) {
    return orderOrName(handle.pd);
  }


  /**
   * @see #removeOrder(ParsedData)
   */
  public static String removeOrder(SqlParserCached.ParsedDataHandle handle) {
    return removeOrder(handle.pd);
  }


  /**
   * 将语法树还原为 sql 文, 部分组件将使用 ctx 中提供的变量改变 sql 的输出结果
   */
  public static String stringify(SqlContext ctx, ParsedData pd) {
    StringBuilder out = new StringBuilder();
    Iterator<IUnit> it = pd.getUnits().iterator();
    while (it.hasNext()) {
      out.append(it.next().stringify(ctx));
    }
    return out.toString();
  }


  /**
   * @see #stringify(SqlContext, ParsedData)
   */
  public static String stringify(SqlContext ctx,
                                 SqlParserCached.ParsedDataHandle handle) {
    return stringify(ctx, handle.pd);
  }
}
