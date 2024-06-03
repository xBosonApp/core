module.exports = {
  warpObject   : warpObject,
  warpFunction : warpFunction,
};
Object.freeze(module.exports);


//
// obj 应该是 Java 对象并继承了 com.xboson.script.JSObject
//
function warpObject(obj, target) {
  if (typeof obj.getFunctionNames != 'function')
    throw new Error("cannot find getFunctionNames in obj");

  var names = obj.getFunctionNames();
  for (var i=0; i<names.length; ++i) {
    var name = names[i];
    var fn = obj[name];

    if (!fn) return;
    if (typeof fn != 'function') return;

    target[name] = warpFunction(obj, name);
  }
  Object.freeze(target);
}


//
// java 对象不支持将函数与对象分离后再用 apply/call 调用
// 所以使用这样的方法, fn 函数名.
// 绑定的好处是可以将方法与对象分离了.
//
function warpFunction(obj, fn) {
  return function(a, b, c, d, e) {
    switch (arguments.length) {
      case 0:
        return obj[fn]();
      case 1:
        return obj[fn](a);
      case 2:
        return obj[fn](a, b);
      case 3:
        return obj[fn](a, b, c);
      case 4:
        return obj[fn](a, b, c, d);
      case 5:
        return obj[fn](a, b, c, d, e);
    }
    throw new Error("bad arguments length " + arguments.length);
  }
}