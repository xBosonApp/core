////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-24 下午5:39
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/ChineseInital.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;


import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 中文字符串变英文首字母串, 带缓存
 */
public class ChineseInital {

  private final static int[] areaCode = {1601, 1637, 1833, 2078, 2274,
          2302, 2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858,
          4027, 4086, 4390, 4558, 4684, 4925, 5249, 5590};

  private final static String[] letters = {  "a", "b", "c", "d", "e",
          "f", "g", "h", "j", "k", "l", "m", "n", "o", "p", "q", "r",
          "s", "t", "w", "x", "y", "z"};

  private final static Map<String, String> cache = new ConcurrentHashMap<>();


  static {
    // 算法部不认识的字
    cache.put("佝", "g");
    cache.put("偻", "l");
    cache.put("囟", "x");
    cache.put("酰", "x");
    cache.put("孢", "b");
    cache.put("苷", "g");
    cache.put("酯", "z");
    cache.put("喹", "k");
    cache.put("呋", "f");
    cache.put("喃", "n");
    cache.put("瘾", "y");
    cache.put("痫", "x");
    cache.put("癫", "d");
    cache.put("祛", "q");
    cache.put("厥", "j");
    // 符号
    cache.put("、", "");
    cache.put("。", "");
    cache.put("，", "");
    cache.put("；", "");
    cache.put("？", "");
    cache.put("！", "");
  }


  /**
   * 取得给定汉字串的首字母串
   */
  public static String getAllFirstLetter(String str) {
    if (str == null || str.trim().length() == 0) {
      return null;
    }
    StringBuilder out = new StringBuilder();
    try {
      for (int i = 0; i < str.length(); i++) {
        String cn = str.substring(i, i + 1);
        String en = cache.get(cn);
        if (en != null) {
          out.append(en);
        } else {
          getFirstLetter(cn, out);
        }
      }
    } catch(Exception e) {
      System.err.println(e);
    }
    return out.toString();
  }


  /**
   * 取得给定汉字的首字母,即声母
   */
  public static void getFirstLetter(String cn_src, StringBuilder out)
          throws UnsupportedEncodingException {
    if (cn_src == null || cn_src.trim().length() == 0)
      return;

    String cn = new String(cn_src.getBytes("GB2312"), "ISO8859-1");

    if (cn.length() > 1) {
      int li_SecPosCode = (((int) cn.charAt(0)) - 160) * 100
                        + (((int) cn.charAt(1)) - 160);

      if (li_SecPosCode > 1600 && li_SecPosCode < 5590) {
        for (int i = 0; i < 23; i++) {
          if (li_SecPosCode >= areaCode[i] && li_SecPosCode < areaCode[i + 1]) {
            out.append(letters[i]);
            cache.put(cn_src, letters[i]);
            break;
          }
        }
      } else {
        out.append(cn_src);
      }
    }
  }

}