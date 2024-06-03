////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-10 上午10:44
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestSqlParser.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.db.analyze.*;
import com.xboson.util.Tool;


public class TestSqlParser extends Test {

  public void test() throws Throwable {
    testTableName();
    testPage();
    multThread();
  }


  public void multThread() {
    final int threadCount = 8;
    final int loopCount = 10000;
    Thread[] all = new Thread[threadCount];

    final String comm = "; -- "+
            Tool.randomString2(SqlParserCached.CACHE_SQL_LENGTH);
    final String sql = "Select * from a"+ comm;
    msg("SQL:", sql);

    for (int i=0; i<threadCount; ++i) {
      all[i] = new Thread(() -> {
        final String prefix = Tool.randomString2(3) +".";
        final String right = "Select * from "+ prefix +"a"+ comm;
        msg("Prefix:", prefix, ", SQL:", right);
        SqlContext ctx = new SqlContext();

        for (int c=0; c<loopCount; ++c) {
          SqlParserCached.ParsedDataHandle handle
                  = SqlParserCached.parse(sql);
          SqlParser.tableNames(ctx, handle, new ReplaseSchema(prefix));
          String xsql = SqlParser.stringify(ctx, handle);

          if (! right.equals(xsql)) {
            fail("Wrong On Thread", c, ":\n\t",
                    xsql, "\n!=\n\t", right);
            break;
          }
        }
      });
      sub("Thread", i, "start");
      all[i].start();
    }

    for (int i=0; i<threadCount; ++i) {
      try {
        all[i].getStackTrace();
        all[i].join();
        all[i].getUncaughtExceptionHandler();
        success("Thread", i, "quit");
      } catch (InterruptedException e) {
        fail("Thread", i, e.getMessage());
      }
    }
  }


  public void testPage() throws Throwable {
    columnName("select * from test1 Order By id ", null);
    columnName("select a from test1 ", "a");
    columnName("select a,b,c from test1 ", "a");
    columnName("select a,b,c from test1 Order By a", null);

    new Throws(ParseException.class) {
      @Override
      public void run() throws Throwable {
        columnName("select * from test1", null);
      }
    };
  }


  private void columnName(String sql, String cn) {
    sub("Column Name");
    msg("[SQL]", sql);
    msg("find:", cn);
    ParsedData pd = SqlParser.parse(sql);
    String tableName = SqlParser.orderOrName(pd);
    if (tableName != cn) {
      eq(tableName, cn, "columnName");
    }
    msg("ok");
  }


