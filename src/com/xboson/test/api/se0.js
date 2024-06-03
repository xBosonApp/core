//
// 该文件配合 TestApi.java
// org : a297dfacd7a84eab9656675f61750078
// app : a9943b0fb1e141b3a3ce7e886d407f5b
// mod : test_double
//
console.log(">>>> ----------------- Test se Functions.", se);
var assert = require("assert");


var pwd = se.encodePlatformPassword("jym", "2017-12-07 17:06:00", "1234");
console.log("Password:", pwd);

console.log("_CACHE_REGION_API_", _CACHE_REGION_API_);