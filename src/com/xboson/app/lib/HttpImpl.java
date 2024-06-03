////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-23 上午11:23
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/HttpImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.app.AppContext;
import com.xboson.app.InnerXResponse;
import com.xboson.been.ApiCall;
import com.xboson.been.CallData;
import com.xboson.been.ScriptEvent;
import com.xboson.j2ee.container.XResponse;
import com.xboson.script.EventFlag;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import okhttp3.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;


/**
 * 每个请求一个实例
 */
public class HttpImpl extends RuntimeUnitImpl {

  public static final MediaType JSON
          = MediaType.parse("application/json; charset=utf-8");

  private Map<String, javax.servlet.http.Cookie> cookies;
  private OkHttpClient hc;


  public HttpImpl(CallData cd) {
    super(cd);
  }


  public void setStatusCode(int code) {
    cd.resp.setStatus(code);
  }


  public String schema() {
    return cd.req.getScheme();
  }


  public String domain() {
    String domain = cd.req.getHeader("Host");
    if (domain == null) {
      domain = cd.req.getServerName();
    }
    return domain;
  }


  public int port() {
    return cd.req.getServerPort();
  }


  public String uri() {
    return cd.req.getRequestURI();
  }


  public String remoteIp() {
    return cd.getRemoteAddr();
  }


  public String method() {
    return cd.req.getMethod();
  }


  public Object headers() {
    ScriptObjectMirror js = createJSObject();
    Enumeration<String> it = cd.req.getHeaderNames();
    while (it.hasMoreElements()) {
      String name = it.nextElement();
      String val = cd.req.getHeader(name);
      js.setMember(name, val);
    }
    return js;
  }


  public Object getHeader(String name) {
    return cd.req.getHeader(name);
  }


  private void parseCookie() {
    cookies = new HashMap<>();
    javax.servlet.http.Cookie[] arr = cd.req.getCookies();

    for (int i=0; i<arr.length; ++i) {
      javax.servlet.http.Cookie ck = arr[i];
      cookies.put(ck.getName(), ck);
    }
  }


  public String getCookie(String name) {
    if (cookies == null) {
      parseCookie();
    }
    return cookies.get(name).getValue();
  }


  public void setCookie(String name, String value) {
    setCookie(name, value, 900, "/");
  }


  public void setCookie(String name, String value, int maxAgeSecond) {
    setCookie(name, value, maxAgeSecond, "/");
  }


  public void setCookie(String name, String value, int maxAgeSecond, String path) {
    javax.servlet.http.Cookie ck = new javax.servlet.http.Cookie(name, value);
    ck.setMaxAge(maxAgeSecond);
    ck.setPath(path);
    cd.resp.addCookie(ck);
  }


  public String encode(String val) throws UnsupportedEncodingException {
    return encode(val, IConstant.CHARSET_NAME);
  }


  public String encode(String val, String charset)
          throws UnsupportedEncodingException {
    return URLEncoder.encode(val, charset);
  }


  public String decode(String val) throws UnsupportedEncodingException {
    return decode(val, IConstant.CHARSET_NAME);
  }


  public String decode(String val, String charset)
          throws UnsupportedEncodingException {
    return URLDecoder.decode(val, charset);
  }


  public Object platformGet(Object api, Object param, Object header)
          throws ServletException {
    return platformPost(api, null, param, header);
  }


  public Object platformGet(Object api, Object param) throws ServletException {
    return platformPost(api, null, param, null);
  }


  public Object platformGet(Object api) throws ServletException {
    return platformPost(api, null, null, null);
  }


  public Object platformPost(Object api) throws ServletException {
    return platformPost(api, null, null, null);
  }


  public Object platformPost(Object api, Object body) throws ServletException {
    return platformPost(api, body, null, null);
  }


  public Object platformPost(Object api, Object body, Object param)
          throws ServletException {
    return platformPost(api, body, param, null);
  }


  /**
   * 该方法通过内部直接调用 api, 可以跨 org 调用.
   */
  public Object platformPost(Object japi,   Object jbody,
                             Object jparam, Object jheader)
          throws ServletException {

    ScriptObjectMirror api = wrap(japi);
    ApiCall ac = new ApiCall(
            getStringAttr(api, "org"),
            getNNStringAttr(api, "app"),
            getNNStringAttr(api, "mod"),
            getNNStringAttr(api, "api")
    );
    //ac.exparam = new HashMap<>();

    ScriptObjectMirror ret = createJSObject();
    ScriptObjectMirror data = createJSObject();
    ret.setMember("data",   data);
    ret.setMember("code",   200);
    ret.setMember("cookie", null);
    ret.setMember("header", null);
    XResponse xr = new InnerXResponse(data);
    ac.call = new CallData(cd, xr);

    if (ac.org == null) {
      ac.org = AppContext.me().originalOrg();
    }

    if (jparam != null) {
      ScriptObjectMirror param = wrap(jparam);
      ac.exparam = param;
    }

    if (jbody != null) {
      ScriptObjectMirror body = wrap(jbody);
      if (ac.exparam == null) {
        ac.exparam = body;
      } else {
        ac.exparam.putAll(body);
      }
    }

    AppContext context = AppContext.me();
    //
    // 防止 api 脚本被编译成引入型脚本.
    //
    context.on(new ScriptEvent(EventFlag.me.OUT_REQUIRE, null));
    context.call(ac);
    return ret;
  }


