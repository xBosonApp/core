//
// 该文件配合 TestApi.java
// org : a297dfacd7a84eab9656675f61750078
// app : a9943b0fb1e141b3a3ce7e886d407f5b
// mod : test_double
//
console.log(">>>> ---------------------------------- Test list Functions.", list);
var assert = require("assert");

var r = list.range([1,2,3,4,5,6,7], 1, 5);
assert.deepEqual([2,3,4,5], r, "list.range");
var r = list.range([{aa:1,bb:2}, {aa:1}], 1, 2);
assert.deepEqual([{aa:1}], r, "list.range");


var r = list.remove([1, 2, 3], 1);
assert.deepEqual([2, 3], r, "list.remove");
var r = list.remove([{aa:1, bb:2}, {aa:1}], {aa:1});
assert.deepEqual([{aa:1, bb:2}], r, "list.remove");
console.log("list.remove", JSON.stringify(r))


assert.deepEqual([1, 3], list.removeAt([1,2,3], 1), "list.removeAt");
assert.deepEqual([{aa:1, bb:2}], list.removeAt([{aa:1, bb:2}, {aa:1}], 1), "list.removeAt");

assert.deepEqual([1,2,3,5], list.add([1,2,3], 5), "list.add");
assert.deepEqual([{aa:1,bb:2},{aa:1}], list.add([{aa:1,bb:2}], {aa:1}), "list.add");

assert.deepEqual([1,2,3,6,4,5], list.addAt([1,2,3,4,5],6,3), "list.addAt");
assert.deepEqual([{aa:1,bb:2},{cc:3}], list.addAt([{aa:1,bb:2}], {cc:3}, 1), "list.addAt");

assert.deepEqual([1,2,3,4,5], list.addAll([1,2,3],[4,5]), "list.addAll");
assert.deepEqual([{aa:1,bb:2},{cc:3}], list.addAll([{aa:1,bb:2}],[{cc:3}]), "list.addAll");


assert(list.contain([1,2,3],1), "list.contain");
assert(list.contain([{aa:1,bb:2},{aa:1}],{aa:1}), "list.contain({}) not implements");

assert.deepEqual([3,1,2], list.reverse([2,1,3]), "list.reverse");
assert.deepEqual([{a:3},{a:1},{a:2}], list.reverse([{a:2},{a:1},{a:3}]), "list.reverse");
assert.eq("1,2,3", list.toString([1,2,3],","), "list.toString");


var ret=list.sort([1,3,5,2,4],null);
assert.deepEqual([1,2,3,4,5], ret, "list.sort 1");
console.log('1:', JSON.stringify(ret));

var ret=list.sort([{a:2},{a:1},{a:3}],"a","0");
assert.deepEqual([{a:1},{a:2},{a:3}], ret, "list.sort 2");
console.log('2:', JSON.stringify(ret));

var ret=list.sort([{a:2},{a:1},{a:3}],"a","1");
assert.deepEqual([{a:3},{a:2},{a:1}], ret, "list.sort 3");
console.log('3:', JSON.stringify(ret));

var ret=list.sort([{a:2,b:1},{a:1,b:4},{a:3,b:3},{a:3,b:2}],"a","0","b","0");
assert.deepEqual([{a:1,b:4},{a:2,b:1},{a:3,b:2},{a:3,b:3}], ret, "list.sort 4");
console.log('4:', JSON.stringify(ret));

var ret=list.sort([{a:2,b:1},{a:1,b:4},{a:3,b:3},{a:2,b:2}],"a","0","b","1");
assert.deepEqual([{a:1,b:4},{a:2,b:2},{a:2,b:1},{a:3,b:3}], ret, "list.sort 5");
console.log('5:', JSON.stringify(ret));