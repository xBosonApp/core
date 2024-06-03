////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-5-21 下午4:01
// 原始文件路径: E:/xboson/xBoson/src/com/xboson/app/lib/ImageImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.script.lib.Buffer;
import com.xboson.script.lib.Bytes;
import com.xboson.util.StringBufferOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageImpl {

  public Picture read(Buffer.JsBuffer buf) throws IOException {
    return new Picture(new ByteArrayInputStream(buf._buffer().array()));
  }


  public Picture read(Bytes bytes) throws IOException {
    return new Picture(new ByteArrayInputStream(bytes.bin()));
  }


  public Picture read(byte[] b) throws IOException {
    return new Picture(new ByteArrayInputStream(b));
  }


  public class Picture {
    private BufferedImage bi;

    private Picture(InputStream i) throws IOException {
      bi = ImageIO.read(i);
    }

    public int height() {
      return bi.getHeight();
    }

    public int width() {
      return bi.getWidth();
    }

    public Buffer.JsBuffer toBuffer(String format) throws IOException {
      StringBufferOutputStream out = new StringBufferOutputStream();
      if (ImageIO.write(bi, format, out)) {
        return new Buffer().from(out.toBytes());
      }
      return null;
    }

    public void resize(int x, int y, int width, int height) {
      bi = bi.getSubimage(x, y, width, height);
    }
  }
}
