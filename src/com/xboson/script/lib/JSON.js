////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月10日 11:58
// 原始文件路径: xBoson/src/com/xboson/script/lib/JSON.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////


module.exports = {
  warp : warp,
};
Object.freeze(module.exports);

//
// 包装原始 JSON 对象, 实现 java 对象的 json 化.
// stringify : 如果本机方法无法序列化, 则会在对象本身寻找 toJSON() 方法并调用
//
function warp(_json) {
  var ret = {
    parse     : _json.parse,
    stringify : stringify,
  };

  Object.freeze(ret);
  return ret;


  //
  // https://developer.mozilla.org/zh-CN/docs/Web/JavaScript
  //        /Reference/Global_Objects/JSON/stringify#toJSON_方法
  //
  function stringify(obj, a, b) {
    if (obj && typeof obj.toJSON == 'function') {
      return obj.toJSON();
    }
    return _json.stringify(obj, a, b);
  }
}
