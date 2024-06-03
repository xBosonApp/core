//
// 该文件配合 TestApi.java
// org : a297dfacd7a84eab9656675f61750078
// app : a9943b0fb1e141b3a3ce7e886d407f5b
// mod : test_double
//
var assert = require('assert');

console.warn("\n\n<--- Script From DS platform, Run in xBoson ------------------------------------------------->");


var c = sql.query("SELECT * FROM a297dfacd7a84eab9656675f61750078.sys_bm001 limit ?", [10], "bm");
console.log("sql.query:", c, sys.jsonFromInstance(sys.result.bm));


var d = sql.currentDBTimeString();
console.log("sql.currentDBTimeString", d);


sql.connection('f3f8b967bd664673a12c3823b007b1a8');
sql.query("Select * from bm_list limit ?", [3], 'bm2');
console.log("sql.connection(key)", sys.jsonFromInstance(sys.result.bm2));


sql.connection("jdbc:mysql://localhost:3306/test", "root", "root");
sql.query("Select * from bm_list limit ?", [2], 'bm3');
console.log("sql.connection(key)", sys.jsonFromInstance(sys.result.bm3));
console.log("sql.dbType()", sql.dbType());


//sql.connection("jdbc:oracle:thin:localhost:1521:test", "root", "root");
sql.connection();
console.log("sql.dbType()", sql.dbType());

var meta = sql.metaData("select * from sys_pl_drm_ds001");
console.log("sql.metaData()", JSON.stringify(meta));


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// All Over
//
console.warn("\n\n<---- <<<<< OK >>>>> Run in xBoson ---------------------------------------------------------->");
//
// All Over
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////