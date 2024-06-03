////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-2-5 下午6:15
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/been/Certificate.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.been;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.xboson.crypto.AbsLicense;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import com.xboson.util.config.YamlConfigImpl;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * 软件使用证书
 */
public class License extends AbsLicense {

  public static final String PUB_FILE = "/public_key.pem";
  public static final String LIC_FILE = "/license.txt";
  public static final String REQ_FILE = "/license.req";
  public static final String PREFIX   = "file:";

  public transient String publicKeyFile;
  public transient String basePath;
  public String signature;


  public License() {
    basePath = SysConfig.me().readConfig().configPath;
    publicKeyFile = "./WebRoot/WEB-INF" + PUB_FILE;
  }


  public void setPublicKeyFile(URL url) throws MalformedURLException {
    publicKeyFile = url.toString();
    if (publicKeyFile.startsWith(PREFIX)) {
      publicKeyFile = publicKeyFile.substring(PREFIX.length());
    }
  }


  public void setPublicKeyFile(ServletContext sc) throws MalformedURLException {
    URL url = sc.getResource("/WEB-INF" + PUB_FILE);
    setPublicKeyFile(url);
  }


  public String zz() {
    if (z == null) {
      super.z();
      return singleline(z);
    } else {
      String oz = z;
      super.z();
      return singleline(oz);
    }
  }


  @Override
  public String getPublicKeyFile() {
    return publicKeyFile;
  }


  public File writeRequest() throws IOException {
    return writeFile(REQ_FILE);
  }


  public File writeLicense() throws IOException {
    return writeFile(LIC_FILE);
  }


  private File writeFile(String file) throws IOException {
    File outFile = new File(basePath + file);
    FileWriter fileOut = new FileWriter(outFile);
    writeTo(fileOut);
    return outFile;
  }


  public void writeTo(Writer out) throws YamlException {
    YamlConfig config = YamlConfigImpl.basicConfig();
    config.writeConfig.setWriteClassname(YamlConfig.WriteClassName.NEVER);
    YamlWriter yaml = new YamlWriter(out, config);
    yaml.write(this);
    yaml.close();
  }


  public static License readLicense() throws IOException {
    String basePath = SysConfig.me().readConfig().configPath;
    StringBuilder buf = Tool.readFromFile(basePath + LIC_FILE);
    YamlConfig yc = new YamlConfig();
    yc.setClassTag("com.xboson.been.License", License.class);
    YamlReader r = new YamlReader(buf.toString(), yc);
    return r.read(License.class);
  }


  @Override
  protected String signatureString() {
    return singleline(signature);
  }
}
