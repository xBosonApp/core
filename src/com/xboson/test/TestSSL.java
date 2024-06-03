////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-12-17 上午9:34
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestSSL.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.util.SSL;
import com.xboson.util.Tool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.security.cert.Certificate;


public class TestSSL {

  final static String cert = "-----BEGIN CERTIFICATE-----\n" +
          "MIIFAzCCAuugAwIBAgIUAIxqv3Iwft0I1Kw/m6U4e20iJhwwDQYJKoZIhvcNAQEL\n" +
          "-----END CERTIFICATE-----";

  final static String ca = "-----BEGIN CERTIFICATE-----\n" +
          "MIIFezCCA2OgAwIBAgIUOeYE2nsgdieER+9oA3eWDtnngdkwDQYJKoZIhvcNAQEL\n" +
          "-----END CERTIFICATE-----";

  final static String key = "-----BEGIN PRIVATE KEY-----\n" +
          "MIIJQgIBADANBgkqhkiG9w0BAQEFAASCCSwwggkoAgEAAoICAQDENVoy/OF593iL\n" +
          "-----END PRIVATE KEY-----";

  final static String pass = "0000";
  final static String url  = "https://10.0.0.3:2375/v1.18/info";

  /**
   * 该测试不在应用上下文, 也不在标准测试用例中.
   * 测试前按照 https://docs.docker.com/engine/security/https/ 的说明配置服务器
   * 将生成的证书和使用的密钥配置到全局变量中.
   */
  public static void main(String []av) throws Exception {
    SSL ssl = new SSL("TLS", pass);
    Certificate c;
    // THROW: Software caused connection abort: recv failed
    c = ssl.addCertificate(ssl.toStream(cert));
    // THROW: unable to find valid certification path to requested target
    ssl.addCertificate(ssl.toStream(ca));
    ssl.addPrivateKey(c, key);

    OkHttpClient.Builder cb = new OkHttpClient.Builder();
    cb.sslSocketFactory(ssl.getSocketFactory(), ssl.getX509TrustManager());
    cb.hostnameVerifier(SSL.APASS);
    OkHttpClient hc = cb.build();

    HttpUrl.Builder url_build = HttpUrl.parse(url).newBuilder();
    Request.Builder build = new Request.Builder();
    build.url(url_build.build());
    build.get();

    Response resp = hc.newCall(build.build()).execute();
    Tool.pl("Docker 回应:", resp.body().string());
  }
}
