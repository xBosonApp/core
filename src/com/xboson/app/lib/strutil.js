////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年12月14日 13:34
// 原始文件路径: xBoson/src/com/xboson/app/lib/strutil.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////


//
// 为兼容原脚本而设计
//
var strutil = {
};


(function(strutil) {

strutil.startWith   = _startWith;
strutil.endWith     = _endWith;
strutil.length      = _length;
strutil.subString   = _subString;
strutil.subStringTo = _subStringTo;
strutil.split       = _split;
strutil.contain     = _contain;
strutil.toUpperCase = _toUpperCase;
strutil.toLowerCase = _toLowerCase;
strutil.replace     = _replace;
strutil.format      = _format;
strutil.trim        = _trim;
strutil.formatDate  = _formatDate;
strutil.index       = _index;
strutil.lastIndex   = _lastIndex;
Object.freeze(strutil);

function _startWith(a, b) {
  return a.startWith(b);
}

function _endWith(a, b) {
  return a.endWith(b);
}

function _length(a) {
  return a.length;
}

function _subString(a, b, c) {
  return a.substring(b);
}

function _subStringTo(a, b, c) {
  return a.substring(b, c);
}

function _split(a, b) {
  return a.split(b);
}

function _contain(a, b) {
  return a.indexOf(b) >= 0;
}

function _toUpperCase(a) {
  return a.toUpperCase();
}

function _toLowerCase(a) {
  return a.toLowerCase();
}

function _replace(a, b, c) {
  return a.replace(b, c);
}

function _format(arguments) {
  throw new Error("unsupport strutil.format()");
}

function _trim(a) {
  return a.trim();
}

function _formatDate(a, b) {
  throw new Error("unsupport strutil.formatDate()");
}

function _index(a, b) {
  return a.indexOf(b);
}

function _lastIndex(a, b) {
  return a.lastIndexOf(b);
}

})(strutil);