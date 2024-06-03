////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2018年1月7日 9:11
// 原始文件路径: xBoson/src/com/xboson/test/js/mongo-db.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var assert = require('assert');
var mongodb = require('mongodb');
var client = mongodb.connect("mongodb://localhost");
// var dbs = client.dbs();
// sys.addRetData(dbs, "dbs");

var db = client.db("phone");
var phone = db.collection("phone");
phone.drop();
console.log('drop()');



var hw_meta = {
  _id : 1,
  name: "HuaWi",
  site: "http://www.huawi.com/cn/",
};
phone.insert(hw_meta);
console.log('insert()');

var i_many = [{
  _id  : 2,
  name : "XioMi",
  site : "http://www.xiomi.cn"
}, {
  _id  : 3,
  name : "MeZu",
  site : "http://www.mezu.com"
}];
phone.insertMany(i_many);
console.log('insertMany()');



var hw = phone.find({ name: 'HuaWi' });
assert(hw.length > 0, "find");
assert.eq(hw_meta.name, hw[0].name, "name attribute");
assert.eq(hw_meta.site, hw[0].site, "site attribute");
console.log('find()');

phone.updateOne(
   { _id: 1 },
   {
     $set: { "name": "HuaWi-100", status: "update" },
     $currentDate: { lastModified: true }
   }
);
assert.eq("HuaWi-100", phone.find({_id: 1})[0].name, "change name");
console.log('updateOne()');


phone.insert({ _id:4, name: "samsvng" });
phone.deleteOne({name: "samsvng"});
var kill = phone.find({name: "samsvng"});
assert.eq(0, kill.length, "samsvng is kill");
console.log("insert() / deleteOne()");



var iname = phone.createIndex({name:1});
phone.getIndexes().forEach(function(i) {
  console.log("index:", i.name);
});


client.close();
console.log("Mango client close()");