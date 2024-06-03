////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-2-7 下午12:21
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/j2ee/container/Processes.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.container;

import com.xboson.been.License;
import com.xboson.been.XBosonException;
import com.xboson.crypto.Crypto;
import com.xboson.event.timer.TimeFactory;
import com.xboson.fs.watcher.INotify;
import com.xboson.fs.watcher.SingleFile;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.Version;
import com.xboson.util.c0nst.IConstant;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.WatchEvent;
import java.util.Base64;
import java.util.HashSet;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.CRC32;


public final class Processes {

  private static Processes instance;
  private static long DAY = 24 * 60 * 60 * 1000;

  /**
   * 在 TestSign 中生成
   * @see com.xboson.test.TestSign
   */
  public static final String s[] = new String[] {
      /* 0 > Bad License, Copy protection */
          "QmFkIExpY2Vuc2UsIENvcHkgcHJvdGVjdGlvbg==",
      /* 1 > Bad License, Wrong application */
          "QmFkIExpY2Vuc2UsIFdyb25nIGFwcGxpY2F0aW9u",
      /* 2 > Bad License, Signature fail */
          "QmFkIExpY2Vuc2UsIFNpZ25hdHVyZSBmYWls",
      /* 3 > Bad License, Has not yet started */
          "QmFkIExpY2Vuc2UsIEhhcyBub3QgeWV0IHN0YXJ0ZWQ=",
      /* 4 > Bad License, Over the use of time */
          "QmFkIExpY2Vuc2UsIE92ZXIgdGhlIHVzZSBvZiB0aW1l",
      /* 5 > License */
          "TGljZW5zZQ==",
      /* 6 > Update */
          "VXBkYXRl",
      /* 7 > bad public key */
          "YmFkIHB1YmxpYyBrZXk=",
      /* 8 > No license to run the function,  */
          "Tm8gbGljZW5zZSB0byBydW4gdGhlIGZ1bmN0aW9uLCA=",
      /* 9 > 未授权的产品, 由[上海竹呗信息技术有限公司]提供技术支持 */
          "5pyq5o6I5p2D55qE5Lqn5ZOBLCDnlLFb5LiK5rW356u55ZGX5L+" +
                  "h5oGv5oqA5pyv5pyJ6ZmQ5YWs5Y+4XeaPkOS+m+aKgOacr+aUr+aMgQ==",
  };

  static {
    Base64.Decoder d = Base64.getDecoder();
    for (int i=0; i<s.length; ++i) {
      s[i] = new String(d.decode(s[i]));
    }
  }


  private final ReentrantLock threadLock;
  private final Log log;
  private String msg;
  private License license;


  public static Processes me() {
    if (instance == null) {
      synchronized (Processes.class) {
        if (instance == null) {
          instance = new Processes();
        }
      }
    }
    return instance;
  }


  private Processes() {
    log = LogFactory.create(s[5]);
    threadLock = new ReentrantLock();
  }


  public void init(ServletContext sc) {
    try {
      if (sc == null || license != null)
        return;

      final long PERIOD = DAY / 2;
      license = License.readLicense();
      license.setPublicKeyFile(sc);

      Point point = new Point();
      point.run();
      TimeFactory.me().schedule(point, PERIOD, PERIOD);

    } catch (FileNotFoundException nf) {
      writeNulLicense();
      throw new XBosonException(
              "Cannot found license file, make it first.", nf);

    } catch (IOException e) {
      throw new XBosonException(e);
    }
  }


  private void writeNulLicense() {
    License lic   = new License();
    lic.appName   = Version.Name;
    lic.company   = "未知";
    lic.dns       = "unknow.com";
    lic.email     = "unknow@unknow.com";
    lic.beginTime = System.currentTimeMillis();
    lic.endTime   = System.currentTimeMillis() + DAY;
    lic.api       = new HashSet<>();
    lic.zz();

    try {
      lic.writeLicense();
    } catch (IOException e) {
      throw new XBosonException("Cannot make license file", e);
    }
  }


  private class Point extends TimerTask implements INotify {
    private SingleFile sf;


    private Point() throws IOException {
      File f = new File(license.LIC_FILE);
      sf = new SingleFile(license.basePath, f.getName(), this);
    }


    @Override
    public void run() {
      try {
        msg = execution(license);
      } catch (Exception e) {
        msg = e.getMessage();
      }
      XResponse.licenseState = msg;
      if (msg != null) log.warn(msg);
    }


    @Override
    public void notify(String basename,
                       String filename,
                       WatchEvent event,
                       WatchEvent.Kind kind) throws IOException
    {
      log.info(s[6], basename, filename);
      License new_license = License.readLicense();
      new_license.publicKeyFile = license.publicKeyFile;
      license = new_license;
      run();
    }


    @Override
    public void remove(String basename) {
    }
  }


  private String execution(License license) throws Exception {
    //
    // crc 算法放在这里方便混淆.
    // 之所以使用 readLine 是因为文本换行在不同系统上的差异导致 crc 不一致.
    //
    CRC32 crc = new CRC32();
    BufferedReader read = new BufferedReader(
            new FileReader(license.getPublicKeyFile()));
    String line;
    while ((line = read.readLine()) != null) {
      crc.update(line.getBytes(IConstant.CHARSET));
    }

    if (crc.getValue() != Version.PKCRC) {
      return s[7] +' '+ Long.toString(Long.MAX_VALUE-crc.getValue(), 16);
    }

    if (! license.zz().equals(License.singleline(license.z))) {
      license.writeRequest();
      return s[0];
    }

    if (! Version.Name.equals(license.appName)) {
      return s[1];
    }

    if (! Crypto.me().verification(license)) {
      return s[2];
    }

    if (license.beginTime > System.currentTimeMillis()) {
      return s[3];
    }

    if (license.endTime < System.currentTimeMillis()) {
      return s[4];
    }

    return null;
  }


  /**
   * 给运行时加授权限制会用到
   */
  public class Filter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain)
            throws IOException, ServletException {

      if (msg != null) {
        response.getWriter().write(msg);
        return;
      }
      chain.doFilter(request, response);
    }
  }


  /**
   * 线程锁, 在未授权时限制为单线程
   */
  public class Happy {

    public void lock() {
      if (msg != null)
        threadLock.lock();
    }

    public void unlock() {
      if (threadLock.getHoldCount() > 0)
        threadLock.unlock();
    }
  }


  /**
   * 返回错误消息, 当返回 null 说明授权基础验证通过.
   */
  public String message() {
    return msg;
  }


  /**
   * 返回初始化完成的证书对象
   */
  public License message2() {
    return license;
  }

}
