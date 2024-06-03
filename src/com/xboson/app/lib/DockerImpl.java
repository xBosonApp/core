////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-12-16 下午12:16
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/DockerImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.been.XBosonException;
import com.xboson.util.CreatorFromUrl;
import com.xboson.util.SSL;
import com.xboson.util.Tool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.security.cert.Certificate;
import java.util.Map;


public class DockerImpl extends RuntimeUnitImpl {

  public final String DEFAULT_VERSION = "1.18";

  private CreatorFromUrl<IDockerClient> clientCreator;


  public DockerImpl() {
    super(null);
    clientCreator = new CreatorFromUrl<>();

    clientCreator.reg("http", (v, p, uris, data)->
            new Http(uris, null));

    clientCreator.reg("https", (v, p, uris, data)->
            new Http(uris, data));

    clientCreator.reg("unix", (v, p, uris, data)-> {
      throw new UnsupportedOperationException();
    });
  }


  public IDockerClient open(String uri) {
    return open(uri, null);
  }


  /**
   * 打开到服务器链接, tslConfig 保存 TSL 配置参数
   * tslConfig.cert 客户端证书, X509-PEM 格式
   * tslConfig.key  证书私钥, pkcs8 格式
   * tslConfig.ca   CA 证书, X509-PEM 格式
   * tslConfig.pass 加密证书和私钥的密钥
   */
  public IDockerClient open(String uri, Object tslConfig) {
    return clientCreator.create(uri, tslConfig);
  }


  public interface IDockerClient {

    /**
     * 设置 docker api 版本
     */
    void setVersion(String v);


    /**
     * 调用接口返回数据
     */
    Object call(String api, Map<String, Object> data) throws Exception;


    default Object call(String api) throws Exception {
      return call(api, null);
    }
  }


  /**
   * TODO: 该对象必须缓存
   */
  private class Http implements IDockerClient {

    private String ver = DEFAULT_VERSION;
    private String host;
    private OkHttpClient hc;


    private Http(String url, Object data) throws Exception {
      this.host = Tool.urlNoSuffix(url);
      OkHttpClient.Builder cb = new OkHttpClient.Builder();

      if (url.startsWith("https://")) {
        if (data == null) {
          throw new XBosonException("SSL Certificate needed");
        }
        bindSslParamter(cb, data);
      }
      this.hc = cb.build();
    }


    private void bindSslParamter(OkHttpClient.Builder cb, Object data)
            throws Exception {
      Map<String, Object> config = (Map<String, Object>) data;
      String cert = (String) config.get("cert");
      String key  = (String) config.get("key");
      String ca   = (String) config.get("ca");
      String pass  = (String) config.get("pass");

      SSL ssl = new SSL(pass);
      ssl.addCertificate(ssl.toStream(ca));
      Certificate c = ssl.addCertificate(ssl.toStream(cert));
      ssl.addPrivateKey(c, key);

      cb.sslSocketFactory(ssl.getSocketFactory(), ssl.getX509TrustManager());
      cb.hostnameVerifier(SSL.APASS);
    }


    @Override
    public void setVersion(String v) {
      this.ver = v;
    }


    @Override
    public Object call(String api, Map<String, Object> data) throws Exception {
      String url = host +"/v"+ ver +"/"+ api;
      HttpUrl.Builder url_build = HttpUrl.parse(url).newBuilder();

      if (data != null) {
        for (Map.Entry<String, Object> en : data.entrySet()) {
          Object o = en.getValue();
          url_build.addQueryParameter(en.getKey(), String.valueOf(o));
        }
      }

      Request.Builder build = new Request.Builder();
      build.url(url_build.build());

      try (Response resp = hc.newCall(build.build()).execute()) {
        return jsonParse(resp.body().string());
      }
    }
  }

}
