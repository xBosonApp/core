////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年12月13日 20:43
// 原始文件路径: xBoson/src/com/xboson/app/lib/compatible-syntax.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////


//
// 代码修正器将对 java 的函数调用转换为对该函数的调用.
//
function __inner_call(_func_name, _obj) {
  var func_name = _func_name;
  if (!_obj) throw new Error("object is null: @object." + func_name);

  try {
    if (_obj.constructor == Array) {
      arguments[0] = list;
      return Function.call.apply(list[func_name], arguments);
    } else {
      arguments[0] = map;
      return Function.call.apply(map[func_name], arguments);
    }
  } catch(e) {
    throw new Error(
        "Can not invoke @object."+ func_name +"(...), " + e.message);
  }
}


//
// 虚拟属性转换规则, 属性对应函数调用
//
var ___virtual_transform_roles = {
  // 对象转换器
  obj : {
  },
  // 数组转换器
  arr : {
    size : function(arr) { return arr.length; },
  },
};
Object.freeze(___virtual_transform_roles);


//
// 代码修正器对虚拟属性转换为该函数的调用
//
function __virtual_attr(_obj, _attr_name) {
  if (!_obj) throw new Error("Object is null: object.~" + _attr_name);

  if (_obj.constructor == Array) {
    var func = ___virtual_transform_roles.arr[_attr_name];
    if (func) return func(_obj);
    throw new Error("Unsupport array.~" + _attr_name);
  } else {
    var func = ___virtual_transform_roles.obj[_attr_name];
    if (func) return func(_obj);
    throw new Error("Unsupport object.~" + _attr_name);
  }
}


//
// 创建含有 {key, value} 属性的字符串对象
//
function __createKVString(key, value) {
  var ret;
  if (typeof value == 'string') {
    ret = new String(value);
  } else if (value !== null && value !== undefined) {
    ret = value;
  } else {
    return value;
  }

  try {
    if (! ret.key) {
      Object.defineProperty(ret, 'key', {
        enumerable  : false,
        writable    : false,
        configurable: true,
        value       : key,
      });
    }

    if (! ret.value) {
      Object.defineProperty(ret, 'value', {
        enumerable  : false,
        writable    : false,
        configurable: true,
        value       : value,
      });
    }
  } catch(e) {
    var sys = moduleHandleContext.get("sys");
    sys.printValue("WARN: {for} Key Value not bind; "+ e.message);
    return value;
  }
  return ret;
}
