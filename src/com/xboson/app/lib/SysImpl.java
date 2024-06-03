////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-23 上午11:20
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/SysImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.app.AppContext;
import com.xboson.app.ErrorCodeMessage;
import com.xboson.app.XjApp;
import com.xboson.auth.PermissionSystem;
import com.xboson.auth.impl.LicenseAuthorizationRating;
import com.xboson.been.CallData;
import com.xboson.been.XBosonException;
import com.xboson.been.XBosonException.BadParameter;
import com.xboson.db.ConnectConfig;
import com.xboson.db.SqlCachedResult;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.j2ee.files.DirectoryGenerate;
import com.xboson.j2ee.files.FileInfo;
import com.xboson.j2ee.files.PrimitiveOperation;
import com.xboson.j2ee.resp.XmlResponse;
import com.xboson.script.lib.Buffer;
import com.xboson.script.lib.Checker;
import com.xboson.util.*;
import com.xboson.util.c0nst.IConstant;
import com.xboson.util.converter.ScriptObjectMirrorJsonConverter;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.ServletInputStream;
import java.io.*;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * 每次请求一个实例; 在 sys 模块上调用 data 函数, 所以继承 DateImpl
 * (openid 就是 userid)
 */
public class SysImpl extends DateImpl {

  public static final String NULSTR = "";
  public static final String NULNULSTR = "null";

  private static final String ORG_SQL_NAME = "user_org_list.sql";
  private static final String USER_ID_TO_PID_SQL = "user_id_to_pid.sql";
  private static final String ADMIN_FLAG_SQL = "user_admin_flag.sql";
  private static final String PID_TO_UID_SQL = "pid_to_userid.sql";

  /**
   * 公共属性
   */
  public final boolean xboson = true;
  public final Object request;
  public final Object requestParameterMap;
  public ScriptObjectMirror result;
  public Object requestJson = null;

  private ConnectConfig orgdb;
  private ScriptObjectMirror printList;
  private ScriptObjectMirror transformTreeDataFunction;
  private ScriptObjectMirror getRelatedTreeDataFunction;
  private ScriptObjectMirror setRetListFunction;
  private Map<String, Object> appCache;
  private boolean isNestedCall;


  public SysImpl(CallData cd, ConnectConfig orgdb, XjApp app) {
    super(cd);
    this.orgdb = orgdb;
    this.request = new RequestImpl(cd);
    this.requestParameterMap = new RequestParametersImpl(cd);
    this.appCache = app.getCacheData();
    this.isNestedCall = AppContext.me().isNestedCall();
  }


  /**
   * 不初始化任何对象
   * @deprecated 仅用于测试
   */
  public SysImpl() {
    super(null);
    request = null;
    requestParameterMap = null;
  }


  public void parseBody() throws IOException {
    String type = cd.req.getContentType();
    if (type == null)
      return;

    ServletInputStream in = cd.req.getInputStream();
    if (in == null || in.isFinished())
      return;

    if (type.contains("application/json")) {
      StringBufferOutputStream buf = new StringBufferOutputStream();
      buf.write(in, true);
      requestJson = jsonParse(buf.toString());
    }
  }


  /**
   * 该方法将直接把数据压入应答数据集中, 无需 setRetData 进行压入.
   * 嵌套调用时不对结果做包装, 因为结果不会被发送到客户端.
   */
  void bindResult(String name, Object value) {
    if (! isNestedCall) {
      if (value instanceof ScriptObjectMirror) {
        value = new ScriptObjectMirrorJsonConverter.Warp(value);
      } else if (value instanceof ScriptObject) {
        value = new ScriptObjectMirrorJsonConverter.Warp(wrap(value));
      }
    }
    cd.xres.bindResponse(name, value);
  }


  public void put(String name, Object value) {
    bindResult(name, value);
  }


  public void addRetData(Object o) {
    addRetData(o, "result");
  }


  public void addRetData(Object value, String name) {
    if (result == null)
      result = createJSObject();

    if (Tool.isNulStr(name))
      return;

    result.setMember(name, unwrap(value));
  }


