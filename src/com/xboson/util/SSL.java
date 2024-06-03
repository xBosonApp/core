////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-12-16 下午2:47
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/SSL.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.util.c0nst.IConstant;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * 不要碰 SSL, 你永远都不知道都干了什么.
 */
public class SSL {

  /** 证书类型 */
  public static final String X509 = "X.509";
  /** 证书格式 */
  public static final String PKCS12 = "PKCS12";
  /** 私钥加密算法 */
  public static final String RSA = "RSA";
  /** 对任何主机地址总是通过 */
  public static final HostnameVerifier APASS = new AlwaysPass();

  private SSLContext sslContext;
  private CertificateFactory factory;
  private KeyStore keyStore;
  private TrustManagerFactory trustManagerFactory;
  private KeyManagerFactory keyManagerFactory;
  private X509TrustManagerImpl x509trust;
  private final char[] password;


  /**
   * 使用 TLS 协议和默认密码创建 ssl 配置
   * @see #SSL(String, String)
   */
  public SSL(String password) throws Exception {
    this("TLS", password);
  }


  /**
   * 使用指定协议和密码创建 ssl 配置,
   * @param protocol - TLS/SSL/...
   * @param password 证书密钥
   * @see SSLContext 可用的协议
   * @throws Exception 任何密码学相关异常都可能被抛出
   */
  public SSL(String protocol, String password) throws Exception {
    sslContext    = SSLContext.getInstance(protocol);
    factory       = CertificateFactory.getInstance(X509);
    x509trust     = new SSL.X509TrustManagerImpl();
    keyStore      = KeyStore.getInstance(PKCS12);
    keyStore.load(null);
    this.password = password.toCharArray();
  }


  /**
   * 添加证书并返回证书
   * @param pemCertificate PEM 格式证书
   * @throws Exception 任何密码学相关异常都可能被抛出
   */
  public Certificate addCertificate(InputStream pemCertificate) throws Exception {
    X509Certificate cert = (X509Certificate) factory.generateCertificate(
            new Pem2Der(pemCertificate).openDerInputStream());
    // 第一个参数必须是小写的, 否则报错
    keyStore.setCertificateEntry("cert:"+ x509trust.size(), cert);
    x509trust.add(cert);
    return cert;
  }


  /**
   * 使用默认密码创建密钥
   * @see #addPrivateKey(Certificate, String, String)
   */
  public void addPrivateKey(Certificate c, String pemKey) throws Exception {
    addPrivateKey(c, pemKey, null);
  }


  /**
   * 设置证书的私钥, 私钥用密码加密.
   *
   * openssl 默认生成的 rsa 密钥格式不匹配需要执行命令转换:
   * <code>
   *   openssl pkcs8 -topk8 -inform PEM -in [infile] -outform PEM -nocrypt
   * </code>
   *
   * @param cert 证书
   * @param pemKey 必须是 pkcs8 格式编码
   * @param keyPass 加密私钥钥的密码
   * @throws Exception
   */
  public void addPrivateKey(Certificate cert, String pemKey, String keyPass)
          throws Exception {
    String key = ECDSA.me().formatPrivateKey(pemKey);

    byte[] encoded = Hex.decode(Hex.Names.BASE64, key);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    KeyFactory keyFactory = KeyFactory.getInstance(RSA);
    PrivateKey pk = keyFactory.generatePrivate(keySpec);

    char[] ps = keyPass == null ? password : keyPass.toCharArray();
    Certificate[] bind = { cert };
    keyStore.setKeyEntry("pkey:"+ x509trust.size(), pk, ps, bind);
  }


  /**
   * 初始化并返回 SSL 工厂.
   */
  public SSLSocketFactory getSocketFactory() throws Exception {
    trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(keyStore);
    keyManagerFactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, password);

    sslContext.init(
            keyManagerFactory.getKeyManagers(),
            trustManagerFactory.getTrustManagers(),
            new SecureRandom());

    return sslContext.getSocketFactory();
  }


  public X509TrustManager getX509TrustManager() {
    return x509trust;
  }


  /**
   * 将字符串包装成字节流
   */
  public static InputStream toStream(String s) {
    byte[] b = s.getBytes(IConstant.CHARSET);
    return new ByteArrayInputStream(b);
  }


  /**
   * 证书信任管理器
   */
  public static class X509TrustManagerImpl implements X509TrustManager {
    private List<X509Certificate> list = new ArrayList<>();


    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
    }


    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
    }


    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return list.toArray(new X509Certificate[list.size()]);
    }


    private void add(X509Certificate c) {
      list.add(c);
    }


    public int size() {
      return list.size();
    }
  }


  /**
   * 读取以 '-----BEGIN' 开头, '-----END' 结尾的 base64 格式证书输入流,
   * 并在转换为 DER 格式的原始字节.
   */
  public static class Pem2Der {
    private static final String BEGIN = "-----BEGIN";
    private static final String END   = "-----END";

    private BufferedReader rl;
    private String line;
    private byte[] bytes;
    private int pos;


    public Pem2Der(InputStream pemInput) throws IOException {
      rl = new BufferedReader(new InputStreamReader(pemInput));
      init();
    }


    public Pem2Der(String pem) throws IOException {
      rl = new BufferedReader(new StringReader(pem));
    }


    private void init() throws IOException {
      line = rl.readLine();
      if (! line.startsWith(BEGIN)) {
        throw new IOException("Bad PEM format");
      }
      pos   = 1;
      bytes = new byte[0];
    }


    /**
     * 打开原始 der 格式输入流
     */
    public InputStream openDerInputStream() {
      InputStream txt = new InputStream() {
        public int read() throws IOException {
          if (pos >= bytes.length) {
            line  = rl.readLine();
            if (line == null || line.startsWith(END)) {
              return -1;
            }
            pos   = 0;
            bytes = line.getBytes(IConstant.CHARSET);
          }
          return bytes[pos++];
        }

        public void close() throws IOException {
          rl.close();
        }
      };
      return Base64.getDecoder().wrap(txt);
    }
  }


  /**
   * 总是通过对服务端的请求
   */
  public static class AlwaysPass implements HostnameVerifier {
    @Override
    public boolean verify(String s, SSLSession sslSession) {
      return true;
    }
  }
}
