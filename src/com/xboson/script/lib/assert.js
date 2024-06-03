////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月7日 17:05
// 原始文件路径: xBoson/src/com/xboson/script/lib/assert.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var assert = module.exports = ok;

assert.ok = ok;
assert.eq = equal;
assert.equal = equal;
assert.deepEqual = deepEqual;
assert.deepStrictEqual = deepStrictEqual;
assert.doesNotThrow = doesNotThrow;
assert.fail = fail;
assert.ifError = ifError;
assert.notDeepEqual = notDeepEqual;
assert.notDeepStrictEqual = notDeepStrictEqual;
assert.notEqual = notEqual;
assert.notStrictEqual = notStrictEqual;
assert.strictEqual = strictEqual;
assert.throws = throws;

assert.AssertionError = AssertionError;

Object.freeze(assert);


function ok(value, message) {
  if (!value)
    throw new AssertionError(message);
}


function equal(a, b, message) {
  if (a != b)
    throw new AssertionError(message, a, b, "!=");
}


function strictEqual() {
  if (actual !== expected) {
    throw new AssertionError(message, actual, expected, "!==");
  }
}


function _deep_loop(actual, expected, eqfn) {
  if (eqfn(actual, expected)) {
    return;
  }

  if (actual && expected && actual.constructor == Array
        && expected.constructor == Array) {

    if (actual.length == expected.length) {
      for (var i=0; i<actual.length; ++i) {
        _deep_loop(actual[i], expected[i], eqfn);
      }
    }
  } else {
    var a = 0, b = 0;
    for (var n in actual) {
      _deep_loop(actual[n], expected[n], eqfn);
      ++a;
    }
    for (var n in expected) {
      _deep_loop(actual[n], expected[n], eqfn);
      ++b;
    }
    if (a != b || (a==0 && b==0)) {
      throw new AssertionError();
    }
  }
}


function deepEqual(actual, expected, message) {
  try {
    _deep_loop(actual, expected, function(a, b) {
      return a == b;
    });
  } catch(e) {
    throw new AssertionError(message, actual, expected, "!=");
  }
}


function deepStrictEqual(actual, expected, message) {
  try {
    _deep_loop(actual, expected, function(a, b) {
      return a === b;
    });
  } catch(e) {
    throw new AssertionError(message, actual, expected, "!=");
  }
}


function fail(actual, expected, message, operator, stackStartFunction) {
  var e = new AssertionError(message, actual, expected, operator);
  if (stackStartFunction) {
    // maybe not working.
    Error.captureStackTrace(e, stackStartFunction);
  }
  throw e;
}


function ifError(value) {
  if (value) {
    throw value;
  }
}


function notDeepEqual(actual, expected, message) {
  try {
    deepEqual(actual, expected, message);
  } catch(e) {
    return;
  }
  throw new AssertionError(message, actual, expected, "==");
}


function notDeepStrictEqual(actual, expected, message) {
  try {
    deepStrictEqual(actual, expected, message);
  } catch(e) {
    return;
  }
  throw new AssertionError(message, actual, expected, "===");
}


function notEqual(actual, expected, message) {
  if (actual == expected) {
    throw new AssertionError(message, actual, expected, "==");
  }
}


function notStrictEqual(actual, expected, message) {
  if (actual === expected) {
    throw new AssertionError(message, actual, expected, "===");
  }
}


function doesNotThrow(block, error, message) {
  try {
    block();
  } catch(e) {
    if (e.constructor == error) {
      throw new AssertionError((message && (message + ":"))||'' + e.message);
    } else {
      throw e;
    }
  }
}


function throws(block, error, message) {
  var test = _make_test(error);
  try {
    block();
  } catch(e) {
    if (test(e)) {
      return;
    }
    throw new AssertionError(message, error.toString(), e.toString());
  }
  throw new AssertionError(message, 'throws error', 'not throw');
}


function _make_test(obj) {
  var test;
  if (obj instanceof RegExp) {
    test = function(e) {
      return obj.test(e.toString());
    };
  } else if (typeof obj == "string") {
    test = function(e) {
      return e.toString().indexOf(obj) >= 0;
    };
  } else if (obj == null) {
    test = function(e) {
      console.warn(e);
      return true;
    }
  } else {
    test = function(e) {
      if (e.constructor === obj) {
        return true;
      } else {
        return obj(e) === true;
      }
    };
  }
  return test;
}


function AssertionError(msg, actual, expected, op) {
  this.name     = 'AssertionError';
  this.actual   = actual;
  this.expected = expected;
  this.operator = op || '!=';
  this.message  = (msg || 'Fail');

  if (actual || expected) {
    this.message = this.message
        + " actual " + (this.operator) + " expected"
        + '\n\t\t  actual: ' + JSON.stringify(actual)
        + '\n\t\texpected: ' + JSON.stringify(expected);
  }
}
AssertionError.prototype = new Error();