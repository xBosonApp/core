////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2018年1月27日 19:15
// 原始文件路径: xBoson/src/com/xboson/test/js/bug.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

try {
  if (1==1) {
    //
    // 8u?? : throw 'TypeError: Cannot call undefined'
    // 8u111: throw 'TypeError: f1 is not a function'
    //
    var r = f1();
    console.log(r, 'nashorn bug fixed !');

    //
    // nashorn 引擎不能调用 if 语句块中定义的函数, 除非该函数在调用前定义.
    //
    function f1() {
      return 'ok';
    }
  }
} catch(e) {
  var msg = e && e.message;

  if (msg.indexOf("call undefined") >= 0 ||
      msg.indexOf("not a function") >= 0) {
    console.warn("BUG: Cannot define function in IF statement");
  } else {
    throw e;
  }
}