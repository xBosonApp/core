//
// 该文件配合 TestApi.java
// org : a297dfacd7a84eab9656675f61750078
// app : a9943b0fb1e141b3a3ce7e886d407f5b
// mod : test_double
//
var assert     = require('assert');

var requestId  = sys.request.requestid;                    // 请求ID，平台会针对每个用户或服务请求生成一个请求ID，此ID
var searchDate = sys.request.searchdate;                   // 获得字符串参数，多个值时逗号分隔
var values     = sys.requestParameterMap.checkbox_values;  // 获得数组参数，无论单个多个值都返回字符串数组
var json       = sys.requestJson;

console.warn("\n\n<--- Script From DS platform, Run in xBoson ------------------------------------------------->");
assert.ok(requestId);
assert.ok(searchDate);
assert.ok(values);
// assert.ok(json); not implement

console.log("requestId:", requestId);
console.log("searchDate:", searchDate);
console.log("values:", values);
console.log("json:", json);


var testObjectA = {
  aa:"test string A"
};
var testNumber = 999;
var testObjectB = {
  bb:"test string B",
  cc:testNumber,
  dd:testObjectA
};
var testObjectC = {
  ee:"test string C"
};
var testList = [testObjectB, testObjectC];
sys.addRetData(testList, "result");
sys.setRetData(0, "new msg", "result");


var currentPId = sys.getUserPID();
var pids = sys.getUserPID(["demo", "root", "_no_exist"]);
console.log("currentPId", currentPId);
console.log("pids", pids);
console.log("pid demo", pids["demo"]);
assert.eq('e3e5cf168dd24b44ba4b72775d5fb215', currentPId);
assert.eq('ee5dcaf3cfaa42dc9b58ea0841341745', pids["demo"]);


console.log("sys.getUserAdminFlag", sys.getUserAdminFlag());
console.log("sys.getUserIdByOpenId", sys.getUserIdByOpenId());
assert.ok(parseInt(sys.getUserAdminFlag()) >= 0, "getUserAdminFlag");
assert.ok(sys.getUserIdByOpenId().length > 0, "getUserIdByOpenId");


var orgList = sys.getUserOrgList();
// console.log("orgList", orgList);
assert(orgList);
if (orgList != null) {
  console.error("不兼容的写法, row 实际为数组下标, 而非数组元素");
  for (var row in orgList) {
    // console.log("ROW", row, orgList[row]);
    //
    // var orgId = row.orgid;
    // var orgName = row.orgnm;
    // var orgType = row.org_type;  // "v":开发商, "t":租户
    //
    console.log("Org ID:", row.orgid, "Name:", row.orgnm, "Type:", row.org_type);
    assert(row);
    assert(row.orgid);
    assert(row__index >= 0);
  }
}


assert(sys.getUserLoginExpiration() > 0, "sys.getUserLoginExpiration");
assert(sys.uuid().length > 0, "sys.uuid()");
assert(sys.nextId() > 0, "sys.uuid()");
assert(Number(sys.randomNumber(3)) < 10e3, "sys.randomNumber(3)");
assert(Number(sys.randomNumber()) < 10e6, "sys.randomNumber()");
assert(Number(sys.randomNumber(10)) < 10e10, "sys.randomNumber(10)");
assert(Number(sys.randomNumber(10)) > 0, "sys.randomNumber(10)");
assert(sys.randomDouble(3, 2) < 1000, "sys.randomDouble(3, 2)");
assert(sys.randomDouble(1, 2) < 10, "sys.randomDouble(1, 2)");
assert(sys.randomDouble(6, 2) < 10e6, "sys.randomDouble(6, 2)");
assert(sys.randomDouble(6, 2) > 0, "sys.randomDouble(6, 2)");
assert(sys.randomIntWithMaxValue(10) < 10, "sys.randomIntWithMaxValue(10)");
assert(sys.randomIntWithMaxValue(1000) < 1000, "sys.randomIntWithMaxValue(1000)");
assert(sys.randomIntWithMaxValue(1000) > 0, "sys.randomIntWithMaxValue(1000)");
assert(sys.randomString(10).length == 10, "sys.randomString(10)");
assert(sys.randomString(5).length == 5, "sys.randomString(5)");
assert.eq("hqhz", sys.pinyinFirstLetter("获取汉字"), "sys.pinyinFirstLetter");
assert.eq("rzxlszyykpbgqc", sys.pinyinFirstLetter("日照香炉生紫烟遥看瀑布挂前川"), "sys.pinyinFirstLetter");
assert.eq("12.35", sys.formattedNumber(12.3456, "#.##"), "sys.formattedNumber");


