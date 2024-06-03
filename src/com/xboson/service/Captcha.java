////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-12-8 上午9:19
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/service/Captcha.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.service;

import com.xboson.been.CallData;
import com.xboson.j2ee.container.XPath;
import com.xboson.j2ee.container.XService;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;


@XPath("/captcha")
public class Captcha extends XService {

  public final static String IMG_TYPE = "png";
  public final static String IMG_MIME = "image/png";

  private Font drawFont;

  public Captcha() {
    File cd = new File(SysConfig.me().readConfig().configPath);
    ImageIO.setCacheDirectory(cd);
    drawFont = new Font("Arial", Font.ITALIC, 20);
  }


  @Override
  public void service(CallData data) throws Exception {
    String code = data.sess.captchaCode;
    if (null == code) {
      code = Tool.randomString(6);
      data.sess.captchaCode = code.toLowerCase();
    }

    BufferedImage img = new BufferedImage(100, 40, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = img.createGraphics();
    g.setColor(Color.white);
    g.fillRect(0, 0, 100, 40);
    g.setColor(Color.red);
    g.setFont(drawFont);
    g.drawString(code, 10, 30);

    data.resp.setContentType(IMG_MIME);
    ImageIO.write(img, IMG_TYPE, data.resp.getOutputStream());
  }


  @Override
  public boolean needLogin() {
    return false;
  }
}
