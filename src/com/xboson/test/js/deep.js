////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月9日 17:52
// 原始文件路径: xBoson/src/com/xboson/test/js/deep.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

module.exports = {
  terr : terr,
  t2   : t2,
  t3   : t3,
  getGlobalA : getGlobalA,
};


function terr(i, msg) {
  if (--i <= 0) {
    throw new Error(msg);
  } else {
    terr(i, msg);
  }
}


function t2() {
  return crossval;
}


function t3(a) {
  if (global.a != a)
    throw new Error("bad global.a " + a);
}


function getGlobalA() {
  return global.a;
}