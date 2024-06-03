////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-12-14 下午6:17
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/sleep/SafeDataFactory.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.sleep;

import com.xboson.app.lib.IApiConstant;
import com.xboson.auth.impl.RoleBaseAccessControl;
import com.xboson.util.AES2;
import com.xboson.util.Hex;
import com.xboson.util.Tool;
import com.xboson.util.Version;
import com.xboson.util.c0nst.IConstant;


/**
 * 在不同的场景对数据/键进行加密
 */
public class SafeDataFactory implements IConstant, IApiConstant {

  private static SafeDataFactory instance;

  private static final IEncryptionStrategy ENC_DEFAULT;
  private static final IEncryptionStrategy ENC_JDBC;
  private static final IEncryptionStrategy ENC_RBAC;

  public static final String SCENES_RBAC = RoleBaseAccessControl.RBAC_HKEY;
  public static final String SCENES_JDBC =
          _R_KEY_PREFIX_ + _CACHE_REGION_JDBC_CONNECTION_;


  static {
    ENC_DEFAULT = new IEncryptionStrategy() {};
    ENC_JDBC = new OnlyValueEnc("jdbc");
    ENC_RBAC = new RBACKeyEnc("rbac");
  }


  private SafeDataFactory() {
  }


  /**
   * 返回场景对应的加密策略, 如果策略不存在会返回默认策略(默认策略不加密)
   */
  public static IEncryptionStrategy get(String scenes) {
    IEncryptionStrategy r = getMaybeNull(scenes);
    if (r == null) return ENC_DEFAULT;
    return r;
  }


  /**
   * 返回场景对应的加密策略, 如果策略不存在会 null
   */
  public static IEncryptionStrategy getMaybeNull(String scenes) {
    // 由于选项很少且不是动态增减, 用 map 消耗太大.
    switch (scenes) {
      case SCENES_RBAC:
        return ENC_RBAC;

      case SCENES_JDBC:
        return ENC_JDBC;

      default:
        return null;
    }
  }


  /**
   * 数据加密策略, 密钥和加密算法由策略选择.
   */
  public interface IEncryptionStrategy {

    /**
     * 加密 key 并返回
     */
    default String encodeKey(String s) { return s; }


    /**
     * 解密 key 并返回
     */
    default String decodeKey(String s) { return s; }


    /**
     * 加密数据并返回
     */
    default String encodeData(String s) { return s; }


    /**
     * 解密数据并返回, s 不能为 null 否则抛出异常
     */
    default String decodeData(String s) { return s; }


    /**
     * 如果允许使用 key 做模糊查询返回 true, 默认返回 true
     */
    default boolean keyAmbiguous() { return true; }

  }


  private static class OnlyKeyEnc implements IEncryptionStrategy {
    private AES2 aes;


    private OnlyKeyEnc(String pass) {
      aes = new AES2("k-"+ pass +"-enc:"+ Version.PKCRC);
    }


    @Override
    public String encodeKey(String s) {
      return Hex.encode64(aes.encryptBin(s.getBytes(CHARSET)));
    }


    @Override
    public String decodeKey(String s) {
      return new String(aes.decryptBin(Hex.decode64(s)));
    }
  }


  private static class RBACKeyEnc implements IEncryptionStrategy {
    private AES2 aes;


    private RBACKeyEnc(String pass) {
      aes = new AES2("rbac-"+ pass +"-enc:"+ Version.PKCRC);
    }


    @Override
    public String encodeKey(String s) {
      int be = s.indexOf(':');
      int ed = s.lastIndexOf(':');
      if (be == ed) {
        return s;
      }
      String a = s.substring(0, ed+1);
      String b = s.substring(ed+1);
      return a + Hex.encode64(aes.encryptBin(b.getBytes(CHARSET)));
    }


    @Override
    public String decodeKey(String s) {
      int be = s.indexOf(':');
      int ed = s.lastIndexOf(':');
      if (be == ed) {
        return s;
      }
      String a = s.substring(0, ed+1);
      String b = s.substring(ed+1);
      return a + new String(aes.decryptBin(Hex.decode64(b)));
    }
  }


  private static class OnlyValueEnc implements IEncryptionStrategy {
    private AES2 aes;


    private OnlyValueEnc(String pass) {
      aes = new AES2("v-"+ pass +"-enc:"+ Version.PKCRC);
    }


    @Override
    public String encodeData(String s) {
      return Hex.encode64(aes.encryptBin(s.getBytes(CHARSET)));
    }


    @Override
    public String decodeData(String s) {
      return new String(aes.decryptBin(Hex.decode64(s)));
    }
  }
}
