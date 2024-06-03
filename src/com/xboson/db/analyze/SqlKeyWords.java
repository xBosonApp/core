////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-10 上午11:21
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/db/analyze/SqlKeyWords.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.db.analyze;

import com.xboson.util.JavaConverter;

import java.util.Set;


public class SqlKeyWords {

  static Set<String> key = JavaConverter.arr2setUpper(new String[] {
          "ACTION", "ADD", "AFTER", "ALL", "ALTER", "ACCESSIBLE",
          "AND", "ANY", "AS", "ASC", "AT",

          "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH",
          "BY", "BYTE",

          "CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER",
          "CHECK", "COLLATE", "COLUMN", "COMMENT", "COMMIT", "CONSTRAINT",

          "DATABASE", "DATABASES", "DECIMAL", "DECLARE", "DEFAULT", "DROP",
          "DELAYED", "DELETE", "DESC", "DESCRIBE", "DISTINCT", "DOUBLE",

          "EACH", "ELSE", "ELSEIF", "ENABLE", "END", "ENUM", "EXIT", "EXECUTE",

          "FALSE", "FETCH", "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FROM", "FULLTEXT",

          "GET", "GRANT", "GROUP",

          "HAVING",

          "IDENTIFIED", "IF", "IGNORE", "IN", "INDEX", "INNER", "INOUT", "INSERT",
          "INT", "INTEGER", "INTO", "EXISTS",

          "JOIN",

          "LIKE", "LIMIT", "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LEFT",

          "MATCH",

          "NCHAR", "NOT", "NULL", "NVARCHAR", "NUMERIC", "NUMBER",

          "OFFSET", "ON", "ONE", "OPTION", "OR", "ORDER", "OUT", "OUTER", "OWNER",

          "REAL", "REMOVE", "REPAIR", "REPEAT", "REPLACE", "RIGHT",

          "SCHEMA", "SELECT", "SET", "SMALLINT", "STRING",

          "TABLE", "TABLES", "TABLESPACE", "TEMPORARY", "TABLE_NAME", "TEXT",
          "THAN", "THEN", "TO", "TYPE", "TYPES",

          "UNION", "UNDEFINED", "UPDATE", "UPGRADE", "USE",

          "VALUE", "VALUES",

          "WHEN", "WHERE", "WHILE", "WITH", "WITHOUT",

          "XOR", "OJ",
  });


  /**
   * 该关键字不放入层级关系
   */
  static Set<String> skipParent = JavaConverter.arr2setUpper(new String[] {
          "IF", "NOT", "EXISTS", "LIKE", "LOW_PRIORITY", "DELAYED",
          "LOW_PRIORITY", "IGNORE", "PARTITION",
  });


  /**
   * 关键字表示表名称列表开始
   */
  static Set<String> beginTable = JavaConverter.arr2setUpper(new String[] {
          "TABLE", "INTO", "FROM", "REPLACE", "UPDATE",
          "JOIN", "OJ",
  });


  static Set<Character> notation = JavaConverter.arr2set(new Character[] {
          ' ', '\t', '\n', '(', ')', ',', '{', '}', ';',
  });

}
