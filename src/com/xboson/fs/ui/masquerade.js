////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2018年1月19日 14:24
// 原始文件路径: xBoson/src/com/xboson/fs/ui/masquerade.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// init
// 若需要同步 nodejs 代码, 直接将对应文件的内容复制到 __install 的实现函数中
///////////////////////////////////////////////////////////////////////////////
var Path            = require('path');
var helper          = require("helper");
var Event           = require("events");
var fileChangeEvent = new Event(); // 该对象没有被绑定事件, 文件修改消息也没有实现.
var console         = console.create("masquerade");
var __eventLoop     = helper.createEventLoop(process);
var __id            = 0;
var __last_modify   = 0;

var innerModule = {
};

function _require(name) {
  var im = innerModule[name];
  if (im) return im.exports;
  return require(name);
}

function createModule(name) {
  return {
    name : name,
    exports : {}
  };
}

function __install(module_name, func) {
  var module = createModule(module_name);
  func(module, _require);
  innerModule[module_name] = module;
}

function __alias(name, aname) {
  innerModule[aname] = innerModule[name];
}

function setInterval(fn) {
  __eventLoop.push(fn);
}

function setTimeout(fn) {
  __eventLoop.push(fn);
}

function setImmediate(fn) {
  __eventLoop.push(fn);
}

function runEventLoop() {
  __eventLoop.runUntilEmpty();
}

function updateGlobalTime(last) {
  __last_modify = Math.max(last, __last_modify) || Date.now();
}

function globalLastTime() {
  return __last_modify;
}

process.on('error', function(e) {
  console.error("Uncaught process message", e.stack);
});
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('configuration-lib', function(module, require) {

module.exports.load = function() {
  return {
    ui_ide : {
      host : "localhost",
      proxyPort : "80",
    }
  };
};

module.exports.wait_init = function(func) {
  func();
};

});
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('mime', function(module, require) {

module.exports.lookup = function(url) {
  return 'text/html';
};

});
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./conf.js', function(module, require) {

var clib = require('configuration-lib');
var root = { data : clib.load() };


clib.wait_init(function() {
  root.data = clib.load();
});


module.exports.wait = clib.wait_init;

module.exports.load = function() {
  return root.data;
};


//
// 切换 public/private 配置到另一个配置上
// 比如: 默认总是使用 masquerade 这个配置, 如果需要使用 mas2 这个配置
// 则调用这个方法 change('mas2')
// 必须在库启动之前调用
//
module.exports.change = function(conf_name) {
  clib.wait_init(function() {
    conf = root.data;
    conf.masquerade.public  = conf[conf_name].public;
    conf.masquerade.private = conf[conf_name].private;
  });
};

}); // ./conf.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// 针对 xBoson 做了修改
///////////////////////////
__install("./hfs.js", function(module, require) {

var Event = require("events");
var plib  = require('path');

if (Event.EventEmitter)
  Event = Event.EventEmitter;

module.exports = {
  watch   : watch,
  eachDir : eachDir,
};


//
// path -- 要监视的根目录
// rcb  -- Function(err, watcher)
//    watcher.on(EVENTNAME, Function(filename))
//
// Event:
//    addfile, adddir, changefile, changedir
//
//
function watch(fs, _path, rcb) {
  _path = Path.normalize(_path);
  console.debug("hfs.watch() >>>", _path);
  var watcher = new Event();

  fileChangeEvent.on('[*]', function(page, type) {
    if (page.startsWith(_path)) {
      console.debug("hfs.dir", type, ">>>", page, ":", _path);
      watcher.emit(type, page);
    }
  });

  rcb(null, watcher);
}

//
// 遍历目录,
// eachChild - 是否遍历子目录
// cb - 每个文件/目录的回调函数 function(path, state)
//
function eachDir(fs, dir, eachChild, cb) {
  if (eachChild) throw new Error("Unsupport 'eachChild'");
  // console.warn("eachDir()", fs, dir);
  fs.readDir(dir, cb);
}

}); // hfs.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./error.js', function(module, require) {

module.exports.SyntaxError = SyntaxError;


//
// 返回一个语法错误异常
//
function SyntaxError(_why, _line, _col, _filename) {

  var ret  = new Error(_why)
  ret.line = _line;
  ret.col  = _col;
  ret.name = 'SyntaxError';
  ret.file = _filename;
  ret.why  = _why;


  ret._rebuild_message = function() {
    var msg = [];
    msg.push('Syntax error');

    if (ret.file) {
      msg.push(' on "');
      msg.push(ret.file);
      msg.push('"');
    }

    if (ret.col>0 && ret.line>0) {
      msg.push(" at [");
      msg.push(ret.line);
      msg.push(",");
      msg.push(ret.col);
      msg.push("]");
    }

    msg.push(", ");
    msg.push(ret.why);
    ret.message = msg.join('');
  };


  var ptostring = ret.toString;

  ret.toString = function() {
    ret._rebuild_message();
    return ptostring.apply(ret);
  }

  ret._rebuild_message();
  return ret;
};

}); // error.js end
__alias('./error.js', "../error.js");
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./tool.js', function(module, require) {

var vm = require('vm');
var SyntaxError = require('./error.js').SyntaxError;


var SYSTEM_VAR = {};
var express_timeout = 1000;
//var __id = 1;

module.exports.string_buf          = string_buf;
module.exports.rendering           = rendering;
module.exports.expression_complier = expression_complier;
module.exports.isSystemVar         = isSystemVar;
module.exports.addSystemVar        = addSystemVar;
module.exports.forEachSystemVar    = forEachSystemVar;
module.exports.comment             = comment;
module.exports.nextId              = nextId;
module.exports.parseExp            = parseExp;


//
// Buffer 包装器, 实现可自动增长的 Buffer
//
function string_buf(len) {
  var grow_coefficient  = 0.75;
      len = len || 50;
  var buf = new Buffer(len);
  var p = 0;

  var ret = {
    write: function(s) {
      if (!s) return;
      var x = p + s.length;

      if (x >= len) {
        var nbuf = new Buffer(x + x * grow_coefficient);
        buf.copy(nbuf);
        len = nbuf.length;
        buf = nbuf;
      }

      buf.write(s, p);
      this.length = p = x;
    },

    toString: function(encoding, start, end) {
      if (!start) start = 0;
      if (!end) end = p;
      return buf.toString(encoding, start, end);
    },

    length: 0
  };

  return ret;
}


//
// 解析表达式 exp = 'a.b.c'
// must_easy_exp -- true 强制解析为简单表达式
// return context.a.b.c
//
// var ctx = { a: { b:'hello' } };
// var ec = expression_complier('a.b');
// ec.val(ctx) == 'hello'
// ctx 在必要时将被转换为 sandbox, 通过 vm.createContext(...)
//
// 简单表达式支持赋值
// ec.val(ctx, 10) == 10, b == 10
//
// new vm.Script(exp, opt); 的效率太低 相差10倍
// runInContext 比 runInNewContext 快 10 倍
//
function expression_complier(exp, must_easy_exp) {
  var ret   = {};
  var attrs = [];
  var adv   = {'[':1, ']':1, '(':1, ')':1, '+':1, '-':1, '*':1, '/':1, '=':1};

  if (parse()) {
    ret.val = function(context, _set_val) {
      var ret = context;
      var i = -1;

      while (++i < attrs.length-1) {
        ret = ret[attrs[i]];
      }

      if (i < attrs.length) {
        if (_set_val) ret[attrs[i]] = _set_val;
        ret = ret[attrs[i]];
      }

      if (i >=0) { return ret;  }
      else       { return null; }
    };

  } else {

    var script = vm.Script(exp, {filename: 'bird-express'});

    ret.val = function(context) {
      if (!vm.isContext(context)) {
        try {
          context = vm.createContext(context);
        } catch(e) {
          console.error('tool.expression_complier warn:', e.message);
          context = vm.createContext({});
        }
      }
      return script.runInContext(context, { timeout: express_timeout });
    };
  }

  return ret;

  // 不允许赋值
  function safe() {
    var i = 0, ch, j = 0;
    var name = [];

    while (i < exp.length) {
      if (exp[i] == '=') {
        if (exp[i+1] != '=') {
          ch = exp[i-1];
          if (ch != '>' && ch != '<') {
            throw SyntaxError(
              "expression not allow Assignment operator: `"
              + exp + '`');
          }
        } else {
          ++i;
        }
      }
      ++i;
    }
  }

  function parse() {
    var i = 0, ch, f = 1, sp = 0;
    var name = [];

    while (i < exp.length) {
      ch = exp[i];

      if (adv[ch] == 1 && (!must_easy_exp)) {
        safe();
        return false;
      }
      else if (ch == ' ' || ch == '\t' || ch == '\n') {
        if (f == 2) {
          ++sp;
        }
        // skip
      }
      else if (f == 1) {
        f = 2;
        continue;
      }
      else if (f == 2) {
        if (ch == '.') {
          f = 1;
          attrs.push(name.join(''));
          name = [];
          sp = 0;
        } else {
          if (sp) {
            throw SyntaxError("expression not allow: '" + exp + "'");
          }
          name.push(ch);
        }
      }
      ++i;
    }

    if (name.length > 0)
      attrs.push(name.join(''));
    return true;
  }
}


//
// 渲染多个渲染器, 到 Buffer 中
// context 是应用上下文
// rendering(Buffer, context, renderFn, renderFn ...)
//
function rendering(buffer, context) {
  var argv = arguments;
  var i = 2 -1;
  _r();

  function _r() {
    if (++i < argv.length) {
      argv[i](_r, buffer, context);
    } else {
      console.log('Rendering over.');
    }
  }
}


//
// context 中有一些变量是不允许用户操作的
// 此时, 返回 true
//
function isSystemVar(name) {
  return SYSTEM_VAR[name] != null;
}

function addSystemVar(name) {
  SYSTEM_VAR[name] = true;
}

function forEachSystemVar(every_name) {
  for (var name in SYSTEM_VAR) {
    every_name(name);
  }
}

//
// 输出一段 html 注释
//
function comment(buf, msg) {
  if (!buf.write) {
    throw new Error('First Argument must Buffer not ' + buf);
  }

  buf.write('<!--');
  if (arguments.length > 2) {
    for (var i = 1; i < arguments.length; ++i) {
      buf.write(arguments[i]);
    }
  } else {
    buf.write(msg);
  }

  buf.write('-->');
}


function nextId() {
  return ++__id;
}


//
// 解析一个字符串, 并解析器中的表达式, 返回一个对象
// 使用其中的 val(context) 方法取得表达式的结果
//
function parseExp(expstr) {
  var i = 0, ch, f = 1, cc = 0;
  var b = 0;
  var valarr = [];

  function str_render(a, b) {
    var _sub = expstr.substring(a, b);
    return {
      val: function() {
        return _sub;
      }
    };
  }

  while (i < expstr.length) {
    ch = expstr[i];

    if (f == 1) { // 普通字符阶段
      if (ch == '#') {
        f = 2;
        continue;
      }
    }
    else if (f == 2) { // 检测表达式开始
      if (ch == '#') {
        ++cc;
        if (cc == 3) {
          var strr = str_render(b, i-2);
          valarr.push(strr);
          f = 3;
          b = i+1;
        }
      }
      else {
        f = 1;
      }
    }
    else if (f == 3) { // 进入表达式
      if (ch == '#') {
        f = 4;
        var expr = expression_complier(expstr.substring(b, i));
        valarr.push(expr);
        continue;
      }
    }
    else if (f == 4) { // 退出表达式, 必须连续三个 #
      if (ch == '#') {
        --cc;
        if (cc == 0) {
          f = 1;
          b = i+1;
        }
      } else {
        throw SyntaxError("must ### to end express");
      }
    }

    ++i;
  }

  if (b < i) {
    var strr = str_render(b, expstr.length);
    valarr.push(strr);
  }

  // 返回的对象内容与 expression_complier() 返回的相同
  return {
    val : function(context) {
      var _ret = [],
          _len = valarr.length;

      for (var i = 0; i < _len; ++i) {
        var v = valarr[i].val(context);
        if (typeof v != 'string') {
          v = JSON.stringify(v);
        }
        _ret.push( v );
      }

      return _ret.join('');
    }
  };
}

}); // tool.js end
__alias('./tool.js', '../tool.js');
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./base.js', function(module, require) {

