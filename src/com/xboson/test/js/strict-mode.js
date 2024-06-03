"use strict";

var list=[], list2={};

//
// fix`for \(var n in list\) \{`
//
for (var n in list) {
  var item = list[n];
  console.log(n, item);
}


//
// fix`for \(x in list2\) \{`
//
for (x in list2) {
  var obj = list2[x];
}