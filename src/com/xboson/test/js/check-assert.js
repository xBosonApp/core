////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月11日 10:25
// 原始文件路径: xBoson/src/com/xboson/test/js/check-assert.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var if_throw_ok = require("./util.js").if_throw_ok;
var assert = require("assert");


assert.ok(true, 'nothing');
if_throw_ok(function() {
  assert(0, 'must throw');
}, 'assert working');

assert.deepEqual({a:1, b:2}, {a:1, b:2}, "deepEqual");
if_throw_ok(function() {
  assert.deepEqual({a:1, b:2}, {a:1, b:[1,2]});
}, 'deepEqual2');
assert.notDeepEqual({a:1, b:2}, {a:1, b:[1,2]});

if_throw_ok(function() {
  assert.deepEqual({a:1, b:2, c:{a:1}}, {a:1, b:2, c:{a:2}});
}, 'deepEqual3');
assert.notDeepEqual({a:1, b:2, c:{a:1}}, {a:1, b:2, c:{a:2}});

if_throw_ok(function() {
  assert.doesNotThrow(function() {
    throw new TypeError('错误信息');
  }, SyntaxError);
}, 'doesNotThrow() 1');

if_throw_ok(function() {
  assert.doesNotThrow(function() {
      throw new TypeError('错误信息');
  }, TypeError);
}, 'doesNotThrow() 2');

if_throw_ok(function _t() {
  assert.fail('a', 'e', 'm', 'op', _t);
}, 'assert.fail');

assert.throws(function() {
  throw new Error();
}, Error, 'good');

if_throw_ok(function () {
  assert.throws(function() {
    throw new Error();
  }, TypeError, 'throws');
}, 'assert.throw');

assert.notStrictEqual(1, 2);
assert.ok(1);
assert.notEqual(1, 2);
assert.notDeepStrictEqual({ a: 1 }, { a: '1' });
assert.notDeepEqual({ a: 1 }, { a: 2 });
assert.ifError(0);
assert.equal(1, '1');