var path = require('path');

module.exports.html_builder = html_builder;

//
// 创建一个 html 生成器返回渲染器函数, 渲染器原型:
//
// function(next, buf, context)
//    next    -- 渲染器中必须调用 next() 通知下一个渲染器开始渲染, next 之后的代码无效
//    buf     -- 必须有 write 方法的对象, 可以通过 string_buf 创建, 最终输出目标
//    context -- 上下文对象, 由初始渲染器创建
//
// 所有参数都不能 null
//
function html_builder() {
  var ret = {
    html: html,
    and: and,
    tag: tag,
    txt: txt,
    saver: saver
  };
  return ret;

  //
  // 创建一个 html 模板, 并返回渲染器
  // body_render - 渲染器用于渲染 body 中的内容
  // head_render - 渲染器用于渲染 head 中的内容
  //
  function html(body_render, head_render) {

    function setHandleRender(next, buf, context) {
      buf.setHeader &&
          buf.setHeader("Content-Type", "text/html; charset=UTF-8");
      next();
    }

    var root =
      tag('html', and(
        tag('head', head_render),
        tag('body', body_render)
      ));

    return and(setHandleRender, root);
  }

  //
  // 把参数插入渲染器队列中,
  // 返回的渲染器会渲染所有队列中的渲染器
  // and(renderFn, renderFn ... renderFn)
  //
  function and() {
    var s = saver();
    for (var i=0; i < arguments.length;  ++i) {
      if (arguments[i]) s.add(arguments[i]);
    }
    return s;
  }

  //
  // 创建一个渲染器对列, 并允许继续通过 add(renderFn) 插入渲染器
  // 返回的渲染器会渲染队列中所有的渲染器
  //
  // @return {
  //  插入一个渲染器到队列中
  //  add : function(renderFn)
  // }
  //
  // for xBoson: 修改渲染器队列, 由异步变为同步
  //
  function saver() {
    var queue = [];

    var ret = function(next, buf, context) {
      var len = queue.length;
      var i = -1;
      nextfn();

      function nextfn() {
        if (++i < len) {
          //
          // 借助 setImmediate 退出函数堆栈
          //
          setImmediate(function() {
            return queue[i](nextfn, buf, context);
          });
        } else {
          return next();
        }
      }
    };

    // 插入渲染器
    ret.add = function(renderFn) {
      queue.push(renderFn);
    }
    return ret;
  }

  //
  // 渲染器之间没有形成调用链, 只是简单的循环
  //
  function easy_saver() {
    var queue = [];

    var ret = function(next, buf, context) {
      var i = -1; _r();

      // _r 充当渲染器的 next 参数
      function _r() {
        if (++i < queue.length-1) {
          queue[i](_r, buf, context);
        } else {
          queue[i](next, buf, context);
        }
      }
    };

    // 插入渲染器
    ret.add = function(renderFn) {
      queue.push(renderFn);
    }
    return ret;
  }

  //
  // 创建一个渲染器, 渲染原始字符串
  //
  function txt(s) {
    return function(next, buf, context) {
      buf.write(s);
      next();
    }
  }

  //
  // 创建一个标签, 返回标签的渲染器
  //
  // tag(name[, content][, attrs])
  //
  // name    -- String: 标签的名字
  // content -- Fuction: 标签体的渲染器
  // attrs   -- {}: 标签属性对象
  //
  function tag(name, a1, a2) {
    var attrs = a2;
    var content = (typeof a1 == 'function' ?  a1 : (attrs=a1, null));

    return function(next, buf, context) {
      buf.write('<');
      buf.write(name);
      for (var n in attrs) {
        buf.write(' ');
        buf.write(n);
        buf.write('="');
        buf.write(attrs[n]);
        buf.write('"');
      }

      if (name[name.length-1] == '/') {
        buf.write('>');
        next();
      } else {
        buf.write('>');
        if (content) content(_over, buf, context);
        else _over();
      }

      function _over() {
        buf.write('</');
        buf.write(name);
        buf.write('>');
        next();
      }
    };
  }
}

}); // base.js end
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// 针对 xBoson 做了修改
///////////////////////////
__install('./systag/tag-api.js', function(module, require) {

var tool = require('../tool.js');
var TIME_OUT = 15;

var copyHeaders = ['cookie', 'authorization'];

//
// <api url='http://url/?parm' to='api1ret' [type='json/txt'] [method='get/post'] [exp='true']/>
//     请求一个 API, method 指定方法 (默认get),
//     使用 type 指定的类型解析数据 (默认为 json)
//     并把结果保存在 to 指定的变量中
//
module.exports = function(taginfo, __, errorHandle) {

  if (!taginfo.selfend)
     throw new Error('api Tag must have no BODY');

  var url    = geta('url');
  var save   = geta('to');
  var tosjon = geta('type', 'json') == 'json';
  var isget  = geta('method', 'get') == 'get';
  var exp    = (taginfo.attr.exp == 'true');
  var tout   = parseInt(geta('timeout', TIME_OUT)) * 1000;
  var geturl;

  if (tool.isSystemVar(save)) {
    throw new Error('cannot modify system var "' + to + '"');
  }

  if (exp) {
    exp = tool.expression_complier(url);
    geturl = function(context) {
      return exp.val(context);
    };
  } else {
    geturl = function() {
      return url;
    };
  }


  return function(next, buf, context) {
    var header = {};
    for (var i=copyHeaders.length-1; i>=0; --i) {
      var h = context.getHeader( copyHeaders[i] );
      if (h) header[ copyHeaders[i] ] = h;
    }

    var _url = geturl(context);
    try {
      var retbody;
      if (isget) {
        retbody = helper.httpGet(_url, header);
      } else {
        retbody = helper.httpPost(_url, header, taginfo.attr['body']);
      }
      if (tosjon) {
        retbody = JSON.parse(retbody);
      }
      context[save] = retbody;
      next();
    } catch(err) {
      errorHandle(err);
    }
  };


  function geta(name, defaultValue) {
    var r = taginfo.attr[name] || defaultValue;
    if (r == null || r == '') {
      throw new Error('must have ' + name + ' attribute');
    }
    return r;
  }
};

}); // systag/tag-api.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// 针对 xBoson 做了修改
///////////////////////////
__install('./systag/tag-auth.js', function(module, require) {

var tool = require('../tool.js');
var clib = require('configuration-lib');
var TIME_OUT = 15 * 1000;

var copyHeaders = ['cookie', 'authorization'];

//
// <auth [to='auth'] />
//     获取页面权限数据，并把结果保存在 to 指定的变量中，默认变量名为 auth
//
module.exports = function(taginfo, __, errorHandle) {

  if (!taginfo.selfend)
     throw new Error('api Tag must have no BODY');

  var pageid = tool.expression_complier(geta('page'));
  var save   = geta('to', 'auth');
  var url;

  if (tool.isSystemVar(pageid) || tool.isSystemVar(save)) {
    throw new Error('cannot modify system var "' + to + '"');
  }


  return function(next, buf, context) {
    try {
      context[save] = helper.pageAccessInfo(pageid);
      next();
    } catch(err) {
      errorHandle(err);
    }
  };


  function geta(name, defaultValue) {
    var r = taginfo.attr[name] || defaultValue;
    if (r == null || r == '') {
      throw new Error('must have ' + name + ' attribute');
    }
    return r;
  }
};

}); // systag/tag-auth.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-create.js', function(module, require) {

var tool = require('../tool.js');

//
// <create to='varname' a-s='string' b-e='express' c-n='9' />
//     创建一个对象 Object, 放入上下文的 to 指定的变量中
//     参数的说明: 由 nnn-T 形成的参数列表 nnn 指定一个属性, T 指定数据类型
//     T 的有效类型: 's': 字符串, 'e':表达式, 'i':整数, 'f':浮点数
//     如果不指定类型, 默认是字符串; 如果 to 指定的变量以及存在则什么都不做
//
module.exports = function(taginfo) {

  if (!taginfo.selfend)
     throw new Error('must have no BODY');

  var to = taginfo.attr.to;
  if (to == null || to == '') {
    throw new Error('must have "to" attribute');
  }

  //
  // 设置对象属性的方法数组, 方法原型 function(target, context)
  // 渲染器调用每一个方法来设置 target 的属性
  //
  var setter = [];

  function pushFn(n, v, ex) {
    if (ex) {
      setter.push(function(obj, context) {
        obj[n] = ex.val(context);
      });
    } else {
      setter.push(function(obj) {
        obj[n] = v;
      });
    }
  }

  //
  // 在编译器完成类型的判别, 提升运行期效率
  //
  for (var attr in taginfo.attr) {
    if (attr == 'to') continue;

    var fi = attr.indexOf('-');
    var n  = attr, t;
    var v  = taginfo.attr[attr];

    if (fi>0 < fi<attr.length-1) {
      n = attr.substring(0, fi);
      t = attr.substr(fi + 1);
    }

    switch(t) {
      case 'e':
        var ex = tool.expression_complier(v);
        pushFn(n, null, ex);
        break;

      case 'f':
        pushFn(n, parseFloat(v));
        break;

      case 'i':
        pushFn(n, parseInt(v));
        break;

      case 's':
      default:
        pushFn(n, v);
        break;
    }
  }

  return function(next, buf, context, tag_over) {
    if (context[to]) {
      tool.comment(buf, 'create Object but exists `', to, '`');
      //throw new Error();
      return next();
    }

    var obj = context[to] = {};

    setter.forEach(function(_set) {
      _set(obj, context);
    });

    next();
  }
};

}); // systag/tag-create.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-del.js', function(module, require) {

var tool = require('../tool.js');


module.exports = function(taginfo) {

  if (!taginfo.selfend)
      throw new Error('must have no BODY');

  var to = taginfo.attr['to'];

  if (!to)
      throw Error("must have 'to' attribute");


  return function(next, buf, context, tag_over) {
    if (!tool.isSystemVar(to)) {
      if (context[to]) {
        delete context[to];
      } else {
        tool.comment(buf, "delete var `", to, '` but not exists');
      }
    } else {
      tool.comment(buf, "cannot delete system var `", to, '`');
    }
    next();
  };
};

}); // systag/tag-del.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-else.js', function(module, require) {

var tool = require('../tool.js');
var IF_ELSE_KEY = '__if_else_var';

//
// <else></else>
//
// 于 if 标签配对使用
//
module.exports = function(taginfo) {

	if (taginfo.selfend)
		 throw new Error('must have BODY');


	return function(next, buf, context, tag_over) {
    var disable = context[IF_ELSE_KEY];
    if (disable || disable == undefined) {
      context.tag_scope.controler.disable_sub();
    }
    delete context[IF_ELSE_KEY];
    next();
  };
};


}); // systag/tag-else.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-if.js', function(module, require) {

