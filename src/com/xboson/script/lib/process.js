////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2018年1月19日 20:02
// 原始文件路径: xBoson/src/com/xboson/script/lib/process.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var sys_module_provider;
var sys_process;
var Event = require("events");
var process = module.exports = new Event();


process.versions = {
};


//
// 初始化方法只能调用一次
//
process.init = function(_sys_module_provider) {
  delete process.init;
  sys_module_provider = _sys_module_provider;
  sys_process = process.binding('process');
  process.versions.engineVersion = sys_process.engineVersion();
  process.versions.languageVersion = sys_process.languageVersion();
};


process.lock = function(locker, cb) {
  if (!locker) {
    locker = sys_process;
  }
  return sys_process.lock(locker, cb);
};


process.hrtime = function(stime) {
  var ret = sys_process.hrtime();
  if (stime) {
    ret[0] -= stime[0];
    ret[1] -= stime[1];
  }
  return ret;
};


process.binding = function(n) {
  return sys_module_provider.getModule('sys/' + n).exports;
};
