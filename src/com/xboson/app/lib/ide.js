////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年12月14日 13:34
// 原始文件路径: xBoson/src/com/xboson/app/lib/ide.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

//
// 原脚本 ide 模块
//
var ide = {};


(function(ide) {

ide.searchApiContent = _searchApiContent;
ide.encodeApiScript = _encodeApiScript;
ide.decodeApiScript = _decodeApiScript;

Object.freeze(ide);


//
// 在 api 列表中搜索关键字, 并返回新的数组
//
function _searchApiContent(keyword, list, caseSensitive) {
  var ret = [];
  var se = moduleHandleContext.get('se');

  if (!caseSensitive) {
    keyword = keyword.toLowerCase();
  }

  for (var i=0; i<list.length; ++i) {
    var _api = list[i];

    if (_api && _api.content && _api.content.length > 0) {
      var content = se.decodeApiScript(_api.content);

      if (!caseSensitive) {
        content = content.toLowerCase();
      }
      if (content.indexOf(keyword) >= 0) {
        ret.push(_api);
      }
    }
  }

  if (ret.length > 0) {
    return ret;
  }
  return null;
}


function _encodeApiScript(code) {
  var se = moduleHandleContext.get('se');
  return se.encodeApiScript(code);
}


function _decodeApiScript(code) {
  var se = moduleHandleContext.get('se');
  return se.decodeApiScript(code);
}

})(ide);