var tool = require('../tool.js');
var IF_ELSE_KEY = '__if_else_var';

tool.addSystemVar(IF_ELSE_KEY);

//
// <if var="c" [not='true']></if>
//
// not==true 会取反结果
// var 未定义会抛出异常
//
module.exports = function(taginfo) {

	if (taginfo.selfend)
		 throw new Error('must have BODY');

	var v = taginfo.attr['var'];
	var n = taginfo.attr['not'];

	if (!v)
		throw Error("must 'var' attribute");

	var ex = tool.expression_complier(v);
	n = Boolean(n);


	return function(next, buf, context, tag_over) {
		var _val = ex.val(context);

		if (isNaN(_val)) {
			if (_val == 'false') _val = false;
		} else {
			_val = Number(_val);
		}

		var disable = Boolean(_val) == n;

		if (disable) {
			context.tag_scope.controler.disable_sub();
			context[IF_ELSE_KEY] = !disable;
		} else {
			delete context[IF_ELSE_KEY];
		}

		tag_over(function() {
			context[IF_ELSE_KEY] = !disable;
		});
    next();
  };
};

}); // systag/tag-if.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-for.js', function(module, require) {

var tool = require('../tool.js');
var util = require('util');

var FOR_DEP = '__foR_Dep_save';
tool.addSystemVar(FOR_DEP);

//
// <for from="array" save='b'  index='i'> ###b### ###i###</for>
// <for from="object" save='c' index='i'> ###c### ###i###</for>
//     循环渲染 标签体
//     每次循环的变量都保存在 save 指定的变量中, 之后可以通过表达式取出
//     每次循环的索引保存在 index 指定的变量中
//
// 嵌套有问题!!!!
//
module.exports = function(taginfo) {

  if (taginfo.selfend) throw new Error('must have BODY');

  var from    = geta('from');
  var from_ex = tool.expression_complier(from);
  var save    = geta('save');
  var index   = taginfo.attr['index'];


  return function(next, buf, context, tag_over) {
    // console.log('for in');

    function notNext() {
      scope.controler.disable_sub();
      return next();
    }

    var obj = from_ex.val(context);
    var scope = context.tag_scope;
    // 如果取出 from 的数据是空, 则不渲染子标签
    if (!obj) return notNext();

    var iter = newIterator(obj);
    // 如果参数无法循环也不会渲染子标签
    if (!iter) return notNext();

    var out_count = 0;

    tag_over(function() {
      // console.log('for out', out_count, iter.test())
      //
      // if 中的标签体渲染完成, 如果还需要循环
      // 则终止当前渲染, 并重新调用标签体
      //
      if (iter.has()) {
        repeatRenderSub();
        return "stop";
      } else {
        // 嵌套循环, 这里会退出多次防止这种情况
        if (out_count++ > 0) {
          return 'stop';
        }
        // scope.controler.enable_sub();
      }
    });

    repeatRenderSub();

    function repeatRenderSub() {
      if (iter.has()) {
        var loopvar = iter.next();
        context[save] = loopvar;

        if (index) {
          context[index] = iter.index();
        }
        next();
      } else {
        notNext();
      }
    }
  };

  function newIterator(obj) {
    if (typeof obj == 'object') {
      if (util.isArray(obj)) {
        return newArrayIterator(obj);
      } else {
        return newObjectIterator(obj);
      }
    }
    return null;
  }

  function newObjectIterator(obj) {
    var arr = [];
    var i = -1;

    for (var n in obj) {
      arr.push(n);
    }

    return {
      // 迭代器调用顺序, has > next > index,
      // next 会跳转到下一个位置
      has: function() {
        return i+1 < arr.length;
      },
      next: function() {
        return obj[arr[++i]];
      },
      index: function() {
        return arr[i];
      },
      test: function() {
        return arr;
      }
    }
  }

  function newArrayIterator(arr) {
    var i = -1;

    return {
      has: function() {
        return i+1 < arr.length;
      },
      next: function() {
        return arr[++i];
      },
      index: function() {
        return i;
      },
      test: function() {
        return arr;
      }
    };
  }

  function geta(name, defaultValue) {
    var r = taginfo.attr[name] || defaultValue;
    if (r == null || r == '') {
      throw new Error('must have ' + name + ' attribute');
    }
    return r;
  }
};

}); // systag/tag-for.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-include.js', function(module, require) {

var tool = require('../tool.js');
var path = require('path');

//
// <include [private="false"] src="/page/a.html"/>
//
//  包含一个页面, 默认从 public 目录, 如果 private='true' 则从
//  private/include 目录读取
//  不能有标签体
//
module.exports = function(taginfo, userTagFactory, errorHandle, fname) {

  if (!taginfo.selfend)
     throw new Error('must no BODY');
  var exp = (taginfo.attr.exp == 'true');

  var src = taginfo.attr['src'];
  if (!src)
    throw Error("must have 'src' attribute");

  var private = (taginfo.attr['private'] == 'true');
  var dir = path.dirname(fname);
  var getsrc;
  var rechange_src;

  function fixsrc(_src, fixdir) {
    var tsrc = path.normalize(_src);
    var tfix = path.normalize(fixdir);
    if (tsrc.indexOf(tfix) >= 0) {
      _src = tsrc.substr(tfix.length);
    }
    return _src;
  }

  if (exp) {
    exp = tool.expression_complier(src, true);
    getsrc = function(context) {
      var target = exp.val(context);
      if (private) {
        return target;
      } else {
        target = path.join(dir, target);
        return fixsrc(target, context.publicdir);
      }
    };
  } else {
    // publicdir 只在　context 中提供, 所以要等到渲染时修正路径
    rechange_src = (!private) && (!path.isAbsolute(src));
    getsrc = function(context) {
      if (rechange_src) {
        src = path.join(dir, src);
        src = fixsrc(src, context.publicdir);
        rechange_src = false;
      }
      return src;
    };
  }


  return function(next, buf, context) {
    context.filepool.getRenderFromType('/include',
        getsrc(context), private, userTagFactory, errorHandle, when_success);

    function when_success(includeRender) {
      includeRender(next, buf, context);
    }
  };
};

}); // systag/tag-include.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-never.js', function(module, require) {

//
// <never/>
//
module.exports = function(taginfo) {

  return function(next, buf, context, tag_over) {
    context.tag_scope.controler.disable_sub();
    next();
  };
};

}); // systag/tag-never.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-pit.js', function(module, require) {

var tool = require('../tool.js');
var SAVE_KEY = '_-_PIT0_BODY';
tool.addSystemVar(SAVE_KEY);

//
// <pit/>
//
module.exports = function(taginfo, userTagFactory, errorHandle, filename) {

  if (!taginfo.selfend)
       throw new Error('must not have BODY');

  return function(next, buf, context, tag_over) {
    if (context[SAVE_KEY]) {
      context[SAVE_KEY](next);
    } else {
      next();
    }
  };
};

}); // systag/tag-pit.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-script.js', function(module, require) {

var SyntaxError = require('../error.js').SyntaxError;
var tool        = require('../tool.js');
var log         = console;


// 代码必须在 scriptTimeout 结束前返回
var scriptTimeout = 15 * 1000;

var from_context_argv = [
  'console', 'log', 'setTimeout', 'setInterval',
];

// 要与 callback_argv 一致
var from_callback_argv = [
  'module', 'end', 'write', 'query', 'session',
  'require', 'val'
];


//
// 运行一个脚本与并缓存
//
function do_script(script, next, buf, context, errorHandle) {

  function nul() { /* do nothing */ }

  function end() {
    // 防止重复调用
    if (next) {
      next();
      next = null;
    } else {
      throw new Error("cannot call end() repeat");
    }
  }

  function write(o) {
    if (!next)
        throw new Error('cannot write data after end()');

    if (Buffer.isBuffer(o) || typeof o == 'string') {
      buf.write(o);
    }
    else {
      buf.write(JSON.stringify(o));
    }
  }

  function _require(module_name, cb) {
    // 子脚本改变要影响父脚本
    var file = context.filepool.getPrivatePath('/script', module_name);
    context.filepool.getScript(
        null, null, file, 0, from_context_argv, from_callback_argv, _script);

    function _script(err, ref_script) {
      if (err) return cb(err);

      if (!ref_script.module.parent) {
        ref_script.module.parent = script.module.id;
        script.module.children.push(ref_script.module.id);
      }

      cb(null, do_script(ref_script, nul, buf, context, errorHandle));
    }
  };

  function val(n, v) {
    var ret = context[n];
    if (v) {
      if (tool.isSystemVar(n))
        throw new Error("cannot modify system var:" + n);
      context[n] = v;
    }
    return ret;
  }

  // --------------------------------------------- End

  try {
    var _module = script.module || { exports: {} };
    var sandbox = context.getVmContext();
    var fn      = script.runInContext(sandbox, { timeout: scriptTimeout });

    // 要与 from_callback_argv 一致
    var callback_argv = [
      _module, nul, write, context.query, context.session,
      _require, val
    ];

    if (from_callback_argv.length != callback_argv.length)
        throw new Error('from_callback_argv NOT EQ callback_argv');

    try {
      fn.apply(_module, callback_argv);
    } finally {
      end();
    }
    return _module.exports;

  } catch(err) {
    if (next) {
      errorHandle(err);
      next = null;
    } else {
      throw err;
    }
  }
}


//
// <script src='/script1.js' [runat='server/client']/>
//
//     如果 runat==server (默认是client)
//     则运行一个服务端脚本, 相对于 /private/script 目录
//     脚本运行在沙箱中, 能力受限
//
module.exports = function(taginfo, userTagFactory, errorHandle, filename) {

  var atserver = (taginfo.attr.runat == 'server');

  if (atserver) {
    // var id  = taginfo.identify;
    var src = taginfo.attr.src;

    //
    // BODY 和 src 属性不能共存
    //
    if (taginfo.selfend) {
      if (src == null)
        throw SyntaxError('if no Body must have `src` attribute');

      var checkpath = false;

      //
      // 服务器脚本, 从 src 指定的文件中取出
      //
      return function(next, buf, context) {
        context.tag_scope.controler.disable_sub();
        if (!checkpath) {
          checkpath = true;
          src = context.filepool.getPrivatePath('/script', src);
        }

        context.filepool.getScript( null, null, src, context.tag_scope.line,
            from_context_argv, from_callback_argv, _script);

        function _script(err, script) {
          if (err) return errorHandle(err);
          do_script(script, next, buf, context, errorHandle);
        }
      }

    } else { // taginfo.selfend == false
      if (src != null)
        throw SyntaxError('if has `src` attribute must have no Body');

      if (taginfo.body.length != 1)
        throw Error('Body not has or overage ' + taginfo.body.length);

      //
      // 服务器脚本, 从当前页面的 BODY 中取出
      //
      return function(next, buf, context, tag_over) {
        context.tag_scope.controler.disable_sub();

        var code = context.tag_scope.body[0];
        var id   = taginfo.identify;

        context.filepool.getScript(code, id, filename, context.tag_scope.line,
            from_context_argv, from_callback_argv, _script);

        function _script(err, script) {
          if (err) return errorHandle(err);
          do_script(script, next, buf, context, errorHandle);
        }
      };
    }

  } else { // runat != server

    var attr_exp = tool.parseExp(taginfo.attr_str);

    //
    // 浏览器脚本
    //
    return function(next, buf, context, tag_over) {
      buf.write('<script ');
      buf.write( attr_exp.val(context) );
      buf.write('>');

      if (tag_over) {
        tag_over(function() {
          buf.write('</script>');
        });
      } else {
        buf.write('</script>');
      }

      next();
    };

  } // End else
};

}); // systag/tag-script.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-set.js', function(module, require) {
var tool = require('../tool.js');