var j = '{"aa":1,"bb":2}';
var x = sys.instanceFromJson(j);
assert.eq(1, x.aa, sys.instanceFromJson);
assert.eq(2, x.bb, sys.instanceFromJson);
assert.eq(j, sys.jsonFromInstance(x), "sys.jsonFromInstance");


var xmlobj = { a:1, b:2, c:3, zz:null, d:[1,2,3], e:{a:'a', b:'b', c:[1,2,null,3]}, f:true };
var xmlstr = sys.xmlFromInstance(xmlobj);
console.log("sys.xmlFromInstance()", xmlstr);
var obj = sys.instanceFromXml(xmlstr);
assert.eq(JSON.stringify(xmlobj), JSON.stringify(obj), "sys.instanceFromXml");
assert.eq(1, obj.a);
assert.eq(3, obj.c);
assert(obj.f === true, "boolean type:" + (typeof obj.f) +" > "+ obj.f);


assert.eq(null, sys.emptyToNull(""), "sys.emptyToNull()");
assert.eq(null, sys.emptyToNull(" "), "sys.emptyToNull()");
assert.eq(null, sys.emptyToNull("nUlL"), "sys.emptyToNull()");
assert.eq("abc", sys.emptyToNull("abc"), "sys.emptyToNull()");


assert(sys.isNumber(100), "sys.isNumber");
assert(sys.isNumber('100'), "sys.isNumber");
assert(!sys.isNumber("xjfe"), "sys.isNumber");

// only call
sys.printValue("ok print");

var b = sys.bytes("111111");
assert.eq("[49,49,49,49,49,49]", JSON.stringify(b), "sys.bytes");

var a = sys.encodeBase64("111111");
var b = sys.encodeBase64([1,1,1,1,1,1]);
console.log(JSON.stringify(a), JSON.stringify(b));
assert(a, "sys.encodeBase64");
assert(b, "sys.encodeBase64");


var ret = sys.encodeBase64String("111111");
assert.eq("MTExMTEx", ret, "sys.encodeBase64String");
console.log("Base64:", ret);


var aa = sys.decodeBase64(a);
console.log(JSON.stringify(aa), new String(aa));
assert.eq("111111", sys.decodeBase64String(a), "sys.decodeBase64String");


assert.eq("96E79218965EB72C92A549DD5A330112", sys.md5("111111"));
var passw = "key123";
var ct = sys.encrypt("123", passw);
var d;
assert.eq("E696D15FF4DFD1F74413F101DF719702", ct, "sys.encrypt");
assert.eq("123", d = sys.decrypt(ct, passw), "sys.decrypt");
console.log("sys.encrypt/decrypt", passw, ct, d);


assert(sys.regexFind("^Java.*", "Java测试"), "sys.regexFind");
var ret = sys.regexSplit("[, |]+", "Java Hello World  Java,Hello,,World|Sun");
console.log(ret);
assert.eq(ret[0], "Java");
assert.eq(ret[1], "Hello");


var ret = sys.regexReplaceFirst("正则表达式", "正则表达式 Hello World 1, 正则表达式 Hello World 2", "正");
assert.eq("正 Hello World 1, 正则表达式 Hello World 2", ret, "sys.regexReplaceFirst");
var ret = sys.regexReplaceAll("正则表达式", "正则表达式 Hello World 1, 正则表达式 Hello World 2", "正");
assert.eq("正 Hello World 1, 正 Hello World 2", ret, "sys.regexReplaceAll");

var sum = [0,0,0,0,0];
for (var i=0; i<10000; ++i) {
  // 比率总和小于100，将随机返回 0/2/4 中的某一个值
  var ret = sys.lotteryRate([20.5, 10.0, 50.5, 10.0], [1, 3]);
  sum[ret] += 1;
}
for (var i=0; i<sum.length; ++i) {
  sum[i] = (sum[i] / 10000 * 100).toFixed(2);
}
assert(Math.abs(30 - parseFloat(sum[0])) < 1, "sys.lotteryRate");
assert.eq(0, sum[1], "sys.lotteryRate");
console.log("sys.lotteryRate", JSON.stringify(sum));

