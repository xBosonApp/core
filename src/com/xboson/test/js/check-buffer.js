////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月10日 20:23
// 原始文件路径: xBoson/src/com/xboson/test/js/check-buffer.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var assert = require("assert");

assert.eq(require("buffer").Buffer, Buffer, "Buffer module init");

var buf1, buf2, buf3, buf4, buf5, buf6;

function abc(_buf) {
  for (var i = 0; i < 26; i++) {
    // 97 是 'a' 的十进制 ASCII 值
    _buf[i] = i + 97;
  }
}


//
// 测试 from
//
buf1 = Buffer.from([1,2,3,"x"]);
buf2 = Buffer.from(buf1);
assert(buf1.equals(buf2), 'equals function ' + buf1 + ' = ' + buf2);

buf1[0] = 0x99;
//console.debug('N1:', buf1, 'N2:', buf2);
assert.notEqual(buf1[0], buf2[0], '对象 from 复制');

buf1 = Buffer.from('ABC');
buf2 = Buffer.from('BCD');
buf3 = Buffer.from('ABCD');


//
// 测试 compare
//
assert.eq(0, buf1.compare(buf1), "比较 buf1 myself");
assert.eq(-1, buf1.compare(buf2), "比较 buf1, buf2");
assert.eq(-1, buf1.compare(buf3), "比较 buf1, buf3");
assert.eq(1, buf2.compare(buf1), "比较 buf2, buf1");
assert.eq(1, buf2.compare(buf3), "比较 buf2, buf3");


//
// 测试 equals
//
buf1 = Buffer.alloc(10);
assert(buf1.equals([0,0,0,0,0, 0,0,0,0,0]),
  "创建一个长度为 10、且用 0 填充的 Buffer。");

buf2 = Buffer.alloc(10, 1);
assert(buf2.equals([1,1,1,1,1, 1,1,1,1,1]),
  '创建一个长度为 10、且用 0x1 填充的 Buffer。');

buf3 = Buffer.allocUnsafe(10);
assert(buf3.equals([0,0,0,0,0, 0,0,0,0,0]),
  "服务端程序强制清零, 不会创建未初始化的 Buffer");

buf4 = Buffer.from([1, 2, 3]);
assert(buf4.equals([0x1, 0x2, 0x3]),
  "创建一个包含 [0x1, 0x2, 0x3] 的 Buffer。");

buf5 = Buffer.from('tést');
assert(buf5.equals([0x74, 0xc3, 0xa9, 0x73, 0x74]),
  "创建一个包含 UTF-8 字节 [0x74, 0xc3, 0xa9, 0x73, 0x74] 的 Buffer。");

buf6 = Buffer.from('tést', 'latin1');
assert(buf6.equals([0x74, 0xe9, 0x73, 0x74]),
  "创建一个包含 Latin-1 字节 [0x74, 0xe9, 0x73, 0x74] 的 Buffer。");


//
// 创建两个 Buffer 实例 buf1 与 buf2 ，
// 并拷贝 buf1 中第 16 个至第 19 个字节到 buf2 第 8 个字节起。
//
buf1 = Buffer.allocUnsafe(26);
buf2 = Buffer.allocUnsafe(26).fill('!');
abc(buf1);
buf1.copy(buf2, 8, 16, 20);

assert.equal(buf2.toString('ascii', 0, 25),
    '!!!!!!!!qrst!!!!!!!!!!!!!',
    "buf1: " + buf1.toString("ascii"));


//
// 创建一个 Buffer ，并拷贝同一 Buffer 中一个区域的数据到另一个重叠的区域。
//
var buf = Buffer.allocUnsafe(26);
abc(buf);
buf.copy(buf, 0, 4, 10);

assert.equal(buf.toString('ascii'),
  'efghijghijklmnopqrstuvwxyz', "hex:" + buf);


//
// js es5 不支持迭代器协议, 使用 java 迭代器语法
//
var i = 0;
var iterator = buf.entries();
while (iterator.hasNext()) {
  var o = iterator.next();
  assert.deepEqual(o, [i, buf[i]], 'entries index:' + i);
  ++i;
}

i = 0;
iterator = buf.keys();
while (iterator.hasNext()) {
  assert.eq(i++, iterator.next(), 'key index:' + i);
}


//
// 与 `arr` 共享内存
//
var arr = new Uint16Array(2);
arr[0] = 5000;
arr[1] = 4000;
//console.log(typeof arr.buffer, arr.buffer);

var buf = Buffer.from(arr.buffer);
assert(buf.equals([0x88, 0x13, 0xa0, 0x0f]), '输出: <Buffer 88 13 a0 0f>');
// 改变原始的 Uint16Array 也将改变 Buffer
arr[1] = 6000;
assert(buf.equals([0x88, 0x13, 0x70, 0x17]), '输出: <Buffer 88 13 70 17>');