//
// <set to='varname' attr='attrname' value='abc' [exp='false']/>
//     设置 to 指定的变量的 attr 属性值为 value
//     如果 exp==true (默认false), 则从上下文中取数据
//     修改系统变量会抛出异常
//
module.exports = function(taginfo) {

  if (!taginfo.selfend)
     throw new Error('must have no BODY');

  var to   = geta('to');
  var val  = geta('value');
  var attr = taginfo.attr['attr'];
  var exp  = (taginfo.attr.exp == 'true');
  var getval = null;

  if (exp) {
    exp = tool.expression_complier(val);
    getval = function(context) {
      return exp.val(context);
    };
  } else {
    getval = function() {
      return val;
    };
  }

  if (tool.isSystemVar(to)) {
    throw new Error('cannot modify system var "' + to + '"');
  }

  return function(next, buf, context, tag_over) {
    // console.log('set', to, attr, val)
    if (attr) {
      if (context[to]) {
        context[to][attr] = getval(context);
      } else {
        tool.comment(buf, "set attr, but object not exists: '", to, "'");
      }
    } else {
      context[to] = getval(context);
    }
    next();
  }

  function geta(name) {
    var r = taginfo.attr[name];
    if (r == null || r == '') {
      throw new Error('must have ' + name + ' attribute');
    }
    return r;
  }
};

}); // systag/tag-set.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-slice.js', function(module, require) {

var tool = require('../tool.js');
var SLICE_KEY = '__SLicE_save';

tool.addSystemVar(SLICE_KEY);


//
// `<slice ref='name' attr1='' attr2='' noerror='1'/>`
// `<slice define='name' noerror='1'>BODY</slice>`
//     创建一个切片, 如果有 ref 属性, 则不能有标签体, 用于引用一个切片到当前位置
//     并且 扩展属性都会传入上下文供给 <slice define/> 的 body 中使用
//     如果有 define 属性, 则必须有标签体, 切片只有在被引用的时候才会渲染
//     如果定义 noerror 则不会因为错误抛出异常, 否则不要定义
//
module.exports = function(taginfo) {

  var to    = taginfo.attr['define'];
  var from  = taginfo.attr['ref'];
  var noerr = taginfo.attr['noerror'];
	var exp   = (taginfo.attr.exp == 'true');

  if (isNull(to) && isNull(from)) {
    throw new Error('must have "define" or "from" attribute');

  } else if (!isNull(from)) {
    if (taginfo.selfend == false) {
      throw new Error('"ref" attribute must not have BODY');
    }

  } else if (!isNull(to)) {
    if (taginfo.selfend == true) {
      throw new Error('"define" attribute must have BODY');
    }
  } else {
    throw new Error('"define" and "ref" attribute not together');
  }

  for (var n in taginfo.attr) {
    if (tool.isSystemVar(n)) {
      throw new Error('cannot modify system var "' + n + '"');
    }
  }


	if (exp) {
		exp = tool.expression_complier(from || to);
	}


  if (from) { // IF (ref)

    return function(next, buf, context, tag_over) {
			if (exp) from = exp.val(context);

      if (context[SLICE_KEY] && context[SLICE_KEY][from]) {
        var renderNext = context[SLICE_KEY][from].render;
        sendAttrToTarget();
        renderNext(next);
      } else if (noerr) {
        next();
      } else {
        throw new Error('Ref slice but not exists: ' + from);
      }

      // 传递参数给 define 使用
      function sendAttrToTarget() {
        for (var n in taginfo.attr) {
          if (n == 'define' || n == 'ref') continue;
          if (context[n]) {
            tool.comment(buf, 'var has exists will overlay:', n);
          }
          context[n] = taginfo.attr[n];
        }
      }
    };

  } else { // IF (define)

    return function(next, buf, context, tag_over) {
			if (exp) to = exp.val(context);

      var scope = context.tag_scope;
      // to 在原位置不会进行渲染
      scope.controler.disable_sub();
      // console.log('slice define begin', to)

      if (!context[SLICE_KEY]) {
           context[SLICE_KEY] = {};
      } else if (context[SLICE_KEY][to]) {

        // for 循环中会重复渲染, 防止抛出错误
        if (context[SLICE_KEY][to].identify === taginfo.identify || noerr) {

          // 即使重复的直接调用, 也不会渲染
          scope.controler.disable_sub();
          return next();
        } else {
          throw new Error('create slice but exists: ' + to);
        }
      }

      // 保存引用这个切片的切片的 next
      var _pnext = [];
      var out_count = 0;

      // 导出函数给 from 用
      context[SLICE_KEY][to] = {
        render: renderNext,
        identify: taginfo.identify,
      };

      tag_over(function() {
        // fixbug. 在标签结束后又一次渲染!
        scope.controler.disable_sub();

        // _pnext 有效说明是通过 renderNext 调用的, 所以要结束在标签的结束
        if (_pnext.length > 0) {
          // console.log('slice over', to)
          _pnext.pop()();
          return 'stop';
        }
        // else {
        //   console.log('slice define over', to);
        // }
      });

      // 完成所有初始化才能返回
      next();

      function renderNext(___pnext) {
        // console.log('slice begin', to);
        scope.controler.enable_sub();
        _pnext.push(___pnext);
        next();
      }
    };

  } // End else

  function isNull(s) {
    return s == null || s == '';
  }
};

}); // systag/tag-slice.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-stop.js', function(module, require) {

var tool = require('../tool.js');

//
// <stop></stop>
//
module.exports = function(taginfo) {

  if (!taginfo.selfend)
       throw new Error('must not have BODY');

  return function(next, buf, context, tag_over) {
    context.__end();
  };
};

}); // systag/tag-stop.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-wait.js', function(module, require) {

var tool = require('../tool.js');

//
// ....................................
// time 属性指定等待时间, ms,
// 默认 100
//
module.exports = function(taginfo) {

	var time = taginfo.attr.time || 300;

  return function(next, buf, context, tag_over) {
    console.log("Test tag | being", taginfo.identify, taginfo.attr);

    if (tag_over) {
      tag_over(function() {
        console.log("Test tag | ending", taginfo.identify);
      });
    }

    setTimeout(next, time);
  };
};

}); // systag/tag-wait.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./systag/tag-once.js', function(module, require) {

var tool = require('../tool.js');

//
// <once id=''></once>
//
module.exports = function(taginfo) {

  if (taginfo.selfend)
       throw new Error('must have BODY');

  var KEY = '--_onee_save';
  var id = taginfo.attr.id;
  if (!id) throw new Error("Must have 'id' attribute");

  return function(next, buf, context, tag_over) {
    var save = context.global[KEY];
    if (!save) {
      save = context.global[KEY] = {};
    }

    if (! save[id]) {
      save[id] = true;
    } else {
      context.tag_scope.controler.disable_sub();
    }
    next();
  };
};

}); // systag/tag-once.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./install-systag.js', function(module, require) {

//
// 加载系统标签到 tagFactory 中
//
module.exports = function(tagFactory) {

	load('if');
	load('else');
	load('for');
	load('include');
	load('slice');
	load('create');
	load('script');
	load('wait');

	// 会改动系统变量, 放在最后做检查
	load('set');
	load('del');
	load('api');
	load('auth');
	load('pit');
  load('never');
	load('stop');
	load('once');

	// 迁移到单独的项目中
	// load('sql');

	//
	// 初始化失败立即停止, 不要尝试 catch 异常
	//
	function load(name) {
		if (tagFactory[name])
			throw new Error("系统标签渲染器安装失败, 标签已经存在:" + name);

		tagFactory[name] = require('./systag/tag-' + name + '.js');
	}
};

}) // install-systag.js end
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./install-def-tag.js', function(module, require) {

var tool  = require('./tool.js');
var path  = require('path');
var log   = console;

var SAVE_KEY = '_-_PIT0_BODY';

//
// 加载用户 HTML 标签到 tagFactory 中
// EXT - 带有 . 开始的扩展名
//
module.exports = function(pool, tagFactory, EXT, fs) {
  var root = pool.getPrivatePath('/tag', '/');
  var hfs  = require('./hfs.js');

  findfile(root);

  hfs.watch(fs, root, function(err, watcher) {
    if (!err) {
      watcher.on('addfile', function(f) {
        var ns = path.dirname( f.substr(root.length) ).replace(/\\|\//g, '-');
        createTag(f, ns);
      });

      watcher.on('removefile', function(f) {
        var tagname = path.basename(f, EXT);
        log.log('remove tag [', tagname, ']', f)
        delete tagFactory[tagname];
      });
    } else {
      log.error(err);
    }
  });

  //
  // 读取目录构建标签文件代理渲染函数
  // 当代理被调用的时候, 加载渲染器
  // 与系统标签的冲突
  // 与 HTML 客户端标签的冲突
  //
  function findfile(_dir, namespace) {
    hfs.eachDir(fs, _dir, false, function(fname, state) {
      if (state.isDirectory()) {
        var ns = path.basename(fname);
        if (namespace) {
          ns = namespace + '-' + ns;
        }
        findfile(fname, ns);
        return;
      }

      createTag(fname, namespace);
    });
  }

  function createTag(file, namespace) {
    if (path.extname(file) != EXT) {
      log.warn("不能作为自定义标签模板:", file, EXT);
      return;
    }

    var tagname = path.basename(file, EXT);
    if (namespace && namespace != '.') {
      tagname = namespace + ":" + tagname;
    }

    if (tagFactory[tagname]) {
      log.warn('标签名称已存在, 禁止覆盖:', tagname);
      return;
    }

    log.log('create tag [', tagname, ']', file)
    tagFactory[tagname] = TagProxy(file, tagFactory, pool);
  }


  function copyFrom(target, obj, checkName) {
    for (var n in obj) {
      if (checkName && tool.isSystemVar(n)) {
        throw new Error('cannot modify system var "' + n + '"');
      }
      target[n] = obj[n];
    }
    return target;
  }


  //
  // 延时渲染模板
  //
  function TagProxy(tagfile, tagFactory, pool) {

    // 这是渲染器工厂
    return function(taginfo, userTagFactory, errorHandle, filename) {

      // 这是渲染器
      return function(next, buf, context, tag_over) {
        var tag_context = {};
        var pit_call = null;
        copyFrom(tag_context, context);
        copyFrom(tag_context, taginfo.attr, true);
        tag_context.parent_context = context;

        //
        // pit 渲染顺序:
        // 1. renderPit 保存到上下文
        // 2. <pit> 标签调用保存的 renderPit 方法注册自身渲染函数
        // 3. renderPit 调用
        // 4. tag_over 的回调函数被调用
        // 5. renderTag 的回调函数被调用
        //
        if (tag_over) {
          var tag_scope = tag_context.tag_scope;
          // tag_scope.controler.disable_sub();
          tag_context[SAVE_KEY] = renderPit;

          tag_over(function() {
            tag_scope.controler.disable_sub();
            if (pit_call) {
              pit_call();
              pit_call = null;
              return 'stop';
            }
          });

          renderTag(function() {
            delete tag_context[SAVE_KEY];
            next();
          });
        } else {
          renderTag(next);
        }

        //
        // 引擎BUG: 该方法定义在 if 中无法调用
        //
        function renderPit(_over) {
          tag_scope.controler.enable_sub();
          pit_call = _over;
          next();
        }

        function renderTag(_render_over) {
          pool.getRender(tagfile, userTagFactory, errorHandle, function(render) {
            render(_render_over, buf, tag_context);
          });
        }
      };

    };
  }

}

}); // install-def-tag.js end
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./template.js', function(module, require) {

