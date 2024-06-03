//
// 该文件配合 TestApi.java
// org : a297dfacd7a84eab9656675f61750078
// app : a9943b0fb1e141b3a3ce7e886d407f5b
// mod : test_double
//
console.log(">>>> ---------------------------------- Test map Functions.", map);
var assert = require("assert");

assert.eq(1, map.get({a:1}, 'a'), "map.get()");
var map0 = map.put({a:1, b:2}, 'c', 3);
assert.eq(1, map0.a, "map.put");
assert.eq(3, map0.c, "map.put");

var all = map.putAll({a:1, b:2}, {c:3});
assert.eq(3, all.c, "map.putAll");
assert.eq(1, all.a, "map.putAll");

var r = map.remove({a:1, b:2}, "a");
assert(!r.a, 'map.remove');
assert(r.b==2, "map.remove");

assert(map.containsKey({a:1, b:2}, 'a'), "map.containsKey");
assert.eq(2, map.size({a:10, b:20}), "map.size");