////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月12日 19:37
// 原始文件路径: xBoson/src/com/xboson/test/js/check-uuid.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var assert = require('assert');
var uuid = require('uuid');
var v1 = uuid.v1;
var v4 = uuid.v4;

uuid.v1 = null;
delete uuid.v1;
assert(uuid.v1, "is freeze");

assert.eq(v1().length, 36);
assert.eq(v4().length, 36);

var id = uuid.v1obj();
var ds = uuid.ds(id);
var pds = uuid.parseDS(ds);
var z = uuid.zip(id);
var uz = uuid.unzip(z);

assert(pds.equals(id), "ds format");
assert(uz.equals(id), "zip uuid");

console.log("UUID : ", id, "[ version:", id.version(), "length:", id.toString().length(), "]");
console.log("DS   : ", ds, "[ length:", ds.length(), "]");
console.log("PDS  : ", pds);
console.log("ZIP  : ", z, "[ length:", z.length(), "]");
console.log("UNZIP: ", uz);