  public void addRetData(String name, Object value) {
    addRetData(value, name);
  }


  public void addRetData(String value, String name) {
    addRetData((Object) value, name);
  }


  public void setRetData(String code) throws IOException {
    setRetData(Integer.parseInt(code));
  }


  public void setRetData(String code, String msg, String ...parm)
          throws IOException {
    int c = Integer.parseInt(code);
    setRetData(c, msg, parm);
  }


  public void setRetData(int code) throws IOException {
    setRetData(code, ErrorCodeMessage.get(code));
  }


  public void setRetData(int code, String msg, String ...parm)
          throws IOException {
    cd.xres.setCode(code);
    cd.xres.setMessage(msg);

    if (result != null) {
      for (int i = 0; i < parm.length; ++i) {
        String name = parm[i];
        Object value = result.get(name);
        bindResult(name, value);

        String cname = name + _COUNT_SUFFIX_;
        Object count = result.get(cname);
        if (count != null) {
          bindResult(cname, count);
        }
      }
    }
    cd.xres.response();
  }


  public String getUserPID() {
    return cd.sess.login_user.pid;
  }


  public Object getUserPID(String userid) throws Exception {
    String ckey = "P1:"+ userid;
    Object pid = appCache.get(ckey);
    if (pid != null) return pid;

    try (SqlResult sr = SqlReader.query(USER_ID_TO_PID_SQL, orgdb, userid)) {
      ResultSet rs = sr.getResult();
      if (rs.next()) {
        pid = rs.getString(1);
        appCache.put(ckey, pid);
        return pid;
      }
    }
    return null;
  }


  public Map<String, Object> getUserPID(String[] users) throws Exception {
    Map<String, Object> ret = new HashMap<>(users.length);
    for (int i=0; i<users.length; ++i) {
      String uid = users[i];
      ret.put(uid, getUserPID(uid));
    }
    return ret;
  }


  public Object getUserAdminFlag() throws Exception {
    return getUserAdminFlag(
            cd.sess.login_user.userid,
            AppContext.me().originalOrg());
  }


  public Object getUserAdminFlag(String userid, String org) throws Exception {
    if (Tool.isNulStr(userid))
      throw new XBosonException.NullParamException("String userid");
    if (Tool.isNulStr(org))
      throw new XBosonException.NullParamException("String org");

    try (SqlCachedResult scr = new SqlCachedResult(orgdb)) {
      String sql = SqlReader.read(ADMIN_FLAG_SQL);
      List<Map<String, Object>> rows = scr.query(sql, userid, org);

      if (rows.size() > 0) {
        Map<String, Object> o = rows.get(0);
        return o.get("admin_flag");
      }
    }
    return null;
  }


  public String getUserIdByOpenId() {
    return cd.sess.login_user.userid;
  }


  public String getUserIdByOpenId(String open_id) {
    return open_id;
  }


  public Object getUserOrgList() throws Exception {
    return getUserOrgList(cd.sess.login_user.userid);
  }


  public Object getUserIdByPID(String[] pids) throws SQLException {
    ScriptObjectMirror ret = createJSObject();
    for (int i=0; i<pids.length; ++i) {
      String pid  = pids[i];
      ret.setMember(pid, getUserIdByPID(pid));
    }
    return ret;
  }


  public Object getUserIdByPID(String pid) throws SQLException {
    String ckey = "P0:"+ pid;
    String uid  = (String) appCache.get(ckey);
    if (uid != null) return uid;

    try (SqlResult sr = SqlReader.query(PID_TO_UID_SQL, orgdb, pid)) {
      ResultSet rs = sr.getResult();
      if (rs.next()) {
        uid = rs.getString(1);
        appCache.put(ckey, uid);
        return uid;
      }
    }
    return null;
  }


  public Object getUserIdByPID() throws Exception {
    return cd.sess.login_user.userid;
  }


