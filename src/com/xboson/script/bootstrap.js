////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月3日 11:55
// 原始文件路径: xBoson/src/com/xboson/script/bootstrap.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////
"use strict"
//
// 通用库/初始化脚本
//
this.global = {};
// process 中的占位属性在 process 初始化后将被替换.
this.process = { versions: {}, lock: function(lt, cb) {return cb()} };
this.Buffer;


(function(context) { // 引导代码

var sys_module_provider;
var pathlib;
var safe_context = {};
var nativeJSON   = JSON;
var MODULE_NAME  = '/node_modules';


// 删除所有全局危险对象, 并绑定到内部对象上.
[
  'exit',     'quit',
  'Java',     'JavaImporter',
  'Packages', 'eval',   'print',
  'loadWithNewGlobal',  'load',
  '$ENV',     '$EXEC',  '$ARG',
].forEach(function(name) {
  safe_context[name] = context[name];
  delete context[name];
});


// 禁止 Function 打印源代码
Function.prototype.toString = function() {
  return 'function ' + (this.name || 'anonymous') + '() { [native code] }'
};


readOnlyAttr(context, '__warp_main', __warp_main);
rwAttrOnClosed(context, 'javax.script.filename');

context.__set_sys_module_provider = __set_sys_module_provider;
context.__env_ready = __env_ready;
context.__boot_over = __boot_over;


function __env_ready() {
  process = sys_module_provider.getModule('process').exports;
  process.init(sys_module_provider);
  pathlib = sys_module_provider.getModule('path').exports;
  context.JSON = process.binding('json').warp(nativeJSON);
  process.lock('', function() {})
}


function __boot_over() {
  delete context.__set_sys_module_provider;
  delete context.__env_ready;
  delete context.__boot_over;
  context.Buffer = sys_module_provider.getModule('buffer').exports.Buffer;
  Object.freeze(context);
}


function __warp_main(fn) { // 主函数包装器
  var app;
  var eflag;
  var currmodule;
  var dirname;
  var fncontext = {};


  return function(module, _app) {
    if (!_app) throw new Error("app(vfs) not provide");
    _app.sendScriptEvent( _app.flag.SCRIPT_PREPARE, module);
    app = _app;
    eflag = _app.flag;
    module.exports = {};
    module.children = {};
    currmodule = module;

    var console = requireUnsync('console').create(module.filename);
    var dirname = get_dirname(module.filename);

    if (!module.paths) {
      module.paths = get_module_paths(dirname);
    }
    try {
      app.sendScriptEvent(eflag.SCRIPT_RUN, currmodule);
      return fn.call(fncontext, requireSync, module,
           dirname, module.filename, module.exports, console);
    } finally {
      app.sendScriptEvent(eflag.SCRIPT_OUT, currmodule);
    }
  }


  function get_dirname(filename) {
    if (dirname)
      return dirname;

    if (!filename)
      return '.';

    dirname = filename.split("/");
    if (dirname.length > 2) {
      dirname.pop();
      dirname = dirname.join('/');
    } else {
      dirname = '/';
    }
    return dirname;
  }


  function requireSync(path) {
    return process.lock(null, function() {
      return requireUnsync(path);
    });
  }


  //
  // 所有的缓存都在 java 上处理.
  //
  function requireUnsync(path) {
    var mod;
    app.sendScriptEvent(eflag.IN_REQUIRE, currmodule);
    //
    // 一定从文件加载器加载
    //
    if (path[0] == '/') {
      path = pathlib.normalize(get_dirname() + path);
      return app.run(path).exports;
    }

    if (path[0] == '.') {
      mod = app.getModule(path, currmodule);
      if (mod) return mod.exports;
    }
    else if (app.isCached(path)) {
      //
      // 系统加载过的模块将缓存在 app 中
      //
      mod = app.run(path);
      if (mod) return mod.exports;
    }

    mod = sys_module_provider.getModule(path, currmodule);

    if (!mod) {
      throw new Error("Cannot found Module: " + path);
    }
    // 不应该每次运行都绑定
    //mod.parent = currmodule;
    //currmodule.children[mod.id] = mod;
    app.sendScriptEvent(eflag.OUT_REQUIRE, currmodule);
    return mod.exports;
  }


  function get_module_paths(dirname) {
    if (!dirname) return [];

    var ret = [ dirname ];
    if (dirname != '/') {
      var sp = dirname.split('/');
      while (sp.length > 1) {
        ret.push(sp.join('/') + MODULE_NAME);
        sp.pop();
      }
    }

    ret.push(MODULE_NAME);
    return ret;
  }
}


function __set_sys_module_provider(_provider) {
  sys_module_provider = _provider;
}


//
// 让对象的属性冻结为固定值
//
function readOnlyAttr(obj, name, value, _get) {
  Object.defineProperty(obj, name, {
    enumerable  : false,
    writable    : false,
    configurable: false,
    value       : value,
  });
}


//
// 即使冻结对象, 这个属性也可以读写
//
function rwAttrOnClosed(obj, name) {
  var value;
  Object.defineProperty(obj, name, {
    enumerable  : false,
    configurable: false,
    get         : function() { return value },
    set         : function(v) { value = v },
  });
}


//
// ! http://mths.be/fromcodepoint v0.1.0 by @mathias
//
if (!String.fromCodePoint) {
  (function() {
    var defineProperty = (function() {
      // IE 8 only supports `Object.defineProperty` on DOM elements
      try {
        var object = {};
        var $defineProperty = Object.defineProperty;
        var result = $defineProperty(object, object, object) && $defineProperty;
      } catch(error) {}
      return result;
    }());
    var stringFromCharCode = String.fromCharCode;
    var floor = Math.floor;
    var fromCodePoint = function() {
      var MAX_SIZE = 0x4000;
      var codeUnits = [];
      var highSurrogate;
      var lowSurrogate;
      var index = -1;
      var length = arguments.length;
      if (!length) {
        return '';
      }
      var result = '';
      while (++index < length) {
        var codePoint = Number(arguments[index]);
        if (
          !isFinite(codePoint) ||       // `NaN`, `+Infinity`, or `-Infinity`
          codePoint < 0 ||              // not a valid Unicode code point
          codePoint > 0x10FFFF ||       // not a valid Unicode code point
          floor(codePoint) != codePoint // not an integer
        ) {
          throw RangeError('Invalid code point: ' + codePoint);
        }
        if (codePoint <= 0xFFFF) { // BMP code point
          codeUnits.push(codePoint);
        } else { // Astral code point; split in surrogate halves
          // http://mathiasbynens.be/notes/javascript-encoding#surrogate-formulae
          codePoint -= 0x10000;
          highSurrogate = (codePoint >> 10) + 0xD800;
          lowSurrogate = (codePoint % 0x400) + 0xDC00;
          codeUnits.push(highSurrogate, lowSurrogate);
        }
        if (index + 1 == length || codeUnits.length > MAX_SIZE) {
          result += stringFromCharCode.apply(null, codeUnits);
          codeUnits.length = 0;
        }
      }
      return result;
    };
    if (defineProperty) {
      defineProperty(String, 'fromCodePoint', {
        'value': fromCodePoint,
        'configurable': true,
        'writable': true
      });
    } else {
      String.fromCodePoint = fromCodePoint;
    }
  }());
}


})(this);