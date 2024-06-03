////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年12月11日 13:47
// 原始文件路径: xBoson/src/com/xboson/app/lib/string_functions.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

(function() {
//
// 在脚本中, 经常会定义一个 sql 变量, 然后又试图使用 sql.query 执行 db 查询, 显然这是
// 因为 beetl 的设计缺陷允许定义的 sql 变量与系统 sql 模块区分, 在 js 标准中这是绝对不
// 允许的. 为了兼容这种情况, 在 String 对象上绑定与 sql 相关的方法, 这样这些 "错误" 的
// 脚本就可以正常运行了.
//
function __bind_sql_functions(func_name_list) {
  func_name_list.forEach(function(func_name) {

    String.prototype[func_name] = function() {
      var sql = moduleHandleContext.get("sql");
      //
      // java 导入的函数第一个参数是 this.
      //
      var arg = [sql];
      for (var i=0; i<arguments.length; ++i) {
        arg.push(arguments[i]);
      }
      return Function.call.apply(sql[ func_name ], arg);
    }

  });
}


__bind_sql_functions([
  "query", "queryPaging", "update", "updateBatch", "metaData",
  "commit", "rollback", "currentDBTimeString", "connection",
  "dbType", "msAccessConnection",
]);


//
// Function.call.apply 不能正确的调用 java 中重载的方法;
// 这些方法被单独重写.
//
String.prototype.query = function(_sql, _param, _name) {
  var sql = moduleHandleContext.get("sql");
  return sql.query(_sql, _param, _name || 'result');
};


String.prototype.queryPaging = function(_sql, _param,
      _pageNum, _pageSize, _save_to, _totalCount) {
  var sql = moduleHandleContext.get("sql");
  return sql.queryPaging(
      _sql,
      _param,
      _pageNum,
      _pageSize,
      _save_to || 'result',
      _totalCount || -1);
};


String.prototype.update = function(_sql, _param, _mcommit) {
  var sql = moduleHandleContext.get("sql");
  return sql.update(_sql, _param, _mcommit || false);
};


String.prototype.updateBatch = function(_sql, _param, _mcommit) {
  var sql = moduleHandleContext.get("sql");
  return sql.updateBatch(_sql, _param, _mcommit || false);
};


String.prototype.connection = function(key_url, user, ps) {
  var sql = moduleHandleContext.get("sql");

  switch (arguments.length) {
    case 0:
      return sql.connection();
    case 1:
      return sql.connection(key_url);
    case 3:
      return sql.connection(key_url, user, ps);
    default:
      throw new Error("bad arguments call sql.connection");
  }
};


})();