  public Object getUserOrgList(String userid) throws Exception {
    ScriptObjectMirror retList = super.createJSList();
    try (SqlResult sr = SqlReader.query(ORG_SQL_NAME, orgdb, userid)) {
      ResultSet rs = sr.getResult();
      copyToList(retList, rs);
    }
    return unwrap(retList);
  }


  public Object getUserLoginExpiration() {
    return SysConfig.me().readConfig().sessionTimeout * 60;
  }


  public String uuid() {
    return Tool.uuid.ds();
  }


  public String getUUID() {
    return Tool.uuid.ds();
  }


  public long nextId() {
    return Tool.nextId();
  }


  public String randomNumber() {
    return randomNumber(6);
  }


  public String randomNumber(Integer i) {
    if (i == null) i = 6;
    return (long)(Math.random() * Math.pow(10, i)) + "";
  }


  public String randomDouble(int p, int s) {
    BigDecimal a = BigDecimal.valueOf(Math.random() * Math.pow(10, p));
    return a.setScale(s, BigDecimal.ROUND_DOWN).toString();
  }


  public int randomIntWithMaxValue(int max) {
    return (int)(Math.random() * max);
  }


  public String randomString(int len) {
    return Tool.randomString(len);
  }


  public String currentTimeString() {
    return Tool.formatDate(new Date());
  }


  /**
   * 特别不理解为什么 sys 上也有这个方法,
   * 该方法与 se.encodePlatformPassword 区别是 ps 已经 md5
   */
  public String encodePlatformPassword(String uid, String date, String ps) {
    return Password.v1(uid, ps, date);
  }


  public String getCurrentTimeString() {
    return currentTimeString();
  }


  public String pinyinFirstLetter(String zh) {
    return ChineseInital.getAllFirstLetter(zh);
  }


  public String getPinyinFirstLetter(String zh) {
    return ChineseInital.getAllFirstLetter(zh);
  }


  public String formattedNumber(double v, String f) {
    java.text.DecimalFormat df = new java.text.DecimalFormat(f);
    return df.format(v);
  }


  public Object instanceFromJson(String str) {
    return jsonParse(str);
  }


  public Object jsonFromInstance(Object obj) {
    return jsonStringify(obj);
  }


  public Object instanceFromXml(String xmlstr) {
    return Tool.createXmlStream().fromXML(xmlstr);
  }


  public Object xmlFromInstance(Object obj) throws IOException {
    ScriptObjectMirrorJsonConverter.Warp warp
            = new ScriptObjectMirrorJsonConverter.Warp(obj);

    Writer out = new StringWriter();
    out.append(XmlResponse.XML_HEAD);
    Tool.createXmlStream().toXML(warp, out);
    return out.toString();
  }


  public Object emptyToNull(String str) {
    if (str == null) return null;
    str = str.trim();
    if (str.length() <= 0) return null;
    if (str.equalsIgnoreCase("NULL")) return null;
    return str;
  }


  public Object isNumber(Object v) {
    try {
      Double.parseDouble(v.toString());
      return true;
    } catch(Exception e) {
      return false;
    }
  }


  public Object parseInt(Object v) {
    try {
      return Integer.parseInt(v.toString());
    } catch(Exception e) {
      return 0;
    }
  }


  public Object executeJavaScript(Object a, Object b) {
    throw new UnsupportedOperationException("executeJavaScript");
  }


  public void printValue(Object... values) {
    if (printList == null) {
      printList = createJSList();
      cd.xres.bindResponse("print",
              new ScriptObjectMirrorJsonConverter.Warp(printList));
    }
    for (Object v : values) {
      printList.setSlot(printList.size(), v);
    }
  }


  public Object bytes(String s) {
    return wrapBytes( s.getBytes(IConstant.CHARSET) );
  }


  public Object encodeBase64(String v) {
    return encodeBase64( v.getBytes(IConstant.CHARSET) );
  }


  public Object encodeBase64(byte[] b) {
    return wrapBytes( Base64.getEncoder().encode(b) );
  }