  public void testTableName() throws Throwable {
    testSqlParse("SELECT * FROM t1 INNER JOIN t2",
            "t1", "t2");
    testSqlParse("SELECT t1.*, t2.* FROM t1 INNER JOIN t2",
            "t1", "t2");
    testSqlParse("SELECT AVG(score), t1.* FROM t1",
            "t1");
    testSqlParse("SELECT CONCAT(last_name,', ',first_name) AS full_name\n" +
            "  FROM mytable ORDER BY full_name;",
            "mytable");
    testSqlParse("SELECT CONCAT(last_name,', ',first_name) full_name\n" +
            "  FROM mytable ORDER BY full_name;",
            "mytable");
    testSqlParse("SELECT t1.name, t2.salary FROM employee AS t1, info AS t2\n" +
            "  WHERE t1.name = t2.name;",
            "employee", "info");
    testSqlParse("SELECT t1.name, t2.salary FROM employee t1, info t2\n" +
            "  WHERE t1.name = t2.name;",
            "employee", "info");
    testSqlParse("SELECT college, region, seed FROM tournament\n" +
            "  ORDER BY region, seed;",
            "tournament");
    testSqlParse("SELECT a, COUNT(b) FROM test_table GROUP BY a ORDER BY NULL;",
            "test_table");
    testSqlParse("SELECT COUNT(col1) AS col2 FROM t GROUP BY col2 HAVING col2 = 2;",
            "t");
    testSqlParse("SELECT user, MAX(salary) FROM users\n" +
            "  GROUP BY user HAVING MAX(salary) > 10;",
            "users");
    testSqlParse("SELECT 12 AS a, a FROM t GROUP BY a;",
            "t");
    testSqlParse("SELECT * FROM tbl LIMIT 5; ",
            "tbl");
    testSqlParse("SELECT t1.name, t2.salary\n" +
            "  FROM employee AS t1 INNER JOIN info AS t2 ON t1.name = t2.name;",
            "employee", "info");
    testSqlParse("SELECT t1.name, t2.salary\n" +
            "  FROM employee t1 INNER JOIN info t2 ON t1.name = t2.name;",
            "employee", "info");
    testSqlParse("SELECT * FROM (SELECT 1, 2, 3) AS t1;");
    testSqlParse("SELECT left_tbl.*\n" +
            "  FROM left_tbl LEFT JOIN right_tbl ON left_tbl.id = right_tbl.id\n" +
            "  WHERE right_tbl.id IS NULL;",
            "left_tbl", "right_tbl");
    testSqlParse("SELECT left_tbl.*\n" +
            "    FROM { OJ left_tbl LEFT OUTER JOIN right_tbl ON left_tbl.id = right_tbl.id }\n" +
            "    WHERE right_tbl.id IS NULL;",
            "left_tbl", "right_tbl");
    testSqlParse("SELECT * FROM table1 INNER JOIN table2 ON table1.id = table2.id;",
            "table1", "table2");
    testSqlParse("SELECT * FROM table1 LEFT JOIN table2 ON table1.id = table2.id;",
            "table1", "table2");
    testSqlParse("SELECT * FROM table1 LEFT JOIN table2 USING (id);",
            "table1", "table2");
    testSqlParse("SELECT * FROM t1 JOIN t2 USING (j);",
            "t1", "t2");
    testSqlParse("SELECT * FROM t1, t2 JOIN t3 ON (t1.i1 = t3.i3);",
            "t1", "t2", "t3");


    testSqlParse("ALTER TABLE t2 DROP COLUMN c, DROP COLUMN d;",
            "t2");
    testSqlParse("ALTER TABLE t1 ENGINE = InnoDB;",
            "t1");
    testSqlParse("ALTER TABLE t1 ROW_FORMAT = COMPRESSED;",
            "t1");
    testSqlParse("ALTER TABLE t1 ENCRYPTION='Y';",
            "t1");
    testSqlParse("ALTER TABLE t1 AUTO_INCREMENT = 13;",
            "t1");
    testSqlParse("ALTER TABLE t1 CHARACTER SET = utf8;",
            "t1");
    testSqlParse("ALTER TABLE t1 COMMENT = 'New table comment';",
            "t1");
    testSqlParse("ALTER TABLE t1 CHANGE a b BIGINT NOT NULL;",
            "t1");
    testSqlParse("ALTER TABLE t1 CHANGE b b INT NOT NULL;",
            "t1");
    testSqlParse("ALTER TABLE t1 MODIFY b INT NOT NULL;",
            "t1");
    testSqlParse("ALTER TABLE t1 MODIFY col1 BIGINT UNSIGNED " +
                    "DEFAULT 1 COMMENT 'my column';",
            "t1");
    testSqlParse("ALTER TABLE tbl_name DROP FOREIGN KEY fk_symbol;",
            "tbl_name");
    testSqlParse("ALTER TABLE tbl_name CONVERT TO CHARACTER SET charset_name;",
            "tbl_name");
    testSqlParse("ALTER TABLE t MODIFY latin1_varchar_col VARCHAR(M) " +
            "CHARACTER SET utf8;",
            "t");
    testSqlParse("ALTER TABLE tbl_name DEFAULT CHARACTER SET charset_name;",
            "tbl_name");


    testSqlParse("DELETE FROM somelog WHERE user = 'jcole'\n" +
            "ORDER BY timestamp_column LIMIT 1;",
            "somelog");
    testSqlParse("INSERT INTO t_copy SELECT * FROM t WHERE",
            "t_copy", "t");
    testSqlParse("DELETE t1, t2 FROM t1 INNER JOIN t2 INNER JOIN t3\n" +
            "WHERE t1.id=t2.id AND t2.id=t3.id;",
            "t1", "t2", "t3");
    testSqlParse("DELETE FROM t1, t2 USING t1 INNER JOIN t2 INNER JOIN t3\n" +
            "WHERE t1.id=t2.id AND t2.id=t3.id;",
            "t1", "t2", "t2", "t3");
    testSqlParse("DELETE t1 FROM t1 LEFT JOIN t2 ON t1.id=t2.id WHERE t2.id IS NULL;",
            "t1", "t2");
    testSqlParse("DELETE t1 FROM test AS t1, test2 WHERE",
            "test", "test2");
    testSqlParse("DELETE a1, a2 FROM t1 AS a1 INNER JOIN t2 AS a2\n" +
            "WHERE a1.id=a2.id;",
            "t1", "t2");

    // 不支持 using 子句
//    testSqlParse("DELETE FROM a1, a2 USING t1 AS a1 INNER JOIN t2 AS a2\n" +
//            "WHERE a1.id=a2.id;",
//            "a1", "a2", "t1");
    // 不支持 TO
//    testSqlParse("RENAME TABLE t TO t_old, t_copy TO t;",
//            "t", "t_old", "t_copy", "t");


    testSqlParse("INSERT INTO t1 VALUES(1, 1);",
            "t1");
    testSqlParse("INSERT INTO tbl_name (col1,col2) VALUES(15,col1*2);",
            "tbl_name");
    testSqlParse("INSERT INTO tbl_name (a,b,c) VALUES(1,2,3),(4,5,6),(7,8,9);",
            "tbl_name");


    testSqlParse("UPDATE t1 SET col1 = col1 + 1;",
            "t1");
    testSqlParse("UPDATE t1 SET col1 = col1 + 1, col2 = col1;",
            "t1");
    testSqlParse("UPDATE t SET id = id + 1 ORDER BY id DESC;",
            "t");
    testSqlParse("UPDATE items,month SET items.price=month.price\n" +
            "WHERE items.id=month.id;",
            "items", "month");


    testSqlParse("REPLACE INTO test VALUES (1, 'Old', '2014-08-20 18:47:00');",
            "test");
    testSqlParse("DELETE FROM t1\n" +
            "WHERE s11 > ANY\n" +
            " (SELECT COUNT(*) /* no hint */ FROM t2\n" +
            "  WHERE NOT EXISTS\n" +
            "   (SELECT * FROM t3\n" +
            "    WHERE ROW(5*t2.s1,77)=\n" +
            "     (SELECT 50,11*s1 FROM t4 UNION SELECT 50,77 FROM\n" +
            "      (SELECT * FROM t5) AS t5)));",
            "t1", "t2", "t3", "t4", "t5");


    testSqlParse("CREATE TABLE new_tbl LIKE orig_tbl;",
            "new_tbl", "orig_tbl");
    testSqlParse("CREATE TABLE new_tbl AS SELECT * FROM orig_tbl;",
            "new_tbl", "orig_tbl");
    testSqlParse("CREATE TABLE t (c CHAR(20) CHARACTER SET utf8 COLLATE utf8_bin);",
            "t");
    testSqlParse("CREATE TABLE test (blob_col BLOB, INDEX(blob_col(10)));",
            "test");
    testSqlParse("SELECT * FROM tbl_name WHERE auto_col IS NULL",
            "tbl_name");
    testSqlParse("CREATE TABLE lookup\n" +
            "  (id INT, INDEX USING BTREE (id))\n" +
            "  ENGINE = MEMORY;",
            "lookup");
    testSqlParse("CREATE TABLE t1 (\n" +
            "    c1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
            "    c2 VARCHAR(100),\n" +
            "    c3 VARCHAR(100) )\n" +
            "ENGINE=NDB\n" +
            "COMMENT=\"NDB_TABLE=READ_BACKUP=0,PARTITION_BALANCE=FOR_RP_BY_NODE\";",
            "t1");
    testSqlParse("CREATE TABLE t1 (col1 INT, col2 CHAR(5), col3 DATETIME)\n" +
            "    PARTITION BY HASH ( YEAR(col3) );",
            "t1");
    testSqlParse("CREATE TABLE t1 (i INT, j INT);",
            "t1");


    testSqlParse("DROP TABLE t_old;",
            "t_old");

    testSpeed();
  }