var base = require('./base.js');
var tool = require('./tool.js');
var SyntaxError  = require('./error.js').SyntaxError;

// 只有开始没有结束符的 HTML 标签
var notEndButEndTag = { '!doctype':1, 'meta':1, 'link':1 }
// 空字符的定义
var spch = { ' ':1, '\t':1, '\n':1, '\r':1 };
var __tag_id = 0;

// ko 索引的定义
var BEGIN     = 0,
    END       = 1,
    TYPE      = 2,
    NAME      = 3,
    ATTR      = 4,
    ATTR_STR  = 5,
    LINE      = 6,
    COL       = 7,
    I_LEN     = 8,
    SKIP_TAG  = 99,
    EXP_TAG   = '_bird_expression_',
    CALL_DEP  = '__cAlL_depth_save';


// 导出库
module.exports.html_complier        = html_complier;
module.exports.parseHtmlStructure   = parseHtmlStructure;

tool.addSystemVar('builder');    // rqa83
tool.addSystemVar('expression'); // rqa83
tool.addSystemVar('parse');      // rqa83
tool.addSystemVar('tag_scope');  // ct135
tool.addSystemVar(CALL_DEP);


//
// 编译 HTML 并返回渲染器
// 出现错误会抛出异常
//
// buf: Buffer -- 保存 html 代码的缓冲区
// filename: string -- 文件名用于输出错误消息, 可以 null
// _v_errorHandle: Function -- 设置错误捕获函数, 之后可以通过返回的对象修改
// _depth_max: Integer -- 标签嵌套调用的最大次数, 默认 10
//
// userTagFactory: Object
//    -- 用户自定义渲染器工厂, 结构: { KEY: Factory }
//    -- KEY:String -- 指明标签名称,
//    -- Factory: function(tag, userTagFactory, _v_errorHandle)
//    --    返回一个渲染器 [look: urp95, urp113]
//
//    -- tag 中保存有:
//    --    attr:{} -- 解析后的属性对象
//    --    attr_str:String -- 属性的原始字符串
//    --    selfend -- 如果没有标签体, 则为 true
//    --    allow_body -- 设置这个属性, 如果有标签体且设置这个属性为 false, 则抛出异常
//    --    identify -- 一个无重复的识别数字
//
// mark: hcbu39
//
function html_complier(buf, userTagFactory, _v_errorHandle, filename, _depth_max) {

  // 配对后的 ko, 可搜索: [pair 元素的结构]
  var pair = [];
  // 每个标签的起始/结束位置, 和标签类型
  var ko = [];
  var depth_max = _depth_max || 10;

  // 只保留 userTagFactory 中需要的 tag
  var name_filter = function(name) {
    if (userTagFactory[name] == null)
      return SKIP_TAG;
  };

  parseHtmlStructure(buf, ko, pair, name_filter, filename);

  var builder = base.html_builder();
  var renderQueue = builder.saver();
  var staticBufQueue = [];

  // 初始化上下文
  renderQueue.add(function(next, buf, context) {
    if (!context) throw new Error("context cannot be null.");

    // mark rqa83
    context.builder    = builder;
    context.expression = tool.expression_complier;
    context.parse      = tool.parseExp;

    if (!context[CALL_DEP]) {
      context[CALL_DEP] = [];
    }

    next();
  });

  createStaticTxtRenderQueue();
  createRenderQueue();
  clearMem();

  // 绑定一个方法, 由于修改错误捕获函数
  renderQueue.setErrorHandle = function(handle) {
    handle && (_v_errorHandle = handle);
  }

  return renderQueue;

  //
  // 因为 _v_errorHandle 会依每次调用改变,
  // 所以设置一个代理函数
  //
  function errorHandle(err) {
    _v_errorHandle(err);
  }

  //
  // 创建用户标签的渲染器
  // 被 renderBasicPack 包装
  //
  function userRenderPack(user_render_factory, p) {

    var user_render;

    try {
      p.allow_body = true;

      //
      // 这里决定用户自定义标签工厂函数原型
      // mark urp113
      //
      user_render = user_render_factory(p, userTagFactory, errorHandle, filename);

      if (p.allow_body == false && p.selfend == true) {
        throw new Error('not allow BODY');
      }
    } catch(complier_err) {
      return sendErr(complier_err);
    }

    return function(next, buf, context) {
      p.controler.enable_sub();

      //
      // 标签范围变量的创建
      //
      // mark ct135
      context.tag_scope = {
        name      : p.name,
        body      : p.body,
        attr      : p.attr,
        attr_str  : p.attr_str,
        controler : p.controler,
        filename  : filename,
        line      : p.line,
      };

      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      //
      // 这里决定自定义渲染器函数的参数, 和 context 中的对象
      //
      // tag_over -- function(cb) 当标签结束时 cb 被调用(此时 attr_str 无效)
      //          -- 自结束标签, 这个属性为 null, cb 返回 'stop' 会终止渲染
      //
      // mark: urp95
      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      try {
        user_render(_over_clear_val, buf, context, p.tag_over);
      } catch(run_time_err) {
        return sendErr(run_time_err);
      }

      function _over_clear_val() {
        context.tag_scope = null;
        next();
      }
    };

    function sendErr(err) {
      var e = SyntaxError("[" + p.name + " Tag] "
            + err.message, p.line, p.col, filename);
      errorHandle(e);
      //
      // 返回一个空渲染器, 并且到此为止, 不再渲染
      //
      return function(next, buf, context) {
      };
    }
  }

  //
  // 返回表达式渲染器
  // 被 renderBasicPack 包装
  //
  function expressionRender(pair) {
    var exp = tool.expression_complier(pair.attr_str);

    return function(next, buf, context) {
      try {
        var ret = exp.val(context);

        if (typeof ret != 'string') {
          ret = JSON.stringify(ret);
        }
        if (ret) {
          buf.write(ret);
        }
      } catch(err) {
        buf.write(err.message);
      }

      next();
    }
  }

  //
  // 清理掉内存引用
  //
  function clearMem() {
    pair
      = ko
      = staticBufQueue
      = name_filter
      // = renderQueue // 不能删除
      = null;
  }

  //
  // 把所有渲染器按顺序拼装起来
  //
  function createRenderQueue() {
    var staticBegin = 0;
    var sbq_len     = staticBufQueue.length;
    var pair_len    = pair.length;

    for (var i = 0; i < pair_len; ++i) {
      var tpair = pair[i];
      var controler = tpair.controler = createControler();
      var body_bufs = tpair.body = [];

      tpair.identify = ++__tag_id;

      // 把这个标签之前的静态文本压入渲染队列
      while (staticBegin < sbq_len) {
        if (staticBufQueue[staticBegin].begin < tpair.b1) {
          renderQueue.add(staticBufQueue[staticBegin].render);
          ++staticBegin;
        } else {
          break;
        }
      }

      if (!tpair.selfend) {
        tpair.tag_over = createOverCall();
        var lastTag = null;

        // 将标签体中的文本对象引用记录在标签对象中,
        // 允许标签对象修改这些文本
        // 同时允许标签对象控制这些文本 (通过控制器)
        for (var si = staticBegin; si < sbq_len; ++si) {
          if (staticBufQueue[si].begin < tpair.e2) {
            body_bufs.push(staticBufQueue[si].buf);
            controler.add_sub(staticBufQueue[si]);
            lastTag = staticBufQueue[si];
          } else {
            break;
          }
        }

        // 将标签体中的标签对象引用记录在标签对象中,
        // 允许标签对象对这些标签进行控制
        for (var ni = i+1; ni < pair_len; ++ni) {
          if (pair[ni].b1 < tpair.e1) {
            controler.add_sub(pair[ni]);

            // lastTag 插入的文本对象可能比当前的标签对象更靠后
            if (lastTag == null || pair[ni].b1 > lastTag.b1) {
              lastTag = pair[ni];
            }
          } else {
            break;
          }
        }

        // 将标签的结束通知函数, 传递给标签体中的最后一个标签
        lastTag.parent_tag_over = tpair.tag_over.call;
      }

      // 标签本身的渲染器加入队列, 对表达式进行的特殊处理
      var render =
        tpair.name == EXP_TAG
          ? renderBasicPack(tpair, expressionRender(tpair))
          : renderBasicPack(tpair, userRenderPack(userTagFactory[tpair.name], tpair));

      renderQueue.add(render);
    }

    // 末尾的渲染器
    for (var i = staticBegin; i < sbq_len; ++i) {
      renderQueue.add(staticBufQueue[i].render);
    }
  }

  //
  // 创建静态文本分段渲染队列
  //
  function createStaticTxtRenderQueue() {
    var begin = 0;

    for (var i = 0; i < ko.length; i += I_LEN) {
      addpart(begin, ko[i + BEGIN]);
      begin = ko[i + END] + 1;
    }

    if (begin < buf.length) {
      addpart(begin, buf.length);
    }

    // 创建一个缓冲区的分片
    function addpart(begin, end) {
      var partbuf = buf.slice(begin, end);

      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      //
      // 静态文本元素的结构
      //
      // mark: ap230
      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      var txt_item = {
        begin  : begin,
        end    : end,
        buf    : partbuf,
        enable : true
      };

      txt_item.render = renderBasicPack(
          txt_item, builder.txt(txt_item.buf));

      staticBufQueue.push(txt_item);
    }
  }

  //
  // 包装一个可渲染元素和一个渲染器, 返回的渲染器实现基础功能
  //
  // item -- 可渲染元素, 有 enable 属性控制是否渲染
  //      -- parent_tag_over 方法通知父标签渲染结束
  //      -- 如果 parent_tag_over 返回 'stop' 则停止渲染
  // render -- 被包装的渲染器, 实现具体的逻辑
  //
  function renderBasicPack(item, render) {
    var id = item.identify ? filename +':'+ item.identify : null;

    return function(next, _buf, context) {
      if (item.enable) {
        if (add_check_depth(context) == false) {
          var e = SyntaxError("[" + item.name + " Tag] overflow max depth call "
                + depth_max , item.line, item.col, filename);

          errorHandle(e);
          return;
        }

        render(basic_over, _buf, context);
      } else {
        next();
      }

      function basic_over() {
        // console.log('call over', item.name)
        var do_next = true;
        // 可能会减到负数, 因为不是从渲染器开始处调用的
        decrease_depth(context);

        // parent_tag_over 与 createOverCall.call 绑定
        // 返回 stop 之后, 还可以通过 调用 next 来继续渲染
        if (item.parent_tag_over) {
          do_next = ( 'stop' !=  item.parent_tag_over(next));
        }

        if (do_next) {
          next();
        }
      }
    };

    function decrease_depth(context) {
      if (!id) return;
      context[CALL_DEP][id] -= 1;
      // console.log('-call', context[CALL_DEP][id], id)
    }

    function add_check_depth(context) {
      if (!id) return true;

      var calld = context[CALL_DEP];
      if (isNaN(calld[id])) {
        calld[id] = 0;
      }

      // console.log('+call', calld[id], id)
      calld[id] += 1;
      return calld[id] < depth_max;
    }
  }
}