  public Object get(String url) throws IOException {
    return get(url, null, "string", null);
  }


  public Object get(String url, Object param) throws IOException {
    return get(url, param, "string", null);
  }


  public Object get(String url, Object param, String type) throws IOException {
    return get(url, param, type, null);
  }


  public Object get(String url, Object param, String type, Object header)
          throws IOException {
    return post(url, null, param, type, header);
  }


  public Object post(String url) throws IOException {
    return post(url, null, null, "string", null);
  }


  public Object post(String url, Object bodydata) throws IOException {
    return post(url, bodydata, null, "string", null);
  }


  public Object post(String url, Object bodydata, Object param)
          throws IOException {
    return post(url, bodydata, param, "string", null);
  }


  public Object post(String url, Object bodydata, Object param,
                     String type) throws IOException {
    return post(url, bodydata, param, type, null);
  }


  public Object post(String url, Object bodydata, Object param,
                     String type, Object header) throws IOException {
    HttpUrl.Builder url_build = HttpUrl.parse(url).newBuilder();
    if (param != null) {
      addParm(url_build, param);
    }

    HttpUrl urlobj = url_build.build();
    Request.Builder build = new Request.Builder();
    build.url(urlobj);

    if (bodydata != null) {
      String bodystr = parseBodyParm(bodydata, type);
      RequestBody body = RequestBody.create(JSON, bodystr);
      build.post(body);
    }

    if (header != null) {
      addHeader(build, header);
    }

    try (Response resp = openClient().newCall(build.build()).execute()) {
      ResponseBody body = resp.body();

      ScriptObjectMirror ret = createJSObject();
      ret.setMember("code",   resp.code());
      ret.setMember("cookie", parseCookies(urlobj, resp));
      ret.setMember("data",   parseData(body, type));
      ret.setMember("header", parseRespHeader(resp));
      return ret;
    }
  }


  private Object parseData(ResponseBody body, String type) throws IOException {
    switch (type.toLowerCase()) {
      case "json":
        return jsonParse(body.string());

      case "xml":
        return Tool.createXmlStream().fromXML(body.string());

      case "string":
      default:
        return body.string();
    }
  }


  private String parseBodyParm(Object d, String type) {
    switch (type.toLowerCase()) {
      case "json":
        return jsonStringify(d);

      case "xml":
        return Tool.createXmlStream().toXML(d);

      case "string":
      default:
        return String.valueOf(d);
    }
  }


  private Object parseRespHeader(Response resp) {
    Headers hs = resp.headers();
    Iterator<String> names = hs.names().iterator();
    ScriptObjectMirror ret = createJSObject();

    while (names.hasNext()) {
      String name = names.next();
      ret.setMember(name, hs.get(name));
    }
    return ret;
  }


  private Object parseCookies(HttpUrl urlobj, Response resp) {
    List<String> cookieStrings = resp.headers("Set-Cookie");
    ScriptObjectMirror ck = createJSObject();

    for(int i=0, size = cookieStrings.size(); i < size; ++i) {
      Cookie cookie = Cookie.parse(urlobj, cookieStrings.get(i));
      ck.setMember(cookie.name(), cookie.value());
    }
    return ck;
  }


  private void addParm(HttpUrl.Builder url_build, Object param) {
    ScriptObjectMirror js = wrap(param);
    Iterator<String> names = js.keySet().iterator();
    while (names.hasNext()) {
      String name = names.next();
      url_build.addQueryParameter(name, String.valueOf(js.getMember(name)));
    }
  }


  private void addHeader(Request.Builder build, Object headers) {
    ScriptObjectMirror js = wrap(headers);
    Iterator<String> names = js.keySet().iterator();
    while (names.hasNext()) {
      String name = names.next();
      build.addHeader(name, String.valueOf(js.getMember(name)));
    }
  }


  private OkHttpClient openClient() {
    if (hc == null) {
      synchronized (this) {
        if (hc == null) {
          //
          // 这个对象可能很昂贵
          //
          hc = new OkHttpClient();
        }
      }
    }
    return hc;
  }


  public Object asyncPlatformGet(Object...p) {
    throw new UnsupportedOperationException("asyncPlatformGet");
  }


  public Object asyncPlatformPost(Object...p) {
    throw new UnsupportedOperationException("asyncPlatformPost");
  }
}