//
// 测试 indexOf
//
buf = Buffer.from('this is a buffer');

assert.eq(buf.indexOf('this'), 0);
assert.eq(buf.indexOf('is'), 2);
assert.eq(buf.indexOf(Buffer.from('a buffer')), 8);
// (97 是 'a' 的十进制 ASCII 值)
assert.eq(buf.indexOf(97), 8);
assert.eq(buf.indexOf(Buffer.from('a buffer example')), -1);
assert.eq(buf.indexOf(Buffer.from('a buffer example').slice(0, 8)), 8);


//
// 测试 slice
//
buf1 = Buffer.alloc(26);
abc(buf1);
buf2 = buf1.slice(0, 3);

assert.eq(buf2.toString(
    'ascii', 0, buf2.length), 'abc', 'slice() not working');
buf1[0] = 33;
assert.eq(buf2.toString(
    'ascii', 0, buf2.length), '!bc', "修改 buf1, buf2 也将改变");


//
// 测试 base64 编码
//
var b64str = buf1.toString("base64");
buf2 = Buffer.from(b64str, 'base64');
assert(buf1.equals(buf2), "base64 code");


//
// 只读的属性
//
buf1.length = 8;
assert.eq(buf1.length, 26, "Buffer.length must readonly");


//
// toJSON
//
var bufstr = JSON.stringify(buf1);
assert(bufstr, "JSON.stringify return undefined");
assert.eq(bufstr, buf1.toJSON(), 'JSON.stringify not working');

var _data = JSON.parse(bufstr);
assert.eq(_data.type, "Buffer", "type attribute");
assert.eq(_data.data.length, buf1.length, 'length fail');
buf3 = Buffer.from(_data.data);
assert(buf3.equals(buf1), 'bad data from JSON');


//
// 将 buf 解析为一个无符号16/32/64位的整数数组，并且以字节顺序原地进行交换。
// 如果 buf.length 不是2/4/8的倍数，则抛出 RangeError 错误。
//
buf2 = Buffer.from([0x1, 0x2, 0x3, 4, 5, 6, 7, 8, 9]);

assert.throws(function() {
  buf2.swap16();
}, /RangeError.*16-bits/);

assert.throws(function() {
  buf2.swap32();
}, /RangeError.*32-bits/);

assert.throws(function() {
  buf2.swap64();
}, /RangeError.*64-bits/);

buf1 = Buffer.from([0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8]);
buf1.swap16();
assert(buf1.equals([2,1,4,3,6,5,8,7]), 'swap16()');

buf1 = Buffer.from([0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8]);
buf1.swap32();
assert(buf1.equals([4,3,2,1,8,7,6,5]), 'swap32()');

buf1 = Buffer.from([0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8]);
buf1.swap64();
assert(buf1.equals([8,7,6,5,4,3,2,1]), 'swap64()');


//
// readXxxBE/LE 系列
//
buf = Buffer.from([1, 2, 3, 4, 5, 6, 7, 8]);


// readDoubleLE / readDoubleBE
assert.eq(buf.readDoubleLE(0), 5.447603722011605e-270, 'readDoubleLE()');
assert.eq(buf.readDoubleBE(0), 8.20788039913184e-304, 'readDoubleBE()');

assert.throws(function() {
  console.log(buf.readDoubleLE(1));
}, /RangeError.*out/);


// readFloatBE
buf = Buffer.from([1, 2, 3, 4]);
assert.eq(buf.readFloatBE(0), 2.387939260590663e-38, 'readFloatBE()');
assert.eq(buf.readFloatLE(0), 1.539989614439558e-36, 'readFloatLE()');

assert.throws(function() {
  console.log(buf.readFloatLE(1));
}, /RangeError.*out/);


// readInt8
buf = Buffer.from([-1, 5]);
assert.eq(buf.readInt8(0), -1, "readInt8");
assert.eq(buf.readInt8(1), 5, "readInt8");

assert.throws(function() {
  console.log(buf.readInt8(2));
}, /RangeError/);


// readInt16BE / readInt16LE
buf = Buffer.from([0, 5]);
assert.eq(buf.readInt16BE(0), 5, "readInt16BE");
assert.eq(buf.readInt16LE(0), 1280, "readInt16LE");

assert.throws(function() {
  buf.readInt16LE(1)
}, /RangeError/);


// readInt32BE / readInt32LE
buf = Buffer.from([0, 0, 0, 5]);
assert.eq(buf.readInt32BE(0), 5, "readInt32BE");
assert.eq(buf.readInt32LE(0), 83886080, "readInt32LE");

