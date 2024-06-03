////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-6-7 下午1:45
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/WebService.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.auth.IAResource;
import com.xboson.auth.PermissionSystem;
import com.xboson.auth.impl.LicenseAuthorizationRating;
import com.xboson.been.XBosonException;
import com.xboson.db.ConnectConfig;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.script.IVisitByScript;
import com.xboson.script.lib.JsInputStream;
import com.xboson.script.lib.JsOutputStream;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IHttp;
import com.xboson.util.c0nst.IXML;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WebService implements IAResource, IHttp, IXML, IVisitByScript {

  public static final int CONN_TIMEOUT = 15 * 1000;
  public static final int READ_TIMEOUT = 60 * 1000;
  public static final int CHUNK_SIZE   = 64 * 1024;
  public static boolean DEBUG = false;

  private final WSDLFactory fact;
  private final ConnectConfig db;
  private final Log log;


  public WebService() throws WSDLException {
    fact = WSDLFactory.newInstance();
    db   = SysConfig.me().readConfig().db;
    log  = LogFactory.create("js-web-service");
  }


  public WSConnection connection(Map setting) throws Exception {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    return new WSConnection(
            setting.get("curl").toString(),
            setting.get("name").toString(),
            setting.get("ns").toString() );
  }


  public WSConnection connection(String url, String func, String ns) throws Exception {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    return new WSConnection(url, func, ns);
  }


  public WSConnection connection(String key) throws Exception {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    Object[] parm = { key };
    try (SqlResult sr = SqlReader.query("ws_open", db, parm)) {
      ResultSet rs = sr.getResult();

      if (rs.next()) {
        String json = rs.getString(1);
        Map setting = Tool.getAdapter(Map.class).fromJson(json);
        log.debug("open ws", setting);
        return connection(setting);
      } else {
        throw new XBosonException.NotFound(
                "Web Service Setting: "+ key);
      }
    }
  }


  @Override
  public String description() {
    return "app.module.webservice.functions()";
  }


  public class WSConnection {
    private final String url;
    private final String func;
    private final String ns;

    /** 可用 soap 版本 10, 11, 12 */
    private int ver;
    private XmlImpl.XmlRoot root;
    private JsOutputStream output;
    private HttpURLConnection conn;


    private WSConnection(String url, String func, String ns)
            throws Exception {
      this.url  = url;
      this.func = func;
      this.ns   = ns;
      this.ver  = 12;
    }


    /**
     * 设置 soap 版本, 10, 11, 12 可用, 如果已经连接抛出异常.
     */
    public void setVersion(int v) {
      if (conn != null)
        throw new IllegalStateException("Is connecting");

      this.ver = v;
    }


    public JsOutputStream connect() throws Exception {
      if (conn != null)
        throw new IllegalStateException("Is connecting");

      conn = (HttpURLConnection) new URL(url).openConnection();
      conn.setReadTimeout(READ_TIMEOUT);
      conn.setConnectTimeout(CONN_TIMEOUT);
      conn.setChunkedStreamingMode(CHUNK_SIZE);
      ModuleHandleContext.autoClose(()-> conn.disconnect());
      conn.setRequestMethod(POST);
      conn.setDoInput(true);
      conn.setDoOutput(true);

      if (ver < 12) {
        conn.setRequestProperty(CONTENT_TYPE, CONTENT_XML + CONTENT_UTF8);
        conn.setRequestProperty(HEAD_SOAP, joinUrl(ns, func));
      } else {
        conn.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_SOAP);
      }
      conn.connect();
      output = new JsOutputStream(conn.getOutputStream());
      return output;
    }



    public XmlImpl.XmlTagWriter buildFunctionCall() throws IOException {
      if (conn == null)
        throw new IllegalStateException("Not connect");
      if (root != null)
        throw new IllegalStateException("Xml Building");

      root = new XmlImpl().build(output, DEBUG);
      root.writeHead();

      XmlImpl.XmlTagWriter env = root.tag(TAG_S_ENVELOPE)
              .attr(NS_XSD,  NS_XSD_URI)
              .attr(NS_XSI,  NS_XSI_URI)
              .attr(NS_SOAP, ver < 12 ? NS_SOAP_URI : NS_SOAP12_URI);

      XmlImpl.XmlTagWriter body = env.tag(TAG_S_BODY);
      XmlImpl.XmlTagWriter funcCall = body.tag(func).attr(NS, ns);
      return funcCall;
    }


    public JsInputStream openInput() throws IOException {
      if (conn == null)
        throw new IllegalStateException("Not connect");
      if (root == null)
        throw new IllegalStateException("Not build function");

      root.end();
      return new JsInputStream(conn.getInputStream());
    }


    public void end() throws IOException {
      if (conn == null)
        throw new IllegalStateException("Not connect");
      if (root == null)
        throw new IllegalStateException("Not build function");

      root.end();
    }
  }


  public Map wsdl(String url) throws WSDLException {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    WSDLReader reader = fact.newWSDLReader();
    Definition def = reader.readWSDL(url);
    return wsdl(def);
  }


  public Map wsdl(String url, String txt) throws WSDLException {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    WSDLReader reader = fact.newWSDLReader();
    InputSource input = new InputSource(new StringReader(txt));
    Definition def = reader.readWSDL(url, input);
    return wsdl(def);
  }


  private Map wsdl(Definition def) {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    Map<Object, Object> ret = new HashMap<>();
    Map parsedTypes = types(def);

    ret.put("ns",      def.getNamespaces());
    ret.put("uri",     def.getDocumentBaseURI());
    ret.put("doc",     getDoc(def));
    ret.put("modules", modules(def, parsedTypes));
    ret.put("types",   parsedTypes);
    return ret;
  }


  private Object modules(Definition def, Map parsedTypes) {
    Map<QName, PortType> modules = def.getPortTypes();
    Map<Object, Object> ret = new HashMap<>(modules.size());

    for (QName name : modules.keySet()) {
      PortType mod = modules.get(name);
      String modnm = name.getLocalPart();

      Map<String, Object> moduleCfg = new HashMap<>();
      ret.put(modnm, moduleCfg);

      String ctype = null, curi = null;
      ExtensibilityElement e = findInvokeInfo(mod, def);
      if (e != null) {
        if (e instanceof HTTPAddress) {
          curi  = ((HTTPAddress) e).getLocationURI();
          ctype = "http";
        }
        else if (e instanceof SOAPAddress) {
          curi  = ((SOAPAddress) e).getLocationURI();
          ctype = "soap";
        }
      }

      moduleCfg.put("uri",       curi);
      moduleCfg.put("ctype",     ctype);
      moduleCfg.put("name",      modnm);
      moduleCfg.put("doc",       getDoc(mod));
      moduleCfg.put("functions", functions(mod, parsedTypes, curi));
    }
    return ret;
  }


  private Object functions(PortType mod, Map parsedTypes, String curl) {
    String NS = mod.getQName().getNamespaceURI();
    List<Operation> oplist = mod.getOperations();
    Map<String, Object> ret = new HashMap<>(oplist.size());

    for (Operation func : oplist) {
      Map<String, Object> funcCfg = new HashMap<>();
      ret.put(func.getName(), funcCfg);

      funcCfg.put("name", func.getName());
      funcCfg.put("ns",   NS);
      funcCfg.put("doc",  getDoc(func));
      funcCfg.put("curl", curl);
      funcCfg.put("input",
          parseParameter(func.getInput().getMessage(), parsedTypes));
      funcCfg.put("output",
          parseParameter(func.getOutput().getMessage(), parsedTypes));
    }
    return ret;
  }


  private ExtensibilityElement findInvokeInfo(PortType mod, Definition def) {
    String modName = mod.getQName().getLocalPart();

    for (Object o : def.getServices().values()) {
      Service srv = (Service) o;
      Port port = (Port) srv.getPorts().get(modName);

      if (port != null) {
        List<ExtensibilityElement> ex = port.getExtensibilityElements();
        for (ExtensibilityElement e : ex) {
          if ("address".equals( e.getElementType().getLocalPart() )) {
            return e;
          }
        }
      }
    }
    return null;
  }


  private Object parseParameter(Message msg, Map types) {
    Map<String, Part> params = msg.getParts();
    Map<String, Object> ret = new HashMap<>(params.size());

    for (Part parm : params.values()) {
      QName type = parm.getTypeName();
      if (type == null) {
        type = parm.getElementName();
      }
      Object typeDesc = types.get(type.getLocalPart());
      ret.put(parm.getName(), typeDesc);
    }
    return ret;
  }


  private Map types(Definition def) {
    Types types = def.getTypes();
    List sub = types.getExtensibilityElements();
    Map ret = new HashMap<>();

    for (Object o : sub) {
      if (o instanceof Schema) {
        Schema s = (Schema) o;
        NodeList list = s.getElement().getChildNodes();
        types(list, ret, null);
      }
    }
    return ret;
  }


  /**
   * 生成的 map:
   *  [名称]: [类型]
   *  [名称]= 字符串
   *  [类型]= [描述][.]
   *  [描述]= $字符串 : 原始类型 | 字符串 : 变量名 | @字符串 : 组合类型
   */
  private void types(NodeList list, Map map, List valueTypes) {
    final int len = list.getLength();

    for (int i=0; i<len; ++i) {
      Node node = list.item(i);
      if ("#text".equals(node.getNodeName()))
        continue;

      String nodeName = '@'+ node.getLocalName();
      String pname = getAttr(node, "name");
      String ptype = getAttr(node, "type");

      if (map != null) {
        valueTypes = new ArrayList();
        NodeList list0 = node.getChildNodes();

        if (list0 != null && list0.getLength() > 0) {
          types(list0, null, valueTypes);
        } else {
          valueTypes.add("$"+ ptype);
        }

        map.put(pname!= null ? pname : nodeName, valueTypes);
      }
      else if (valueTypes != null) {
        String typeName;
        if (pname == null) {
          typeName = nodeName;
        } else {
          typeName = pname;
        }
        if (ptype != null) {
          typeName += "$"+ ptype;
        }
        valueTypes.add(typeName);
        NodeList list0 = node.getChildNodes();

        if (list0 != null && list0.getLength() > 0) {
          types(list0, null, valueTypes);
        }
      }
      else {
        throw new NullPointerException("Map/Types both null");
      }
    }
  }


  private String getAttr(Node n, String name) {
    NamedNodeMap map = n.getAttributes();
    if (map == null)
      return null;

    Node name_node = map.getNamedItem(name);
    if (name_node == null)
      return null;

    return name_node.getNodeValue();
  }


  private Object getDoc(WSDLElement e) {
    Element d = e.getDocumentationElement();
    if (d == null)
      return null;

    NodeList l = d.getChildNodes();
    StringBuilder buf = new StringBuilder();
    final int len = l.getLength();

    for (int i=0; i<len; ++i) {
      buf.append(l.item(i).getNodeValue());
    }
    return buf;
  }


  private String joinUrl(String a, String b) {
    if (a.charAt(a.length()-1) == '/') {
      return a + b;
    } else {
      return a +'/'+ b;
    }
  }
}
