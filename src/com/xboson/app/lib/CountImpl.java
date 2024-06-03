////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-10-20 下午1:39
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/CountImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.auth.PermissionException;
import com.xboson.sleep.RedisMesmerizer;
import com.xboson.util.AES2;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 统计工具, 时间数据快速索引
 */
public class CountImpl {

  public final static int DB_NUM = 1;

  private Map<String, String> passCache;
  private CTYPE[] allType = new CTYPE[10];

  /** 总计 */
  public final CTYPE TOTAL = new CTYPE(0, "-", 0, 0);
  /** 全年统计 */
  public final CTYPE YEAR  = new CTYPE(1, "YYYY", 0, 0);
  /** 月统计 */
  public final CTYPE MONTH = new CTYPE(2, "YYYY-MM", 0, 12);
  /** 每日统计 */
  public final CTYPE DAY   = new CTYPE(3, "YYYY-MM-dd", 0, 31);
  /** 每天小时统计 */
  public final CTYPE HOUR  = new CTYPE(4, "YYYY-MM-dd HH", 0, 24);

  /** 固定月总计 */
  public final CTYPE FIX_MONTH    = new CTYPE(5, "MM", 0, 12);
  /** 固定日总计(月) */
  public final CTYPE FIX_DAY_MON  = new CTYPE(6, "dd", 0, 31);
  /** 固定日总计(年) */
  public final CTYPE FIX_DAY_YEAR = new CTYPE(7, "DDD", 0, 365);
  /** 固定小时总计 */
  public final CTYPE FIX_HOUR     = new CTYPE(8, "HH", 0, 24);
  /** 固定周总计(年) */
  public final CTYPE FIX_WEEK     = new CTYPE(9, "ww", 0, 53);


  public CountImpl() {
    passCache = Collections.synchronizedMap(new WeakHashMap<>());
  }


  /**
   * 创建实例, 返回访问数据的密钥
   */
  public String create(String key) throws Exception {
    String pass = Tool.randomString(15);
    String val  = Tool.randomString(100);
    String enc  = new AES2(pass).encrypt(val);
    transaction((j) -> {
      j.set("C."+ key +".ENC", enc);
      j.set("C."+ key +".VAL", val);
    });
    return pass;
  }


  private void checkPass(String key, String pass) throws Exception {
    String p = passCache.get(key);

    if (p == null) {
      openRedis((j) -> {
        String enc = j.get("C."+ key + ".ENC");
        String val = j.get("C."+ key + ".VAL");
        String dec = new String(new AES2(pass).decrypt(enc), IConstant.CHARSET);

        if (!val.equals(dec)) {
          throw new PermissionException("bad password");
        }
        passCache.put(key, pass);
        return null;
      });
    } else if (!p.equals(pass)) {
      throw new PermissionException("bad password");
    }
  }


  /**
   * 在当前时间点上增加一次实例的访问计数
   */
  public void inc(String key) throws Exception {
    inc(key, new Date());
  }


  public void inc(String key, Date d) throws Exception {
    transaction((j) -> {
      for (CTYPE c : allType) {
        j.incr(c.key_data_full(key, d));
      }
      j.sadd(YEAR.key_year_index(key), YEAR.format(d));
    });
  }


  /**
   * 使用访问密钥打开查询
   * @param key 统计对象主键
   * @param pass 访问密钥
   * @return
   */
  public Search openSearch(String key, String pass) throws Exception {
    return new Search(key, pass);
  }


  private List<Object> transaction(IDO d) throws Exception {
    try (Jedis j = RedisMesmerizer.me().open();
         Transaction t = j.multi()) {
      int dbn = j.getDB().intValue();
      t.select(DB_NUM);
      d.o(t);
      t.select(dbn);
      return t.exec();
    }
  }


  private Object openRedis(IDO2 d) throws Exception {
    int dbn = 0;
    Jedis j = null;
    try {
      j = RedisMesmerizer.me().open();
      dbn = j.getDB().intValue();
      j.select(DB_NUM);
      return d.o(j);
    } finally {
      if (j != null) {
        try {
          j.select(dbn);
        } finally {
          j.close();
        }
      }
    }
  }


  interface IDO {
    void o(Transaction t) throws Exception;
  }


  interface IDO2 {
    Object o(Jedis j) throws Exception;
  }


  public class CTYPE {
    private int num;
    private String p;
    private SimpleDateFormat f;
    private String[] rangeKey;

    private CTYPE(int n, String pattern, int begin, int end) {
      num = n;
      f = new SimpleDateFormat(pattern);
      p = pattern;
      allType[num] = this;
      rangeKey = createRange(begin, end);
    }

    private String key_data_full(String key, Date d) {
      return "C."+ key +".D."+ num +"."+ format(d);
    }

    private synchronized String key_year_index(String key) {
      return "C."+ key +".Y.IDX";
    }

    /** 不要直接调用 f.format 而是调用该方法 */
    private synchronized String format(Date d) {
      return f.format(d);
    }

    private String[] createRange(int begin, int end) {
      String[] ret = new String[end-begin];
      NumberFormat nf = new DecimalFormat(
              end < 10 ? "0":(end < 100? "00":"000"));

      for (int i=begin; i<end; ++i) {
        ret[i] = nf.format(i);
      }
      return ret;
    }
  }


  public class Search {
    private String key;


    private Search(String key, String pass) throws Exception {
      checkPass(key, pass);
      this.key = key;
    }


    /**
     * 返回 type 类型的计数器的值, 精确匹配
     */
    public Object get(CTYPE type, Date d) throws Exception {
      return openRedis((j) -> {
        return j.get( type.key_data_full(key, d) );
      });
    }


    /**
     * 返回 type 类型的计数器的值, 范围匹配
     */
    public Object range(CTYPE type, Date d) throws Exception {
      return openRedis((j) -> {
        String prefix;
        String[] keys = type.rangeKey;

        switch (type.num) {
          case 0:
          case 1:
            prefix = "C."+ key +".D.1.";
            Set<String> ret = j.smembers(type.key_year_index(key));
            keys = ret.toArray(new String[ret.size()]);
            break;

          case 2:
          case 3:
            prefix = "C."+ key +".D."+ type.num +"."
                    + allType[type.num-1].format(d) +"-";
            break;

          case 4:
            prefix = "C."+ key +".D."+ type.num +"."
                    + allType[type.num-1].format(d) +" ";
            break;

          case 5:
          case 6:
          case 7:
          case 8:
          case 9:
            prefix = "C."+ key +".D."+ type.num +".";
            break;

          default:
            throw new Exception("Unknow type");
        }

        Map<String, String> ret = new HashMap<>(keys.length);
        for (String k : keys) {
          String v = j.get(prefix + k);
          ret.put(k, v == null? IConstant.ZERO_STR: v);
        }
        return ret;
      });
    }
  }
}
