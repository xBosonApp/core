////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月10日 09:26
// 原始文件路径: xBoson/src/com/xboson/test/js/check-safe.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var if_throw_ok = require("./util.js").if_throw_ok;
var assert = require("assert");


assert.eq(typeof safe_context, 'undefined', "safe_context not safe");
assert.eq(typeof fncontext, 'undefined', 'fncontext not safe');

var con = require("console").create(null).log('heha');

//
// 模拟一个com.xboson.script.Application 的实例
//
var _app = {
  isCached: function() { return true; },
  flag: { IN_REQUIRE: 1, SCRIPT_OUT: 2 },
  sendScriptEvent: function() {},
  run: function(name) {
    return { exports: require(name) };
  },
};

try {
  var safe__warp_main = false;
  __warp_main = null;
  __warp_main(function() {
    safe__warp_main = true;
  })({ filename: 'test' }, _app);
  assert(safe__warp_main, "__warp_main not safe");
} catch(e) {
  if (e.message.indexOf("undefined") >= 0) {
    console.error("出错原因可能是代码有改动, Application 模拟不完整");
  }
  throw e;
}


assert.throws(function() {
  crossval = 109;
  if (require('./deep.js').t2() == crossval) {
    throw new Error("!! change sand box context value");
  }
}, /ReferenceError.*crossval/);


assert.throws(function() {
  var a = require("./deep.js");
  a.terr(10, 'deep exception');
}, /Error.*deep exception/);


assert.throws(function() {
  load("foo.js");
}, /ReferenceError.*load/);


assert.throws(function() {
  var System  = Java.type("java.lang.System");
  console.error("fail:", System);
  fail = true;
}, /ReferenceError.*Java/);


assert.throws(function() {
  var imports = new JavaImporter(java.util, java.io);
}, /ReferenceError.*JavaImporter/);
  
  
assert.throws(function() {
  var r = new java.lang.Runnable() {
      run: function() { print("run"); }
  };
}, /ReferenceError.*lang/);


assert.throws(function() {
  var o = console.getClass().getResource("/");
}, /reflection not supported/);


assert.eq(typeof $ENV, "undefined", "$ENV");
assert.eq(typeof $EXEC, "undefined", "$EXEC");
assert.eq(typeof $ARG, "undefined", "$ARG");


//if_throw_ok(function() {
//  var files = `ls -l`;
//  console.log(files);
//}, "block ls -l on system process");


assert.throws(function() {
  eval("1+1");
}, /ReferenceError.*eval/);


assert.throws(function() {
  exit(99);
}, /ReferenceError.*exit/);


assert.throws(function() {
  quit(98);
}, /ReferenceError.*quit/);


assert.throws(function() {
  var List = java.util.List;
}, /ReferenceError.*util/);


assert.throws(function() {
  var JFrame = javax.swing.JFrame;
}, /ReferenceError.*swing/);


assert.throws(function() {
  var Vector = Packages.java.util.Vector;
}, /ReferenceError.*Packages/);


assert.throws(function() {
  Buffer.from([]).eval("1+1");
}, /TypeError.*.*function.*/);


//
// 因为默认上下文禁止创建变量, 而 name 定义在默认上下文导致错误.
//
assert.throws(function() {
  var arr = {a:1, b:2, c:3};
  for (name in arr) {
    console.log(name, arr[name], "is working !!!!!!!!!!!!!!!!!!");
  }
}, /ReferenceError.*name/);


console.log('ok');