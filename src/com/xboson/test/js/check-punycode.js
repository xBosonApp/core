////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月10日 21:17
// 原始文件路径: xBoson/src/com/xboson/test/js/check-punycode.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var punycode = require('punycode');
var assert = require('assert');

assert.eq(punycode.decode('maana-pta'), 'mañana');
assert.eq(punycode.decode('--dqo34k'), '☃-⌘');

assert.eq(punycode.encode('mañana'), 'maana-pta');
assert.eq(punycode.encode('☃-⌘'), '--dqo34k');

assert.eq(punycode.toASCII('mañana.com'), 'xn--maana-pta.com');
assert.eq(punycode.toASCII('☃-⌘.com'), 'xn----dqo34k.com')
assert.eq(punycode.toASCII('example.com'), 'example.com');

assert.eq(punycode.toUnicode('xn--maana-pta.com'),'mañana.com');
assert.eq(punycode.toUnicode('xn----dqo34k.com'), '☃-⌘.com');
assert.eq(punycode.toUnicode('example.com'), 'example.com');

assert.deepEqual(punycode.ucs2.decode('abc'), [0x61, 0x62, 0x63], 'ucs2 decode 1');
// surrogate pair for U+1D306 tetragram for centre:
assert.deepEqual(punycode.ucs2.decode('\uD834\uDF06'), [0x1D306], 'ucs2 decode 2');

assert.eq(punycode.ucs2.encode([0x61, 0x62, 0x63]), 'abc', 'ucs2 encode 1');
assert.eq(punycode.ucs2.encode([0x1D306]), '\uD834\uDF06', 'ucs2 encode 2');

console.log('punycode.version =', punycode.version);