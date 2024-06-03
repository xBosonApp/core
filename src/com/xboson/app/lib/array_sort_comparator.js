////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月11日 10:25
// 原始文件路径: xBoson/src/com/xboson/app/lib/array_sort_comparator.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

(function() {
//
// 配置 list 对象的 sort 函数, 该函数的底层使用 js 实现.
//
function array_sort_implement_js(p0) {
  var arr = this;
  var params = arguments;
  var names, ups;

  if (p0 == null || params.length == 0) {
    arr.sort();
  }
  else if (params.length % 2 != 0) {
    throw new Error("bad params length " + params.length);
  }
  else {
    names = [];
    ups = [];
    for (var i=0; i<params.length; i+=2) {
      names.push(params[i]);
      if (params[i+1] == '0') {
        ups.push({a:1, b:-1});
      } else {
        ups.push({a:-1, b:1});
      }
    }
    arr.sort(_sort_function);
  }


  function _sort_function(a, b) {
    for (var i=0; i<names.length; ++i) {
      var name = names[i];
      if (a[name] > b[name]) {
        return ups[i].a;
      } else if (a[name] < b[name]) {
        return ups[i].b;
      }
    }
    return 0;
  }
}

list.array_sort_implement_js = array_sort_implement_js;

})();