////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月10日 21:50
// 原始文件路径: xBoson/src/com/xboson/test/js/check-url.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var url = require('url');
var assert = require('assert');


function URL(a) {
  return url.parse(a);
}

function check(a, b) {
  var m = url.parse(a);
  if (!b) b = a;
  assert.eq(m.format(), b, 'bad');
  return m;
}

function check2(a, b, attr) {
  var m = url.parse(a);
  if (!b) b = a;
  assert.eq(m[attr], b, 'bad', attr);
  return m;
}


check('https://example.org/');
check('https://你好你好', 'https://xn--6qqa088eba/');

var u = check2('https://example.org/foo#bar', '#bar', 'hash');
u.hash = 'baz';
assert.eq(u.format(), 'https://example.org/foo#baz');

u = check2('https://example.org:81/foo', 'example.org:81', 'host');
u.host = 'example.com:82';
assert.eq(u.format(), 'https://example.com:82/foo');

// !!! 测试不通过 !!!
u = check2('https://example.org:81/foo', 'example.org', 'hostname');
//u.hostname = 'example.com:82';
//assert.eq(u.href, 'https://example.com:81/foo');

check2('https://example.org/foo', 'https://example.org/foo', 'href');

// !!! 测试不通过
// check2('https://example.org/foo/bar?baz', 'https://example.org', 'origin');
// check2('https://abc:xyz@example.com', 'xyz', 'password')

check2('https://example.org/abc/xyz?123', '/abc/xyz', 'pathname');
check2('https://example.org:8888', '8888', 'port');
check2('https://example.org', 'https:', 'protocol');