//
// 控制器, 在父元素和子元素之间传递
// 隐藏变量, 防止在上下文中越界操作
//
function createControler() {
  var ret = {
  };

  var _sub = [];
  var state = 'unknow';

  ret.disable_sub = function() {
    _sub.forEach(function(item) {
      item.enable = false;
    });
    state = 'diable';
  };

  ret.enable_sub = function() {
    _sub.forEach(function(item) {
      item.enable = true;
    });
    state = 'enable';
  };

  ret.state_sub = function() {
    return state;
  };

  ret.add_sub = function(obj) {
    obj && _sub.push(obj);
  }

  return ret;
}

//
// 当标签体中的最后一个标签结束渲染后
// 通知父标签
//
function createOverCall() {
  var ovar_cb = null;

  var ret = function(_ovar_cb) {
    ovar_cb = _ovar_cb;
  };

  ret.call = function(x) {
    // if (!ovar_cb) {
    //   console.error("call but no fn")
    // }
    return ovar_cb && ovar_cb(x);
  };

  return ret;
}

//
// 解析 buf:Buffer 中的 HTML 数据
// 结果存入 pair:[], filter 做过滤
// filter == null, 可以解析出所有的标签
//
function parseHtmlStructure(buf, ko, pair, filter, filename) {
  var len  = buf.length;

  init();

  if (ko.length % I_LEN != 0) {
    throw new Error('ko offset error');
  }

  dopair();
  return;

  //
  // ko 中的标记进行配对, 保存在 pair
  //
  function dopair() {
    var b=0, e= 0;

    while (b < ko.length) {
      var rect;
      var ty = ko[b + TYPE];

      if (ty == 'begin') {
        e = b + I_LEN;
        var deb = 1;
        var find = false;

        while (e < ko.length) {
          if (ko[e + TYPE] == 'end') {
            --deb;
            if (deb == 0 && ko[e + NAME] == ko[b + NAME]) {
              find = true;
              break;
            }
            else if (deb < 0) {
              break;
            }
          } else if (ko[e + TYPE] == 'begin') {
            ++deb;
          }

          e += I_LEN;
        }

        if (find) {
          rect = {
            b1 : ko[b + BEGIN], b2: ko[b + END],
            e1 : ko[e + BEGIN], e2: ko[e + END],
            selfend: false
          };
        } else {
          throw SyntaxError("Syntax error cannot find END tag `" +
              ko[b + NAME] + "`", ko[b + LINE], ko[b + COL], filename);
        }

      } else if (ty == 'self') {

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //
        // pair 元素的结构 1
        //
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        rect = {
          b1 : ko[b + BEGIN], b2: ko[b + END],
          e1 : -1,            e2: ko[b + END],
          selfend: true
        };
      }

      if (rect) {
        //
        // pair 元素的结构 2
        //
        rect.name     = ko[b + NAME];
        rect.attr     = ko[b + ATTR];
        rect.attr_str = ko[b + ATTR_STR];
        rect.line     = ko[b + LINE];
        rect.col      = ko[b + COL];
        rect.enable   = true;

        pair.push(rect);
        rect = null;
      }
      b += I_LEN;
    }
  }

  //
  // 将 html 标记的位置记录在 ko 中
  //
  function init() {
    var line = 1, col = 1;
    var flag = 1;
    var a, b, i = 0
    var exc = 0;
    var name, endmark, type;
    var scriptindex = 0;

    while(i < len) {
      var ch = buf[i];

      if (flag == 1) { // 寻找 '<'' 字符 作为标签的开始
        if (ch == '#') {
          flag = 3;
          continue;
        }

        if (ch == '<') {
          ko.push(i);
          a = i;
          flag = 2;

        } else if (ch == '>') {
          if (name != 'script') {
            // throw SyntaxError('should not `>` ' +
            //     buf.toString().substr(i-50,99), line, col, filename);
            // 这个逻辑既不是错误, 也不是警告
            // console.warn('should not `>` near:`' +
            //     buf.toString().substr(i-10, 30), '` at [', line, col, ']', filename)
          }
        }
      }

      else if (flag == 6) { // script 标记不允许有子标签, 注释中的, 字符串中的未处理
        var scriptEnd = '</script>';

        if (spch[ch] != 1) {
          if (ch == scriptEnd[scriptindex]) {
            if (++scriptindex >= scriptEnd.length) {
              i = a = (i - scriptEnd.length +1);
              ko.push(a);
              // console.log('is script find end', a, line, ko, buf[a]);
              flag = 2;
            }
          } else {
            scriptindex = 0;
          }
        }
      }

      else if (flag == 2) { // 找到 '<' 后寻找 '>' 作为标签结束
        // 掠过注释
        if (ch == '!' && a+1==i) {
          if (buf[i+1] == '-' && buf[i+2] == '-') {
            var u = i;
            while (u < len) {
              if (buf[u] == '\n') { ++line; col=0; }
              else
              if (buf[u] == '-' && buf[u+1] == '-' && buf[u+2] == '>') {
                i = u + 3; break;
              }
              ++u;
            }
            if (u >= len) {
              throw SyntaxError('Comment not END!', line, col, filename);
            }
            flag = 1;
            ko.pop();
            continue;
          }
        }

        else if ((ch == "'" || ch == '"') && (buf[i-1] != '\\')) {
          if (endmark) {
            if (endmark == ch) endmark = null;
          } else {
            endmark = ch;
          }
        }

        else if (ch == '>') {
          if (endmark) { ++i; continue; }
          ko.push(i);
          b = i;
          flag = 1;
          name = findAttr(a, b, line, col);
          type = ko[ko.length - I_LEN + TYPE];

          if (filter && (filter(name) == SKIP_TAG)) {

            for (var ski=0; ski<I_LEN; ++ski)
              ko.pop();

            i = a+1; // 偏移回到 '<' 符号的下一位
          }
          else if (name == 'script' && type == 'begin') {
            flag = 6;
            continue;
          }

        } else if (ch == '<') {
          if (endmark) { ++i; continue; }
          // throw SyntaxError('should not `<`:: ' +
          //     buf.toString().substr(i-10,30), line, col, filename);

          // 这个逻辑既不是错误, 也不是警告
          // console.warn('should not `<` near `' +
          //     buf.toString().substr(i-10, 30), '` at [', line, col, ']', filename)
        }
      }
      else if (flag == 3) {
        if (ch == '#') {
          if (++exc >= 3) { // 出现 ### 开始表达式
            a = i - 2;
            flag = 4;
          }
        } else {
          exc = 0;
          flag = 1;
          continue;
        }
      }
      else if (flag == 4) {
        if (ch == '#') {
          if (--exc <= 0) { // 出现 ### 结束表达式
            b = i;
            flag = 1;

            ko.push(a);
            ko.push(b);
            ko.push('self');
            ko.push(EXP_TAG);
            ko.push(null);
            ko.push(buf.slice(a+3, b-2));
            ko.push(line);
            ko.push(col);
          }
        } else if (exc == 3) { // 表达式没有结束
          if (ch == '\r' || ch == '\n') {
            throw SyntaxError("expression not allowed new line",
                line, col, filename);
          }
        } else {
           throw SyntaxError("expression end invalid char:" + ch,
              line, col, filename);
        }
      }

      if (ch == '\n') {
        // if (flag > 2) throw SyntaxError("Tag New Line is not allowed " +
        //     buf.toString().substr(i-10, 30), line, col, filename);

        ++line; col = 0;
      }
      else ++col;
      // 使用 continue 可以跳过 ++i, 就是把字符重新处理一次
      ++i;
    }
  }

  function findAttr(a, b, line, col) {
    var f = 0;
    var n=[], v=[], endmark, skip=0;
    var tagname = [];
    var selfend = false;
    var isend = false;
    var attr = {};
    var attr_str = [];

    // 第一次 ++a 可以跨过首个符号 '<'
    while (++a<b) {
      var c = buf[a];

      if (c == ' ' || c == '\t' || c == '\n') {
        if (f == 3) {
          v.push(c);
        }
        else if (f == 0) {
          if (tagname.length > 0) f = 1;
        }
        // Else skip sp
      }
      else if (f == 4) {
        throw SyntaxError("Tag end but some invaild char: "
            + buf.toString().substr(a-10, 50), line, col, filename);
      }
      else if (f == 3) {
        if (c == endmark) {
          attr[n.join('')] = v.join('');
          n = [];
          v = [];
          f = 1;
          endmark = null;
        } else {
          v.push(c);
        }
      }
      else if (f == 2) {
        if (c == "'" || c == '"') {
          endmark = c;
          f = 3;
        }
      }
      else if (f == 1) {
        if ((c == "'" || c == '"') && endmark == null) {
          endmark = c;
        }
        else if (c == endmark) {
          endmark = null;
        }
        else if (c == '/' && endmark == null) {
          selfend = true;
          f = 4;
        }
        else if (spch[c] != 1) {
          if (c == '=') {
            f = 2;
          } else {
            n.push(c);
          }
        }
      }
      else /* if (f == 0) */ {
        if (c == '/') {
          if (tagname.length > 0) {
            selfend = true;
          } else {
            isend = true;
          }
        } else if (spch[c] != 1) {
          tagname.push(c);
        }
      }

      if (f > 0 && f < 4) {
        if (!selfend) {
          attr_str.push(c);
        }
      }
    }

    tagname = tagname.join("").toLowerCase();

    if (selfend || notEndButEndTag[tagname])
                    { ko.push('self');  }
    else if (isend) { ko.push('end');   }
    else {            ko.push('begin'); }

    ko.push(tagname);
    ko.push(attr);
    ko.push(attr_str.join(''));
    ko.push(line);
    ko.push(col);
    return tagname;
  }
}

}); // ./template.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// 针对 xBoson 做了修改
///////////////////////////
__install('./file-pool.js', function(module, require) {

var vm   = require('vm');
var path = require('path');
var log  = console;
var tpl  = require('./template.js');
var bse  = require('./base.js');

var PRIVATE    = module.exports.PRIVATE = true;
var PUBLIC     = module.exports.PUBLIC  = false;
var COMPLIER   = 'html-complier-render';
var STATIC_TXT = 'static-text-render';

module.exports.createPool = createPool;

//
// 创建模板缓存，加快访问速度
//
// cnf -- 全局配置
// fs  -- 文件获取器，与 ‘nodejs.fs’ 库的定义相同
//
// !还需要加入当内存紧张的时候如何清理内存
//
function createPool(cnf, fs) {

var EXT_NAME       = cnf.extname ? ('.' + cnf.extname) : '.xhtml';
var MAX_FILE_SIZE  = cnf.max_file_size || 1 * 1024 * 1024;
var CACHE_TIME     = cnf.cache_time    || 3 * 60; // 秒
var DEPTH_MAX      = cnf.depth_max     || 10;

// 对编译好的脚本进行缓存
var script_cache = {};
// 全局使用唯一的缓存
var render_cache = {};

var ret = {
  getWatcher        : getWatcher,
  getFile           : getFile,
  getRender         : getRender,
  getRenderFromType : getRenderFromType,
  getPrivatePath    : getPrivatePath,
  getScript         : getScript,
  PRIVATE           : PRIVATE,
  PUBLIC            : PUBLIC,
};

return ret;

//
// 从一段代码或文件中取得脚本, 编译后返回
// 该方法与 nodejs.require 不同, 是异步的
//
// 如果 code 为 null 则从 filename 文件中读取脚本, 同时 id 被忽略
// 否则 code 中保存有脚本, filename + id 作为缓存识别
//
// 编译后的脚本运行后会返回一个包装函数, 这个函数的参数名称
// 在 from_callback_argv 中定义
// 这个函数会从沙箱中取得一些变量作为上下文变量, 这些变量的名称
// 在 from_context_argv 中定义
//
// script 对象的属性:
//    filename 关联的文件名
//    module   运行时的 this 对象
//
function getScript(code, id, filename, lineOffset,
    from_context_argv, from_callback_argv, cb)
{
  var scid = code ? (filename +':'+ id) : filename;

  if (script_cache[scid]) {
    // log.debug('is cache script', scid);
    return cb(null, script_cache[scid]);
  }

  try {
    if (code) {
      getWatcher(filename, del_cache);
      cb(null, code_success(code));
    } else {
      fs.readFile(filename, function(err, buf) {
        if (err) return cb(err);
        getWatcher(filename, del_cache);
        cb(null, code_success(buf));
      });
    }
    updateGlobalTime(Date.now());
  } catch(err) {
    cb(err);
  }

  function code_success(code) {
    var script = compile(code);
    if (script) {
      script_cache[scid] = script;
      script.filename = filename;
      script.module =
        {
          exports   : {},
          filename  : filename,
          id        : scid,
          parent    : null,
          children  : []
        };
    }
    return script;
  }

  function del_cache() {
    log.debug('del cached script', scid);
    delete script_cache[scid];
  }

  //
  // 编译一个脚本, 与运行相关
  //
  function compile(code) {
    var argv1 = from_context_argv  ? from_context_argv.join(',')  : '',
        argv2 = from_callback_argv ? from_callback_argv.join(',') : '';

    var warp = [
      '(function (', argv2, ') { var thiz = {};',
        '(function (', argv1, ") { ",
            code,
        "\n}).call(thiz, ", argv1, ')',
      '});'
    ];

    code = warp.join('');
    return vm.Script(code, {
      filename    : filename,
      lineOffset  : lineOffset -1, // 减去一行 warp 产生的行
    });
  }
}


//
// 当文件超时或改变, 监听器会被通知
// 返回 null
// ! 该方法在 xBoson 重新实现 !
//
function getWatcher(filename, _change_listener) {
//  fileChangeEvent.on(filename, _change_listener);
  console.debug("getWatcher() >>>", filename);
  fileChangeEvent.on(filename, _change_listener);
}


//
// _over: function(err, fileBuffer)
//
function getFile(filename, when_read_over, _change, _encoding) {

  fs.readFile(filename,  { encoding: _encoding },
    function(err, data) {
      if (err)
        return when_read_over(err);

      //
      // 这里创建了 fileBuffer 的数据结构
      //
      var fileBuffer = {
        buf: data,
        changed: false
      };

      fs.stat(filename, function(err, st) {
        fileBuffer.last_modify = lastModTime(err, st);
        getWatcher(filename, clear_buffer);
        when_read_over(null, fileBuffer);
      });

      function clear_buffer() {
        fileBuffer.changed = true;
        fileBuffer.buf = null
        _change && _change();
      }
    }
  );
}


//
// 把文件解析为渲染器返回, 可能返回的是缓存
// 如果扩展名不是服务端脚本, 则返回文本渲染器
// _over: function(err, fileRender)
//
function getRender(filename, tag_factory, _err_cb, _over_cb) {
  if (render_cache[filename]) {
    return _over_cb(render_cache[filename]);
  }
  return helper.lockCall(getRenderImpl, getRenderImpl,
          filename, tag_factory, _err_cb, _over_cb);
}


//
// 在该函数的调用上加锁, 防止多线程重入, 重复编译模板.
//
function getRenderImpl(filename, tag_factory, _err_cb, _over_cb) {
  if (render_cache[filename]) {
    return _over_cb(render_cache[filename]);
  }

  var isTemplateFile = (path.extname(filename) == EXT_NAME);
  var _encoding = isTemplateFile && cnf.encoding;

  getFile(filename, function(err, fileBuffer) {
    if (err) return _err_cb(err);

    try {
      var part = null;

      if (isTemplateFile) {
        part = tpl.html_complier(fileBuffer.buf,
            tag_factory, _err_cb, filename, DEPTH_MAX);
        part.file_type = COMPLIER;
        part.last_modify = 0;
      } else {
        part = bse.html_builder().txt(fileBuffer.buf);
        part.file_type = STATIC_TXT;
        part.last_modify = fileBuffer.last_modify;
      }

      updateGlobalTime(fileBuffer.last_modify);
      render_cache[filename] = part;
      _over_cb(part);
    } catch(e) {
      _err_cb(e);
    }

  }, function() {
    render_cache[filename] = null;
  }, _encoding);
}


//
// 检查路径有效性, 并返回这个路径
// 失败抛出异常
//
function getPrivatePath(dir, filename, is_public) {
  var base = path.normalize( is_public ? cnf.public : cnf.private );
  if (dir) base = path.join(base, dir);
  var filepath = path.join(base, filename);

  if (filepath.indexOf(base) != 0) {
    throw new Error("Permissions error to read file " + filepath);
  }
  return filepath;
}


//
// 从目录中读取文件并解析为渲染器
// filename -- 原始文件名
// p_state  -- 是否从私有目录来
// dir      -- 子目录, 如果 p_static==false, 则忽略这个参数
// _over_cb -- Function(render) 返回渲染器,
//             render.file_type 渲染器类型,
//             render.last_modify 最后修改时间, -1 则无效
//
function getRenderFromType(dir, filename, p_state, tag_factory, _err_cb, _over_cb) {
  try {
    var is_public = (p_state == PUBLIC);
    var fn = getPrivatePath(is_public ? null : dir, filename, is_public);
    var isTemplateFile = (path.extname(filename) == EXT_NAME);

    fs.stat(fn, function(err, stats) {
      if (err) return _err_cb(err);

      if (stats.size > MAX_FILE_SIZE && isTemplateFile == false) {
        getBigStaticFileRender(fn, _err_cb, _over_cb, stats.size);
      } else {
        getRender(fn, tag_factory, _err_cb, _over_cb);
      }
    });

  } catch(err) {
    _err_cb(err);
  }
}


//
// 大文件不会缓冲, 使用这个方法创建渲染器
// 渲染器只能使用一次, 之后丢弃
//
function getBigStaticFileRender(filename, _err_cb, _over_cb, filesize) {

  fs.open(filename, 'r', function(err, fd) {
    if (err) return _err_cb(err);

    var render = function(next, buf, context) {
      var reader = fs.createReadStream(null, {
        fd: fd, autoClose: true
      });

      reader.pipe(buf, { end: false });

      reader.on('end', function() {
        next();
      });
    };

    fs.fstat(fd, function(err, st) {
      render.file_type = "big-nocache-file";
      render.last_modify = lastModTime(err, st);
      _over_cb(render);
    });
  });
}


function lastModTime(err, st) {
  // console.log('-------------))))))))))))))))', err, st)
  if (err) return -1;
  return st.mtime.getTime();
}

// createPool --END--
}

}); // file-pool.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
__install('./mid.js', function(module, require) {

var systag = require('./install-systag.js');
var deftag = require('./install-def-tag.js');
var pooll  = require('./file-pool.js');
var tool   = require('./tool.js');
var conf   = require('./conf.js');
var path   = require('path');
var log    = console;
var mime   = require('mime');
var vm     = require('vm');
var url    = require('url');
var mix    = require('mixer-lib');
var util   = require('util') ;
var qs     = require('querystring');


//var __id = 0;


//
// baseurl     -- url 起始路径
// _config:
//    public   -- 公共访问目录, web 资源根目录, 绝对目录
//    private  -- 私有目录, 脚本等文件目录, 绝对目录
// _debug      -- 调试模式在 html 页面上打印错误栈
// _fs_lib     -- 文件获取器，与 ‘fs’ 库的定义相同, 默认使用 fs,
//    自定义 fs-lib 需要实现的方法, 定义与 fs 中的相同
//    readFileSync watch readFile
//    stat open createReadStream fstat
//
// 待实现: session,
//
module.exports = function(baseurl, _config, _debug, _fs_lib) {
  var config = _config ||  conf.load().masquerade;
  var is_dev = _debug  || (conf.load().env == 'development');
  var EXT    = '.' + config.extname;
  var fs     = _fs_lib || require('fs');
  var pool   = pooll.createPool(config, fs);

  //
  // 一个应用范围的沙箱, 允许多个脚本之间的数据通讯
  // 延迟创造这个对象, 提升效率
  // ! 随请求而创建沙箱, 会导致内存泄漏 * nodejs 6.9 已经修复.
  //
  // var sandbox = null;

  //
  // 一旦静态文件出错, 这个救急用
  //
  // var staticfile = mix.util.mid().ex_static(config.public, baseurl, true);

  //
  // 标签工厂, 用于创建各种服务端标签
  //
  var tag_factory = {};

  //
  // 默认页使用 ',' 分割
  //
  var default_page = config.default_page;
  if (!default_page) default_page = 'index.html';
  default_page = default_page.split(',');

  //
  // baseurl 始终不以 '/' 结尾, 或本身是 ''
  //
  baseurl = last_not_div(baseurl);


  systemVar();
  systag(tag_factory);
  deftag(pool, tag_factory, EXT, fs);

  var ret = _mid_process;
  ret.add_plugin = add_plugin;
  ret.reload_tags = _reload_tags;
  return ret;


  function _reload_tags(path, typename) {
    if (!path) {
      return _reload_all_tags();
    }
    path = Path.normalize(path);
    typename = typename || 'change';
    console.debug("Reloading >>>", path, ':', typename);
    fileChangeEvent.emit(path, typename);
    fileChangeEvent.emit('[*]', path, typename);
    updateGlobalTime(Date.now());
  }

  //
  // xBoson 增加: 重新读取标签库
  //
  function _reload_all_tags() {
    console.warn("Reload TAG Library...");

    var old = tag_factory;
    tag_factory = {};
    systag(tag_factory);
    deftag(pool, tag_factory, EXT, fs);

    for (var name in old) {
      if (! tag_factory[name]) {
        tag_factory[name] = old[name];
      }
    }
    fileChangeEvent.emit('[reload]');
  }


  function _mid_process(res, rep, next) {
    // 后面用到的属性, 而 native 并未提供
    var query   = url.parse(res.url, true);
    res.query   = query.query;
    res.path    = query.pathname;

    if (!next) {
      next = log.debug;
    }

    if (res.path.indexOf(baseurl) != 0) {
      return next();
    }

    rep.on('error', function(err) {
      log.error('Response', err);
    });

    var stime = process.hrtime();
    var _url = res.path;

    // 一旦静态文件出错, 这个救急用 2
    // if (path.extname(res.url) != EXT) {
    //   return staticfile(res, rep, next);
    // }

    find_url_page(res.path, function(err, __url, isJump) {
      if (err) return next(err);
      try {
        if (isJump) {
          var _base_path = res.header('proxy-original-url');
          if (_base_path) {
            _base_path = url.parse(_base_path).pathname;
          } else {
            _base_path = baseurl;
          }
          redirect(join_url(_base_path, __url));
          return;
        }
        checkRequest(function() {
          pool.getRenderFromType(null, __url,
              pool.PUBLIC, tag_factory, errorHandle, successHandle);
        });

      } catch(err) {
        errorHandle(err);
      }
    });


    function successHandle(render) {
      // console.log(res.headers)
      rep.setHeader("Content-Type", mime.lookup(_url));

      if (lastModifyHandle(render)) {
        return;
      }

      var context = createRequestContext(res, rep);
      render.setErrorHandle && render.setErrorHandle(errorHandle);
      render(overRender, rep, context);
    }


    function lastModifyHandle(render) {
      if (res.header('pragma') == 'no-cache')
        return false;

      if (res.header('cache-control') == 'no-cache')
        return false;

      var ltime = render.last_modify || globalLastTime();
      var if_mod_sin = res.header('If-Modified-Since');
      if (if_mod_sin) {
        if_mod_sin = new Date( if_mod_sin ).getTime();
        //
        // 999 毫秒修正, 修改时间忽略毫秒部分
        //
        if (ltime - if_mod_sin < 1000) {
          rep.statusCode = 304;
          rep.setHeader("Masquerade", "not-modify");
          overRender('[304] Not modify');
          return true;
        }
      }

      rep.setHeader("Masquerade",    "render");
      rep.setHeader("Cache-Control", "must-revalidate");
      rep.setHeader("Pragma",        "no-cache");
      rep.setHeader('Last-Modified', new Date(ltime).toGMTString() );
      return false;
    }


    function errorHandle(err) {
      log.error('On Error', res.url, err);
      // is_dev=false;
      if (!rep.finished) {
        try {
          rep.statusCode = 500;
          rep.write('<pre>');
          rep.write('Cannot Get ');
          rep.write(res.url);
          rep.write("\n\n");

          if (is_dev) {
            rep.write(err.stack);
          } else if (err.why) {
            rep.write(err.why);
            rep.write('at line ');
            rep.write(err.line + '');
            rep.write(', column ');
            rep.write(err.col + '');
          } else {
            rep.write(err.message);
          }

          rep.write('</pre>');
        } catch(eee) {
          log.error(eee);
        }
      }
      overRender();
    }


    function overRender(debug_msg) {
      rep.end();
      var diff = process.hrtime(stime);
      var use  = diff[0] * 1e3 + diff[1] / 1e6;
      log.debug('Request', _url, debug_msg||'', 'use', use, 'ms');
    }


    function redirect(where) {
      rep.statusCode = 302;
      rep.setHeader('Location', where);
      rep.end();
    }


    function checkRequest(next) {
      if (res.header('Range')) {
        // log.error("cannot support Range HTTP field");
        rep.setHeader('Accept-Ranges', 'none');
      }

      if (res.header('content-type') == 'application/x-www-form-urlencoded') {
        return parseBody(res, next);
      }
      return next();
    }
  }


  // 在系统初始化前, 先要注册变量名
  function systemVar() {
    tool.addSystemVar('getHeader');
    tool.addSystemVar('setHeader');
    tool.addSystemVar('nextId');
    tool.addSystemVar('__end');
    tool.addSystemVar('query');
    tool.addSystemVar('getVmContext');
    tool.addSystemVar('baseurl');
    tool.addSystemVar('log');
    tool.addSystemVar('filepool');
    tool.addSystemVar('config');
    tool.addSystemVar('publicdir');
  }


  function createRequestContext(res, rep) {
    var context = {
      getHeader     : function(name) { return res.header(name) },
      setHeader     : function(n, v) { rep.setHeader(n, v) },
      nextId        : function() { return ++__id; },
      __end         : function(x) { rep.end(x) },
      query         : res.query,
      getVmContext  : getVmContext,
      baseurl       : baseurl,
      filepool      : pool,
      config        : config.runtime_cfg,
      publicdir     : config.public,
      setTimeout    : setTimeout,
      setInterval   : setInterval,
      log           : log,
      console       : log,
      global        : {},
    };

    return context;

    // 限制 context 即是可运行脚本的上下文.
    function getVmContext() {
      if (vm.isContext(context)) {
        return context;
      } else {
        // 每个请求一个沙箱, 在 nodejs 6.x 上不再有内存泄漏.
        return vm.createContext(context);
      }
    }
  }


  //
  // 判断路径是否是目录, 自动与配置中指定的默认页做跳转
  // 返回的 url 不含有 baseurl 路径
  //
  function find_url_page(_org_url, rcb) {
    var _url   = _org_url.substr(baseurl.length);
    var i      = -1;
    var r_url  = decodeURI(_url);
    var isJump = false;

    _next();


    function _next() {
      var file = path.join(config.public, r_url);
      fs.stat(file, function(err, stats) {
        if (err) {
          _next_def(_next);
        } else {
          if (stats.isFile()) {
            rcb(null, r_url, isJump);
          } else if (stats.isDirectory()) {
            isJump = true;
            _next_def(_next);
          } else  {
            _next_def(_next);
          }
        }
      });
    }


    function _next_def(__n) {
      if (++i < default_page.length) {
        if (_url) {
          r_url = join_url(_url, default_page[i]);
        } else {
          r_url = default_page[i];
        }
        __n();
      } else {
        rcb('not found ' + _org_url);
      }
    }
  }


  function add_plugin(pinfo) {
    if (!pinfo.name)
      throw new Error('invalid plugin name');

    if (!pinfo.func)
      throw new Error('invalid plugin render function');

    if (tag_factory[pinfo.name])
      throw new Error(pinfo.name + ' plugin is exist');

    tag_factory[pinfo.name] = pinfo.func;
  }
};


//
// 连接 url = a+b
//
function join_url(a, b) {
  var bf = b[0] == '/';
  var al = a[a.length-1] == '/';

  if (bf) {
    if (al) {
      return a + b.substr(1);
    } else {
      return a + b;
    }
  } else {
    if (al) {
      return a + b;
    } else {
      return a + '/' + b;
    }
  }
}


//
// 保证 url 不以 '/' 结尾
//
function last_not_div(u) {
  if (!u)
    return '';
  if (u[ u.length-1 ] == '/')
    return u.substring(0, u.length-1);
  return u;
}


//
// 保证 url 是以 '/' 结尾
//
function last_div(u) {
  if (!u)
    return '/';
  if (u[ u.length-1 ] == '/')
    return u;
  return u + '/';
}


function parseBody(res, next) {
  var bufs = [];

  res.on('data', function(b) {
    bufs.push(b);
  });

  res.on('end', function() {
    var post = Buffer.concat(bufs).toString();
    var body = qs.parse(post);
    var query = res.query;

    for (var n in body) {
      if (query[n]) {
        if (util.isArray(query[n])) {
          query[n].push(body[n]);
        } else {
          query[n] = [ query[n], body[n] ];
        }
      } else {
        query[n] = body[n];
      }
    }

    res.query = query;
    next();
  });
}


process.on('uncaughtException', function(err) {
  console.error('Caught exception: ', err.stack);
});

}); // mid.js END
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
function masquerade(baseurl, config, debug, uifs) {
  var mid = _require('./mid.js');

  //
  // fs 兼容层
  //
  var fs = uifs_to_nodefs(uifs);
  var mid_process = mid(baseurl, config, debug, fs);
  service.reload_tags = mid_process.reload_tags;
  service.add_plugin = mid_process.add_plugin;
  //  runEventLoop(); // 可能不需要了
  return service;

  function service(servletReq, servletResp) {
    //
    // request 兼容层
    //
    var req = servlet_request_to_node(servletReq);
    //
    // response 兼容层
    //
    var resp = servlet_response_to_node(servletResp);

    try {
      mid_process(req, resp, function(err) {
        if (err) console.error(err, new Error().stack);
      });
    } catch(error) {
      resp.write(error.stack || error.message || e);
    } finally {
      runEventLoop();
      resp.finished = true;
    }
  }
}