  public Object encodeBase64(Buffer.JsBuffer buf) {
    return encodeBase64( buf._buffer().array() );
  }


  public String encodeBase64String(String v) {
    return encodeBase64String( v.getBytes(IConstant.CHARSET) );
  }


  public String encodeBase64String(byte[] b) {
    return Base64.getEncoder().encodeToString(b);
  }


  public String encodeBase64String(Buffer.JsBuffer buf) {
    return encodeBase64String( buf._buffer().array() );
  }


  public Object decodeBase64(String v) {
    return wrapBytes( Base64.getDecoder().decode(v) );
  }


  public Object decodeBase64(byte[] b) {
    return wrapBytes( Base64.getDecoder().decode(b) );
  }


  public Object decodeBase64(Buffer.JsBuffer buf) {
    return decodeBase64( buf._buffer().array() );
  }


  public String decodeBase64String(String v) {
    return new String(Base64.getDecoder().decode(v), IConstant.CHARSET);
  }


  public String decodeBase64String(byte[] v) {
    return new String(Base64.getDecoder().decode(v), IConstant.CHARSET);
  }


  public String decodeBase64String(Buffer.JsBuffer buf) {
    return decodeBase64String( buf._buffer().array() );
  }


  public String md5(String v) {
    return Hex.upperHex(Password.md5(v));
  }


  public String encrypt(String content, String ps) throws Exception {
    AES2 ekey = new AES2(ps);
    return ekey.encrypt(content);
  }


  public String decrypt(String cipher_text, String ps) {
    AES2 ekey = new AES2(ps);
    return new String(ekey.decrypt(cipher_text), IConstant.CHARSET);
  }


  public boolean regexFind(String regex, String str) {
    return Pattern.matches(regex, str);
  }


  public boolean regexMatches(String regex, String str) {
    return regexFind(regex, str);
  }


  public Object regexSplit(String regex, String str) {
    return str.split(regex);
  }


  public String regexReplaceFirst(String regex, String str, String repl) {
    return str.replaceFirst(regex, repl);
  }


  public String regexReplaceAll(String regex, String str, String repl) {
    return str.replaceAll(regex, repl);
  }


  public Object lotteryRate(double[] list) {
    return lotteryRate(list, null);
  }


  /**
   * 俄罗斯轮盘赌 !!
   */
  public int lotteryRate(double[] list, int[] ign) {
    final double d = Math.random() * 101;
    double a = 0;
    int i;

    // ign 如果为 null, 性能最好
    if (ign != null && ign.length > 0) {
      // 防止对 list 的修改
      list = Arrays.copyOf(list, list.length);

      double share = 0;
      int listcount = list.length;

      // 计算要分摊的忽略列表中指定的项
      for (i=0; i<ign.length; ++i) {
        int index = ign[i];
        if (list[index] < 0) continue;
        share += list[index];
        list[index] = -1;
        --listcount;
      }

      // 将分摊值加入正常项
      share = share / listcount;
      for (i=0; i<list.length; ++i) {
        if (list[i] >= 0) {
          list[i] += share;
        }
      }
    }

    for (i=0; i<list.length; ++i) {
      if (list[i] >= 0) {
        a += list[i];
        if (d < a) {
          break;
        }
      }
    }
    return i;
  }


  /**
   * 将给出的目录与 DBFS 系统的目录连接,
   * 当 dir 有不安全字符串时抛出 checkMag 指定的异常.
   */
  private String getDirFromDBFileSystem(String dir, String checkMsg) {
    if (Tool.isNulStr(dir)) {
      dir = DirectoryGenerate.get(cd);
    } else {
      Checker.me.safepath(dir, checkMsg);
      dir = DirectoryGenerate.get(cd) + '/' + dir;
    }
    return Tool.normalize(dir);
  }


