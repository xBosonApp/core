////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-17 上午9:56
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/CryptoImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.been.XBosonException;
import com.xboson.script.lib.Buffer;
import com.xboson.script.lib.Bytes;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.SecureRandom;


public class CryptoImpl extends RuntimeUnitImpl {

  public CryptoImpl() {
    super(null);
  }


  public CipherJs createCipher(String algorithm, String pass) throws Exception {
    return _cipher(algorithm, pass, Cipher.ENCRYPT_MODE);
  }


  public CipherJs createDecipher(String algorithm, String pass) throws Exception {
    return _cipher(algorithm, pass, Cipher.DECRYPT_MODE);
  }


  /**
   * @see Digest
   */
  public Digest.HashWarp createHash(String algorithm) throws Exception {
    return new Digest.HashWarp(algorithm);
  }


  private CipherJs _cipher(String alg, String pass, int mode) throws Exception {
    alg = alg.toLowerCase();
    Cipher cipher;

    switch (alg) {
      case "aes":
        cipher = Cipher.getInstance("AES");
        cipher.init(mode, getKey(alg, pass));
        break;

      case "des":
        SecureRandom random = new SecureRandom();
        cipher = Cipher.getInstance("DES");
        cipher.init(mode, getKey(alg, pass), random);
        break;

      case "pbe":
        byte[] salt = Tool.randomBytes(8);
        PBEParameterSpec spec = new PBEParameterSpec(salt, 100);
        cipher = Cipher.getInstance("PBEWITHMD5andDES");
        cipher.init(mode, getKey(alg, pass), spec);
        break;

      case "idea":
        cipher = Cipher.getInstance("IDEA/ECB/ISO10126Padding");
        cipher.init(mode, getKey(alg, pass));
        break;

      default:
        throw new XBosonException("Unknow algorithm "+ alg);
    }
    return new CipherJs(cipher);
  }


  private SecretKey getKey(String algorithm, String pass) throws Exception {
    switch (algorithm) {
      case "aes":
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(pass.getBytes(IConstant.CHARSET));
        kgen.init(128, secureRandom);
        return kgen.generateKey();

      case "des":
        DESKeySpec desKey = new DESKeySpec(pass.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        return keyFactory.generateSecret(desKey);

      case "pbe":
        PBEKeySpec pbeKeySpec = new PBEKeySpec(pass.toCharArray());
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWITHMD5andDES");
        return factory.generateSecret(pbeKeySpec);

      case "idea":
        KeyGenerator keyGenerator = KeyGenerator.getInstance("IDEA");
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey;

      default:
        throw new XBosonException("Cannot gen key "+ algorithm);
    }
  }


  /**
   * 加密/解密导出类
   */
  public class CipherJs {
    private Cipher c;

    private CipherJs(Cipher c) {
      this.c = c;
    }

    public Bytes update(String str) {
      byte[] b = c.update(str.getBytes(IConstant.CHARSET));
      return b == null ? new Bytes() : new Bytes(b);
    }

    public Bytes update(Bytes bin) {
      byte[] b = c.update(bin.bin());
      return b == null ? new Bytes() : new Bytes(b);
    }

    public Bytes update(Buffer.JsBuffer buf) {
      byte[] b = c.update(buf._buffer().array());
      return b == null ? new Bytes() : new Bytes(b);
    }

    public Bytes end() throws BadPaddingException, IllegalBlockSizeException {
      byte[] b = c.doFinal();
      return b == null ? new Bytes() : new Bytes(b);
    }
  }
}