function uifs_to_nodefs(uifs) {
  var fs = {
    watch             : watch,
    readFile          : readFile,
    stat              : stat,
    open              : unsupport,
    createReadStream  : unsupport,
    fstat             : stat,
    readDir           : readDir,
  };
  return fs;


  function readFile(path, opt, cb) {
    if (cb == null && typeof opt == 'function')
      cb = opt;

    var encoding = opt && opt.encoding;

    try {
      var byte_arr = uifs.readFile(path);
      var buf = Buffer.from(byte_arr);

      if (encoding) {
        cb(null, buf.toString(encoding));
      } else {
        cb(null, buf);
      }
    } catch(err) {
      cb(err);
    }
  }

  function stat(path, cb) {
    try {
      var attr = uifs.readAttribute(path);
      if (!attr)
        return cb('not fonud ' + path);

      cb(null, createStat(attr));
    } catch(err) {
      cb(err);
    }
  }

  function readDir(path, cb) {
    while (path.length > 1 && path[path.length-1] == '/') {
      path = path.substring(0, path.length-1);
    }
    var dircontent = uifs.readDir(path);
    if (dircontent) {
      dircontent.forEach(function(attr) {
        var stat = createStat(attr);
        cb(path +'/'+ attr.path, stat);
      });
    }
  }

  function createStat(attr) {
    var ret = {
      ctime       : new Date(attr.lastModify), // change time,
      mtime       : new Date(attr.lastModify), // modify time,
      birthtime   : new Date(0),  // uifs 没有创建日期属性
      isDirectory : function() { return attr.isDir() },
      isFile      : function() { return attr.isFile() },
    };
    return ret;
  }

  function watch(path, cb) {
    path = Path.normalize(path);
    console.debug("Watch file change >>>", path);
    fileChangeEvent.on(path, cb);
  }

  function unsupport() {
    throw new Error("不支持静态文件加载");
  }
}