  /**
   * 从文件中解析 csv
   * @param fileInfo [dirname(忽略), filename, charset]
   * @see #parseCsv(Reader, String, String, String, String[], int)
   */
  public Object csvToList(String[] fileInfo, String delimiter, String quote,
                          String escape, String[] header, int preview) {
    String dir = getDirFromDBFileSystem(
            fileInfo[0], "param fileInfo[0] (dirname)");

    try (FileInfo info = PrimitiveOperation.me().openReadFile(dir, fileInfo[1])) {
      InputStreamReader reader = new InputStreamReader(info.input, fileInfo[2]);
      return parseCsv(reader, delimiter, quote, escape, header, preview);
    } catch (Exception e) {
      throw new XBosonException(e);
    }
  }


  /**
   * 解析 csv 字符串
   * @see #parseCsv(Reader, String, String, String, String[], int)
   */
  public Object csvToList(String content, String delimiter, String quote,
                          String escape, String[] header, int preview) {
    StringReader reader = new StringReader(content);
    return parseCsv(reader, delimiter, quote, escape, header, preview);
  }


  /**
   * 解析 csv 文本, 返回 list 对象
   *
   * @param read 读取 read 中的文本
   * @param delimiter csv 列分隔符
   * @param quote csv 使用引号来包装一个列值, quote 来定义引号,
   * @param escape (忽略) 使用引号包装后, 输出引号前的转义符号
   * @param header 自定义表头, null 或空数组则从文件中解析
   * @param preview >0 则只输出指定的行, 否则输出全部
   * @return
   */
  private Object parseCsv(Reader read, String delimiter, String quote,
                          String escape, String[] header, int preview) {
    CsvPreference.Builder cb = new CsvPreference.Builder(
            quote.charAt(0), delimiter.charAt(0), "\r\n");

    try (CsvMapReader csv = new CsvMapReader(read, cb.build())) {
      if (header == null || header.length < 1) {
        header = csv.getHeader(true);
      }
      ScriptObjectMirror list = createJSList();

      Map<String, String> row;
      for (int i=0; ;++i) {
        row = csv.read(header);
        if (row == null) break;
        if (preview > 0 && i >= preview-1) break;
        list.setSlot(i, createJSObject(row));
      }

      read.close();
      return list;
    } catch(Exception e) {
      throw new XBosonException(e);
    }
  }


  /**
   * 转换 list 数据为 csv, 并保存在文件中.
   *
   * @param dir 目录
   * @param filename 保存文件名
   * @param charset 输出编码
   * @param olist 输入数据数组
   */
  public void listToCsv(String dir, String filename,
                        String charset, Object olist) throws IOException {
    Pipe p = new Pipe((OutputStream out) -> {
      try {
        CsvMapWriter csv = new CsvMapWriter(
                new OutputStreamWriter(out, charset),
                CsvPreference.STANDARD_PREFERENCE);

        ScriptObjectMirror list = wrap(olist);
        ScriptObjectMirror firstRow = wrap(list.getSlot(0));
        String[] header = firstRow.getOwnKeys(false);
        csv.writeHeader(header);

        final int size = list.size();
        for (int i = 0; i < size; ++i) {
          ScriptObjectMirror js = wrap(list.getSlot(i));
          csv.write(js, header);
        }

        csv.flush();
      } catch (Exception e) {
        throw new XBosonException(e);
      }
    });

    dir = getDirFromDBFileSystem(dir, "param 1 (dir)");
    PrimitiveOperation.me().updateFile(dir, filename, p.openInputStream());
  }


  /**
   * 将符合条件的 clist 中的元素附加到 plist 元素的 keyname 属性上.
   * associate 可以设置多个属性, 他们是 '并且' 的关系.
   * 生成的 keyname 属性是一个数组, 数组中保存着复制来的 clist 中的元素.
   *
   * @param plist 元素是 object 对象
   * @param clist 元素是 object 对象
   * @param associate [[k1, k2],[...], ...] k1 是 plist 属性名, k2 是 clist 属性名.
   * @param keyname 在 plist 中创建的数组属性名称
   */
  public void setRetList(Object plist, Object clist,
                         Object associate, Object keyname) {
    if (setRetListFunction == null) {
      Object func = Context.getGlobal().get("setRetList");
      setRetListFunction = wrap(func);
    }

    setRetListFunction.call(null, plist, clist, associate, keyname);
  }


