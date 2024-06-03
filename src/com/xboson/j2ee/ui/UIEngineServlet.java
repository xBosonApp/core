////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-18 下午8:22
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/ui/UIEngineServlet.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.ui;

import com.xboson.been.Config;
import com.xboson.been.UrlSplit;
import com.xboson.been.XBosonException;
import com.xboson.fs.redis.RedisFileAttr;
import com.xboson.fs.redis.IRedisFileSystemProvider;
import com.xboson.fs.ui.UIFileFactory;
import com.xboson.j2ee.container.IHttpHeader;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.script.lib.Path;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;

import javax.activation.FileTypeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.Enumeration;
import java.util.Set;


/**
 * 当操作成功返回 http 状态 200, 文件找不到返回 404,
 * 操作失败返回 500 并且设置 http 头域 Error-Message 包含错误消息字符串.
 *
 * 应用路径: /[xboson]/face/*[文件目录]/**
 *
 * @see com.xboson.init.Startup 配置到容器
 */
public class UIEngineServlet extends HttpServlet implements IHttpHeader {

  public static final String MY_URL = "/face";
  public static final String HTML_TYPE = CONTENT_TYPE_HTML;

  private IRedisFileSystemProvider file_provider;
  private TemplateEngine template;
  private Log log;
  private FileTypeMap mime;
  private String baseurl;

  /** 当该配置为 true, 用户打开的路径是目录则返回目录内文件列表 */
  private boolean list_dir;


  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.log = LogFactory.create("ui-engine");
    this.mime = MimeTypeFactory.getFileTypeMap();
    this.baseurl = config.getServletContext().getContextPath() + MY_URL;

    Config cf = SysConfig.me().readConfig();
    this.list_dir = cf.uiListDir;
    this.file_provider = UIFileFactory.open();
    this.template = new TemplateEngine(file_provider);
  }


  /**
   * 返回文件路径
   * @param req
   * @return 请求文件的路径, 已经规范化
   * @throws IOException
   */
  private String getReqFile(HttpServletRequest req)
          throws IOException {

    UrlSplit url = new UrlSplit(req);
    String last = url.getLast();
    if (last == null) {
      return null;
    }

    String path = Tool.normalize(last);
    if (path.equals("/")) return null;
    return path;
  }


  /**
   * 读取文件
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {

    String path = getReqFile(req);
    if (path == null) {
      if (!list_dir) {
        resp.sendError(400,
                "No file request (e.g: /face/ui/paas/login.html)");
        return;
      }
      path = "/";
    }

    try {
      RedisFileAttr fs = file_provider.readAttribute(path);

      if (fs == null) {
        resp.sendError(400, path);
        return;
      }

      if (fs.isFile()) {
        if (TemplateEngine.EXT.equalsIgnoreCase(Path.me.extname(path))) {
          resp.setContentType(HTML_TYPE);
          template.service(req, resp);
          return;
        }

        file_provider.readFileContent(fs);

        String file_type = mime.getContentType(path);
        resp.setContentType(file_type);

        OutputStream out = resp.getOutputStream();
        out.write(fs.getFileContent());
        out.flush();

        log.debug("Get File:", file_type, path);
      }
      else if (fs.isDir() && list_dir) {
        resp.setContentType(HTML_TYPE);
        resp.setHeader(HEAD_CACHE, VAL_CACHE_NO);
        Set<RedisFileAttr> files = file_provider.readDir(fs.path);
        HtmlDirList.toHtml(resp.getWriter(), files, baseurl + path);
      }

    } catch(NoSuchFileException
            | FileNotFoundException
            | XBosonException.NotFound e) {
      log.debug(e);
      resp.sendError(404, path);

    } catch(AccessDeniedException access) {
      log.debug(access);
      resp.sendError(406, path);
    }
  }


  /**
   * 写入文件
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {
    throw new UnsupportedOperationException("POST");
  }


  /**
   * 删除文件
   */
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {
    UrlSplit url = new UrlSplit(req);
    log.warn("DELETE file ", url.getLast());
    throw new UnsupportedOperationException();
  }


  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {
    String path = getReqFile(req);
    String file_type = mime.getContentType(path);
    resp.setContentType(file_type);
    log.debug("Head", file_type, path);
  }


  @Override
  protected long getLastModified(HttpServletRequest req) {
    try {
      String path = getReqFile(req);
      if (path == null)
        return -1;
      if (path.endsWith(TemplateEngine.EXT))
        return -1;

      log.debug("Last Modified", path);
      return file_provider.modifyTime(path);

    } catch (Exception e) {
      log.error(e);
      return -1;
    }
  }
}
