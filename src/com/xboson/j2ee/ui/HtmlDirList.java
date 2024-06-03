////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-20 下午1:32
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/j2ee/html/HtmlDirList.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.ui;

import com.xboson.fs.redis.RedisFileAttr;
import com.xboson.util.Tool;

import javax.activation.FileTypeMap;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Set;


public class HtmlDirList {

  static com.xboson.script.lib.Path tool = new com.xboson.script.lib.Path();


  /**
   * 生成目录列表
   *
   * @param html 输出
   * @param files
   * @param baseurl 网络路径
   * @throws IOException
   */
  public static void toHtml(Writer html, Set<RedisFileAttr> files, String baseurl)
          throws IOException {

    html.write("<html><head>");
    style(html);
    html.write("</head><body><h1>Directory ");
    html.write(baseurl);
    html.write("</h1><table>");

    html.write("<tr><td><a href='");
    html.write(tool.normalize(baseurl + "/../"));
    html.write("'>[..]</a></td><tr>");

    FileTypeMap types = MimeTypeFactory.getFileTypeMap();

    for (RedisFileAttr p : files) {
      html.write("<tr>");

      String name = p.path;
      html.write("<td><a href='");
      html.write(tool.normalize(baseurl + "/" + name));
      html.write("'>");
      html.write(name);
      html.write("</a></td>");

      html.write("<td>");
      html.write(p.isDir() ? "[DIR]" : types.getContentType(name));
      html.write("</td>");

      html.write("<td>");
      html.write(p.lastModify > 0 ? Tool.formatDate(new Date(p.lastModify)) : "");
      html.write("</td>");

      html.write("</tr>");
    }

    html.write("</table><section class='right'>");
    html.write(new Date().toString());
    html.write("<hr/> J.yanming");
    html.write("</section></html>");
    html.flush();
  }


  public static void style(Writer html) throws IOException {
    html.write("<style>");
    html.write("body { padding: 50px } ");
    html.write("table {} ");
    html.write("td { border-bottom:1px solid #c3c3c3; padding: 3px 30px; } ");
    html.write("section { margin: 50px 0 } ");
    html.write(".right { text-align: right; color: #1e1140 } ");
    html.write("hr { border: 1px dashed  #9e971d; border-top:0; margin: 1px;} ");
    html.write("</style>");
  }

}