  /**
   * 将平行的 list 数据转换为深层 tree 格式; 数据对象根据属性 child_key 来寻找含有
   * 属性 parent_key 的数据对象, 并将自身附加到属性名 keyname 的数组上;
   * 如果数据对象 parent_key 为 null, 则认为是根节点;
   * 支持无限深层的 tree 数据格式.
   *
   * @param dataList 原始数据
   * @param parent_key 父节点属性名称
   * @param child_key 子节点属性名称
   * @param key_name 生成的子节点数组
   * @return 返回 tree 格式的数据
   */
  public Object transformTreeData(Object dataList, String parent_key,
                                  String child_key, String key_name) {
    if (transformTreeDataFunction == null) {
      Object function = Context.getGlobal().get("transformTreeData");
      transformTreeDataFunction = wrap(function);
    }

    return transformTreeDataFunction.call(
            null, dataList, parent_key, child_key, key_name);
  }


  /**
   * 迷之算法
   */
  public Object getRelatedTreeData(Object all, Object filter,
                                   String parent_attr, String child_attr) {
    if (getRelatedTreeDataFunction == null) {
      Object func = Context.getGlobal().get("getRelatedTreeData");
      getRelatedTreeDataFunction = wrap(func);
    }

    return getRelatedTreeDataFunction.call(
            null, all, filter, parent_attr, child_attr);
  }


  public boolean isEmpty(String o) {
    return Tool.isNulStr(o);
  }


  public boolean isEmpty(Object[] arr) {
    return arr == null || arr.length == 0;
  }


  public boolean isEmpty(ScriptObject js) {
    return js.isEmpty();
  }


  public boolean isEmpty(Object n) {
    return n == null;
  }


  public String toString(Object o) {
    return o.toString();
  }


  public boolean toBool(boolean b) {
    return b;
  }


  public boolean toBool(String s) {
    return s != null && (s.equals("1") || s.equalsIgnoreCase("true"));
  }


  public boolean toBool(Number n) {
    return n.intValue() > 0;
  }


  public boolean toBool(Object o) {
    return false;
  }


  public char charAt(String str, int index) {
    return str.charAt(index);
  }


  public int indexOf(String str, String find) {
    return str.indexOf(find);
  }


  public int size(Object js) {
    return wrap(js).size();
  }


  public boolean startWith(String str, String begin) {
    return str.startsWith(begin);
  }


  public boolean endWith(String str, String end) {
    return str.endsWith(end);
  }


  public int length(String str) {
    return str.length();
  }


  public String subString(String str, int begin) {
    if (Tool.isNulStr(str)) return NULSTR;
    if (begin >= str.length()) return NULSTR;
    return str.substring(begin);
  }


  public String subStringTo(String str, int begin, int end) {
    if (Tool.isNulStr(str)) return NULSTR;
    if (begin >= str.length()) return NULSTR;
    return str.substring(begin, end);
  }


  public Object split(String str, String regex) {
    String[] s = str.split(regex);
    ScriptObjectMirror js = createJSList(s.length);
    for (int i=0; i<s.length; ++i) {
      js.setSlot(i, s[i]);
    }
    return js;
  }


  public boolean contain(String str, String sub) {
    return str.contains(sub);
  }


  public String toLowerCase(String str) {
    return str.toLowerCase();
  }


  public String toUpperCase(String str) {
    return str.toUpperCase();
  }


  public String replace(String str, String what, String replacement) {
    return str.replace(what, replacement);
  }


  public String format(String format, Object[] parm) {
    return MessageFormat.format(format, parm);
  }


  /**
   * 对于 null 返回 "", 对于 "null" 也返回 "", 否则返回 s.trim()
   */
  public String trim(String s) {
    if (s == null) return NULSTR;
    s = s.trim();
    if (s.equals(NULNULSTR)) return NULSTR;
    return s;
  }