var clist = [{a:"1", b:'2'}, {a:'3', b:'4'}, {a:'5', b:'6'}];
// save to file
sys.listToCsv(null, "list.csv", "utf8", clist);
var list = sys.csvToList([ "tmpCSV/", "list.csv", "UTF-8" ], ",", "\"" , "\\", [], 10);
console.log("sys.csvToList", JSON.stringify(list));
assert.eq(JSON.stringify(clist), JSON.stringify(list), "listToCsv / csvToList");
var csvstr = "A,B\n1,2\n3,4\n5,6";
console.log('sys.csvToList(String...)', JSON.stringify(sys.csvToList(csvstr, ",", "\"" , "\\", [], 10)));

var plist = [{id:1, a:1,b:2,c:3}, {id:5, a:6, b:7, c:8}];
sys.setRetList(plist, clist, [['id', 'a']], 'sub');
console.log("sys.setRetList", JSON.stringify(plist));
assert.eq("1", plist[0].sub[0].a);
assert.eq("5", plist[1].sub[0].a);


var tlist = [{id:1, pid:null}, {id:2, pid:1}, {id:3}, {id:4, pid:2}];
var tree = sys.transformTreeData(tlist, 'id', 'pid', 'sub');
console.log('sys.transformTreeData', JSON.stringify(tree));
assert.eq(2, tree[0].sub[0].id);
assert.eq(4, tree[0].sub[0].sub[0].id);


var tlist2 = [{id:1, pid:null}, {id:2, pid:1}, {id:3}, {id:4, pid:2}];
var r = sys.getRelatedTreeData(tlist2, [{id:10, pid:1}], 'id', 'pid');
console.log('sys.getRelatedTreeData', JSON.stringify(r));


assert(sys.isEmpty(null), "sys.isEmpty()");
assert(sys.isEmpty([]), "sys.isEmpty()");
assert(sys.isEmpty({}), "sys.isEmpty()");
assert(sys.isEmpty(""), "sys.isEmpty()");

assert.eq("123", sys.toString(123), "sys.toString");
assert(sys.toBool("true"), 'sys.toBool');
assert(sys.toBool("tRue"), 'sys.toBool');
assert(sys.toBool("1"), 'sys.toBool');
assert(!sys.toBool({}), 'sys.toBool');
assert.eq('b', sys.charAt("abc", 1), "sys.charAt");
assert.eq(1, sys.indexOf('abc', 'bc'), "sys.indexOf");
assert.eq(3, sys.size([1,2,3]), "sys.size");
assert.eq(2, sys.size({a:2,b:3}), "sys.size");

assert(sys.startWith("hello", 'he'), "sys.startWith");
assert(sys.endWith("hello", 'lo'), "sys.endWith");
assert.eq(5, sys.length("hello"), "sys.length");
assert.eq("ello", sys.subString('hello', 1), "sys.subString");
assert.eq("e", sys.subStringTo("hello", 1, 2), "sys.subStringTo");

var a = sys.split("hello", 'hi');
var b = sys.split("hello.hi", '.');
var c = sys.split("hello.hi", '\\.');
console.log(JSON.stringify([a,b,c]));
assert.eq('hello', a[0]);
assert(sys.isEmpty(b));
assert.eq('hello', c[0]);
assert.eq('hi', c[1]);

assert(sys.contain('hello', 'h'), 'sys.contain');
assert.eq("HELLO", sys.toUpperCase('hello'), 'sys.toUpperCase');
assert.eq("hello", sys.toLowerCase('HELLO'), 'sys.toLowerCase');
assert.eq("hellooo", sys.replace("hello", "o", "ooo"), "sys.replace");
assert.eq("hello there, my name is jym",
  sys.format("hello {0}, my name is {1}", ['there', 'jym']), "sys.format");
assert.eq("x", sys.trim(' x '), 'sys.trim');
assert.eq(12.35, sys.trunc(12.345, 2), 'sys.trunc');
assert.eq(12, sys.trunc(12.345, 0), "sys.trunc");


var filename = sys.listToZip([
  { name : "1", content: JSON.stringify(tlist2) },
  { name : "2", content: JSON.stringify(clist) }
  ], "");
console.log('sys.listToZip:', filename);

var rlist = sys.zipToList("", filename);
console.log("sys.zipToList:", JSON.stringify(rlist));
assert.eq(JSON.stringify(tlist2), rlist[0].content, "sys.zipToList");
assert.eq(JSON.stringify(clist), rlist[1].content, "sys.zipToList");
assert.eq("1", rlist[0].name, "sys.zipToList");
assert.eq("2", rlist[1].name, "sys.zipToList");

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// All Over
//
console.warn("\n\n<---- <<<<< OK >>>>> Run in xBoson ---------------------------------------------------------->");
//
// All Over
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////