function servlet_request_to_node(servletReq) {
  var cp  = servletReq.getContextPath();
  var rq  = servletReq.getRequestURI();
  var qr  = servletReq.getQueryString();
  var url = rq.substring(cp.length, rq.length);
  var cache_headers = null;
  if (qr != null) {
    url += '?' + qr;
  }

  var req = {
    url     : url,
    headers : function() {
      if (!cache_headers) {
        cache_headers = {};
        var names = servletReq.getHeaderNames();
        while (names.hasMoreElements()) {
          var name = names.nextElement();
          cache_headers[name] = servletReq.getHeader(name);
        }
      }
      return cache_headers;
    },
    header : function(name) {
      return servletReq.getHeader(name);
    },
  };
  return req;
}


function servlet_response_to_node(servletResp) {
  var writer = servletResp.getWriter();
  var resp = {
    on          : function() {},
    setHeader   : function(n, v) { servletResp.setHeader(n, v); },
    finished    : false,
    write       : write,
    end         : function() { writer.flush() },
  };

  Object.defineProperty(resp, "statusCode", {
    get: function() {
      return servletResp.getStatus();
    },
    set: function(value) {
      servletResp.setStatus(value);
    },
    configurable: false,
    enumerable: false,
  });

  function write(str) {
    if (!str) return;
    if (Buffer.isBuffer(str)) {
      str = str.toString("utf8");
    }
    try {
      writer.write(str);
    } catch(err) {
      console.error("write() fail", err);
    }
  }

  return resp;
}


module.exports = {
  init  : masquerade,
  event : fileChangeEvent,
}
///////////////////////////////////////////////////////////////////////////////