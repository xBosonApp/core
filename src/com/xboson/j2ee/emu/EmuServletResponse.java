////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-23 上午11:40
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/impl/EmuServletResponse.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.j2ee.emu;

import com.xboson.log.Log;
import com.xboson.log.LogFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;


public class EmuServletResponse implements HttpServletResponse {

  private Log log = LogFactory.create("InnerServletResponse");
  public final ServletOutputStream out;


  public EmuServletResponse() {
    out = new LogServletOutputStream(log);
  }


  public EmuServletResponse(ServletOutputStream out) {
    this.out = out;
  }


  @Override
  public void addCookie(Cookie cookie) {
    log.info("addCookie", cookie);
  }


  @Override
  public boolean containsHeader(String s) {
    return false;
  }


  @Override
  public String encodeURL(String s) {
    return null;
  }


  @Override
  public String encodeRedirectURL(String s) {
    return null;
  }


  @Override
  public String encodeUrl(String s) {
    return null;
  }


  @Override
  public String encodeRedirectUrl(String s) {
    return null;
  }


  @Override
  public void sendError(int i, String s) throws IOException {
    log.error("sendError", i, s);
  }


  @Override
  public void sendError(int i) throws IOException {
    log.error("sendError", i);
  }


  @Override
  public void sendRedirect(String s) throws IOException {
    log.info("sendRedirect", s);
  }


  @Override
  public void setDateHeader(String s, long l) {
    log.info("setDateHeader", s, l);
  }


  @Override
  public void addDateHeader(String s, long l) {
    log.info("addDateHeader", s, l);
  }


  @Override
  public void setHeader(String s, String s1) {
    log.info("setHeader", s, s1);
  }


  @Override
  public void addHeader(String s, String s1) {
    log.info("addHeader", s, s1);
  }


  @Override
  public void setIntHeader(String s, int i) {
    log.info("setIntHeader", s, i);
  }


  @Override
  public void addIntHeader(String s, int i) {
    log.info("addIntHeader", s, i);
  }


  @Override
  public void setStatus(int i) {
    log.info("setStatus", i);
  }


  @Override
  public void setStatus(int i, String s) {
    log.info("setStatus", i, s);
  }


  @Override
  public int getStatus() {
    return 0;
  }


  @Override
  public String getHeader(String s) {
    return null;
  }


  @Override
  public Collection<String> getHeaders(String s) {
    return null;
  }


  @Override
  public Collection<String> getHeaderNames() {
    return null;
  }


  @Override
  public String getCharacterEncoding() {
    return null;
  }


  @Override
  public String getContentType() {
    return null;
  }


  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return out;
  }


  @Override
  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(System.out);
  }


  @Override
  public void setCharacterEncoding(String s) {
    log.info("setCharacterEncoding", s);
  }


  @Override
  public void setContentLength(int i) {
    log.info("setContentLength", i);
  }


  @Override
  public void setContentLengthLong(long l) {
    log.info("setContentLengthLong", l);
  }


  @Override
  public void setContentType(String s) {
    log.info("setContentType", s);
  }


  @Override
  public void setBufferSize(int i) {
    log.info("setBufferSize", i);
  }


  @Override
  public int getBufferSize() {
    return 0;
  }


  @Override
  public void flushBuffer() throws IOException {
    out.flush();
  }


  @Override
  public void resetBuffer() {
  }


  @Override
  public boolean isCommitted() {
    return false;
  }


  @Override
  public void reset() {
  }


  @Override
  public void setLocale(Locale locale) {

  }


  @Override
  public Locale getLocale() {
    return null;
  }
}
