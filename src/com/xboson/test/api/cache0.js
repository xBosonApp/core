//
// 该文件配合 TestApi.java
// org : a297dfacd7a84eab9656675f61750078
// app : a9943b0fb1e141b3a3ce7e886d407f5b
// mod : test_double
//
console.log(">>>> ----------------- Test cache Functions.", cache);

var assert = require("assert");

var d = [["a"], ["b"], sys.randomString(10)];
cache.set("regionName", "keyName", d);
var v = cache.get("regionName", "keyName");

assert.deepEqual(d, v, "set/get");
console.log(JSON.stringify(v));
console.log("cache ok");

cache.del("regionName", "keyName");
var n = cache.get("regionName", "keyName");
assert(n == null, "del");
console.log("NULL", n);


cache.set("regionName", "k1", 1);
cache.set("regionName", "k2", 2);
cache.delAll("regionName", ['k1', 'k2']);
assert(cache.get("regionName", "k1")==null, "delAll");
assert(cache.get("regionName", "k1")==null, "delAll");


console.log("cache model ok");