  public double trunc(double d, int scale) {
    return BigDecimal.valueOf(d)
            .setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
  }


  /**
   * 压缩 list 到 path 目录中, 动态生成文件.
   * 尝试提前打开 blob 输出流, 而不是将数据堆积在内存.
   *
   * @param list 要压缩的数据列表, 数据结构: [{name: 压缩项目名, content: 内容字符串,
   *             path: 读取文件路径}, {..}, ...], content/path 只能选一个使用.
   * @param path 保存目录, 基于当前用户目录的子目录
   * @return 返回生成的文件名
   */
  public String listToZip(Object[] list, final String path) throws Exception {
    Checker.me.safepath(path, "param 2 (path)");
    String dir  = Tool.normalize( DirectoryGenerate.get(cd) + '/' + path);
    String file = Tool.uuid.v4() + ".zip";

    StringBufferOutputStream buf = new StringBufferOutputStream();
    ZipOutputStream zip = new ZipOutputStream(buf);

    for (int i=0; i<list.length; ++i) {
      ScriptObjectMirror obj = wrap(list[i]);
      ZipEntry zipEntry = new ZipEntry(obj.getMember("name").toString());
      zip.putNextEntry(zipEntry);

      if (obj.hasMember("content")) {
        String content = (String) obj.getMember("content");
        zip.write(content.getBytes(IConstant.CHARSET));
        continue;
      }

      if (obj.hasMember("path")) {
        String ipath = (String) obj.getMember("path");
        try (FileInfo in = PrimitiveOperation.me().openReadFile(dir, ipath)) {
          Tool.copy(in.input, zip, false);
        }
        continue;
      }

      throw new XBosonException("Nothing to write zip file. " +
              "list[{ name, content, path }], must set content or path.");
    }

    zip.closeEntry();
    zip.finish();
    zip.flush();

    PrimitiveOperation.me().updateFile(dir, file, buf.openInputStream());
    return file;
  }


  /**
   * 解压缩文件, 返回解压的数据
   *
   * @param path 压缩文件路径, 基于当前用户目录的子目录
   * @param filename 压缩文件名
   * @return 解压的 list 数据对象
   */
  public Object zipToList(String path, String filename) throws Exception {
    Checker.me.safepath(path, "param 1 (path)");
    String dir  = Tool.normalize( DirectoryGenerate.get(cd) + '/' + path);

    ScriptObjectMirror arr = createJSList();
    int reti = arr.size()-1;

    try (FileInfo file = PrimitiveOperation.me().openReadFile(dir, filename)) {
      ZipInputStream zip = new ZipInputStream(file.input);

      for (;;) {
        ZipEntry entry = zip.getNextEntry();
        if (entry == null)
          break;

        ScriptObjectMirror jentry = createJSObject();
        arr.setSlot(++reti, jentry);
        jentry.setMember("name", entry.getName());

        StringBufferOutputStream buf = new StringBufferOutputStream();
        Tool.copy(zip, buf, false);
        jentry.setMember("content", buf.toString());
      }

      zip.close();
    }
    return arr;
  }


  public String setReportData(String fileName, String dataName,
                              String readPath, String savePath) {
    if (dataName == null)
      throw new BadParameter("2", "dataName is null");

    Object data = result.getMember(dataName);
    if (data == null)
      throw new BadParameter("2",
              "Cannot found bind result `" + dataName + '`');

    return setReportData(fileName, data, readPath, savePath);
  }