  private void testSqlParse(String sql, String...tableNames) {
    sub(sql, "(length:", sql.length() +")");
    SqlContext ctx = new SqlContext();

    try (SqlParserCached.ParsedDataHandle pd = SqlParserCached.parse(sql)) {
      ParseChecker pc = new ParseChecker(tableNames);
      SqlParser.tableNames(ctx, pd, pc);
      pc.checkCount();

      msg(SqlParser.stringify(ctx, pd));
    } catch (Error e) {
      throw e;
    }
  }


  /**
   *  100000 Used Time  1254 ms, Used Memory:95
   * 1000000 Used Time 10265 ms
   *
   * 加缓存:
   *  100000 Used Time   679 ms, Used Memory:109
   * 1000000 Used Time  4914 ms, Used Memory:490
   *
   * 不要尝试在遍历 ParsedData 时用 class 直接判断代替 instanceof,
   * 那样反而会降低速度, 并且在继承上容易出错.
   */
  private void testSpeed() throws Exception {
    String sql = "DELETE FROM t1\n" +
                    "WHERE s11 > ANY\n" +
                    " (SELECT COUNT(*) /* no hint */ FROM t2\n" +
                    "  WHERE NOT EXISTS\n" +
                    "   (SELECT * FROM t3\n" +
                    "    WHERE ROW(5*t2.s1,77)=\n" +
                    "     (SELECT 50,11*s1 FROM t4 UNION SELECT 50,77 FROM\n" +
                    "      (SELECT * FROM t5) AS t5)));";

    IUnitListener donothing = (ctx, unit) -> { return; };
    SqlContext ctx = new SqlContext();

    beginTime();
    int count = 100000;
    for (int i=0; i<count; ++i) {
      // 80% 的开销
      SqlParserCached.ParsedDataHandle pd = SqlParserCached.parse(sql);
      SqlParser.tableNames(ctx, pd, donothing);
      SqlParser.stringify(ctx, pd);
      pd.close();
    }
    endTime("Loop", count);
    memuse();
  }


  public static void main(String[] a) {
    new TestSqlParser();
  }


  class ParseChecker implements IUnitListener {
    String[] rightTableNames;
    int index = 0;

    ParseChecker(String[] rightTableNames) {
      this.rightTableNames = rightTableNames;
    }

    @Override
    public void on(SqlContext ctx, IUnit u) {
      String info = "at " + index +", "+ u + "  [" + u.getParent() + "]";
      if (index >= rightTableNames.length) {
        throw new AssertionError(info);
      }
      eq(rightTableNames[index], u.getData(), info);
      msg("--- OK", info);
      ++index;
      ctx.set(u, "<CHANGE>." + u.getData());
    }

    public void checkCount() {
      eq(rightTableNames.length, index, "table name count");
    }
  }


  class ReplaseSchema implements IUnitListener {

    String replaceSchemaPrefix;

    public ReplaseSchema(String name) {
      replaceSchemaPrefix = name;
    }

    @Override
    public void on(SqlContext ctx, IUnit u) {
      String tableName = (String) u.getData();
      ctx.set(u, replaceSchemaPrefix + tableName);
    }
  }
}
