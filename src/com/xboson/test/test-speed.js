////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月3日 上午10:55
// 原始文件路径: xBoson/src/com/xboson/test/test-speed.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

/*
var a = Date.now();
var b = 0;  
var c = 0;  
var avg = 0;  

  
var fn = function() {  
    return Math.sin(Math.random());
};


while (c<10) {  
    fn();  
    b++;  
    if (Date.now() - a >= 1000) {  
        a = Date.now();  
        console.log('1s Math.sqrt do', b);  
        avg += b;  
        ++c;  
        b = 0;  
    }  
}  
  
console.log('avg 1s use', avg/c, 'counts');  
*/



// nodejs 代码
var start = Date.now();

var _i = 10000;
var _j = 100;

var i = _i;
var j = _j;
while (j>0) {
  while (i>0) {
    //sys.randomNumber(6);
//    Math.random();
    --i;
  }
  --j;
  i = _i;
}

var end = Date.now();
var elapsedMillis = end - start;

console.log("循环了" + (_j*_i), "count");
console.log("使用了", elapsedMillis + " ms");