assert.throws(function() {
  buf.readInt32LE(1);
}, /RangeError/);


// readIntBE(offset, byteLength)
// not implements


// readUInt8
buf = Buffer.from([1, -2]);
assert.eq(buf.readUInt8(0), 1);
assert.eq(buf.readUInt8(1), 254);

assert.throws(function() {
  buf.readUInt8(2)
}, /RangeError/);


// readUInt16BE / readUInt16LE
buf = Buffer.from([0x12, 0x34, 0x56]);

assert.eq(buf.readUInt16BE(0).toString(16), 1234);
assert.eq(buf.readUInt16LE(0).toString(16), 3412);
assert.eq(buf.readUInt16BE(1).toString(16), 3456);
assert.eq(buf.readUInt16LE(1).toString(16), 5634);

assert.throws(function() {
  buf.readUInt16LE(2).toString(16);
}, /RangeError/);


// readUInt32BE / readUInt32LE
buf = Buffer.from([0x12, 0x34, 0x56, 0x78]);

assert.eq(buf.readUInt32BE(0).toString(16), 12345678);
assert.eq(buf.readUInt32LE(0).toString(16), 78563412);

assert.throws(function() {
  console.log(buf.readUInt32LE(1).toString(16));
}, /RangeError/);


// readUIntBE(offset, byteLength
// not implements


//
// write(string...
//
buf = Buffer.allocUnsafe(256);

var len = buf.write('\u00bd + \u00bc = \u00be', 0);
assert.eq(len, 12, "write length");
assert.eq(buf.toString('utf8', 0, len), "½ + ¼ = ¾", "output string");


// writeDoubleBE / writeDoubleLE
buf = Buffer.allocUnsafe(8);

buf.writeDoubleBE(0xdeadbeefcafebabe, 0);
assert(buf.equals([0x43, 0xeb, 0xd5, 0xb7, 0xdd, 0xf9, 0x5f, 0xd7]));

buf.writeDoubleLE(0xdeadbeefcafebabe, 0);
assert(buf.equals([0xd7, 0x5f, 0xf9, 0xdd, 0xb7, 0xd5, 0xeb, 0x43]));


// writeFloatLE / writeFloatBE
buf = Buffer.allocUnsafe(4);

buf.writeFloatBE(0xcafebabe, 0);
assert(buf.equals([0x4f, 0x4a, 0xfe, 0xbb]));

buf.writeFloatLE(0xcafebabe, 0);
assert(buf.equals([0xbb, 0xfe, 0x4a, 0x4f]));


// writeInt8
buf = Buffer.allocUnsafe(2);

buf.writeInt8(2, 0);
buf.writeInt8(-2, 1);
assert(buf.equals([0x02, 0xFE]));


// writeInt16BE / writeInt16LE
buf = Buffer.allocUnsafe(4);

buf.writeInt16BE(0x0102, 0);
buf.writeInt16LE(0x0304, 2);
assert(buf.equals([ 0x1,0x2,0x4,0x3 ]));


// writeInt32BE / writeInt32LE
buf = Buffer.allocUnsafe(8);

buf.writeInt32BE(0x01020304, 0);
buf.writeInt32LE(0x05060708, 4);
assert(buf.equals([ 1,2,3,4,8,7,6,5 ]));


// writeIntBE(int value, int offset, int byteLength)
// writeIntLE(int value, int offset, int byteLength)
// not implement


// writeUInt8
buf = Buffer.allocUnsafe(4);

buf.writeUInt8(0x3, 0);
buf.writeUInt8(0x4, 1);
buf.writeUInt8(0x23, 2);
buf.writeUInt8(0x42, 3);
assert(buf.equals([3, 4, 0x23, 0x42]));


// writeUInt16BE / writeUInt16LE
buf = Buffer.allocUnsafe(4);

buf.writeUInt16BE(0xdead, 0);
buf.writeUInt16BE(0xbeef, 2);
assert(buf.equals([0xde, 0xad, 0xbe, 0xef]), 'writeUInt16BE');

buf.writeUInt16LE(0xdead, 0);
buf.writeUInt16LE(0xbeef, 2);
assert(buf.equals([0xad, 0xde, 0xef, 0xbe]), 'writeUInt16LE');


// writeUInt32BE / writeUInt32LE
buf = Buffer.allocUnsafe(4);

buf.writeUInt32BE(0xfeedface, 0);
assert(buf.equals([0xfe, 0xed, 0xfa, 0xce]));

buf.writeUInt32LE(0xfeedface, 0);
assert(buf.equals([0xce, 0xfa, 0xed, 0xfe]));


// writeUIntBE / writeUIntLE
// not implement