  public String setReportData(String fileName, Object data,
                              String readPath, String savePath) {
    ScriptObjectMirror list = wrap(data);
    if (! list.isArray())
      throw new BadParameter("2", "data not List");

    if (Tool.isNulStr(fileName))
      throw new BadParameter("1", "fileName not string");

    if (Tool.isNulStr(readPath))
      throw new BadParameter("3", "readPath not string");

    if (Tool.isNulStr(savePath))
      throw new BadParameter("4", "savePath not string");

    Checker.me.safepath(readPath, "param 3 (readPath)");
    Checker.me.safepath(savePath, "param 4 (savePath)");
    final String SHEET_NAME = "data";
    final String base = DirectoryGenerate.get(cd);
    final String readDir = Tool.normalize( base + '/' + readPath);
    final String saveDir = Tool.normalize( base + '/' + savePath);

    try (CloseableSet close = new CloseableSet()) {
      Workbook wb;
      try {
        FileInfo file = PrimitiveOperation.me().openReadFile(readDir, fileName);
        close.add(file);
        wb = new HSSFWorkbook(file.input);
      } catch(Exception e) {
        wb = new HSSFWorkbook();
      }

      close.add(wb);
      Sheet sheet = wb.getSheet(SHEET_NAME);
      if (sheet == null) {
        sheet = wb.createSheet(SHEET_NAME);
      }

      Map<String, Integer> name2num = new HashMap<>();
      Ref<Integer> rowNum = new Ref<>(0);
      writeList(list, sheet, name2num, rowNum);

      wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
      StringBufferOutputStream buf = new StringBufferOutputStream();
      wb.write(buf);

      String saveFile = generateTimeName(fileName);
      PrimitiveOperation.me().updateFile(
              saveDir, saveFile, buf.openInputStream());

      return saveFile;
    } catch (IOException e) {
      throw new XBosonException.IOError(e);
    }
  }


  private void writeList(ScriptObjectMirror list, Sheet sheet,
                         Map<String, Integer> name2num, Ref<Integer> rowNum) {
    final int size = list.size();
    for (int i=0; i<size; ++i) {
      Object o = list.getSlot(i);
      ScriptObjectMirror obj;
      if (o instanceof String) {
        obj = wrap(result.getMember((String) o));
      } else {
        obj = wrap(o);
      }

      if (obj.isArray()) {
        writeList(obj, sheet, name2num, rowNum);
      } else {
        Row row = sheet.createRow(rowNum.x);
        writeRow(obj, row, name2num);
        rowNum.x += 1;
      }
    }
  }


  private void writeRow(ScriptObjectMirror obj, Row row,
                        Map<String, Integer> name2num) {
    for (Map.Entry<String, Object> entry : obj.entrySet()) {
      String colName = entry.getKey();
      Integer c = name2num.get(colName);
      if (c == null) {
        c = name2num.size();
        name2num.put(colName, c);
      }

      Cell cell = row.createCell(c);
      Object value = entry.getValue();

      if (value instanceof Date) {
        cell.setCellValue((Date) value);
      }
      else if (value instanceof Number) {
        cell.setCellValue(((Number) value).doubleValue());
      }
      else if (value instanceof Boolean) {
        cell.setCellValue((boolean) value);
      }
      else {
        cell.setCellValue(String.valueOf(entry.getValue()));
      }
    }
  }


  private String generateTimeName(String fileName) {
    String name, ext;
    int dot = fileName.lastIndexOf(".");
    if (dot >= 0) {
      name = fileName.substring(0, dot);
      ext  = fileName.substring(dot);
    } else {
      name = fileName;
      ext  = "";
    }
    return name +'_'+ Tool.formatDate(new Date()) + ext;
  }


  /**
   * 没有 api 用到这个函数, 通过其他函数组合可以实现
   */
  public String convertCsvToXls(String fileName, String csv) {
    throw new UnsupportedOperationException("convertCsvToXls");
  }


  /**
   * 没有 api 用到这个函数
   */
  public void bizLog(String logid, Object... params) {
    throw new UnsupportedOperationException("bizLog");
  }


  public Object httpGet(Object... params) {
    throw new UnsupportedOperationException("Use http.platformGet(..)");
  }


  /**
   * 检查授权证书中是否有 authName 的使用授权, 未授权抛出异常.
   *
   * ! 隐藏函数不做文档 !
   */
  public void authorization(String authName) {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, ()-> authName);
  }
}
