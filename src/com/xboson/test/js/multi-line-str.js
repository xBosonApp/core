////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年12月26日 11:26
// 原始文件路径: xBoson/src/com/xboson/test/js/multi-line.str.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var assert = require('assert');

//
// fix`var str = ""\s+\+ "\\nl1,"\s+\+ "\\nl2,"\s+\+ "\\nl3,"\s+\+ "\\nl4,"\s+\+ "\\nend"`>>
//
var str = `
l1,
l2,
l3,
l4,
end`;

console.log(str);
assert.eq("\nl1,\nl2,\nl3,\nl4,\nend", str, "multi-line-string");

//
// fix`var s1\s= "abc`def`xxx"`>>
//
var s1 = "abc`def`xxx";
console.log(s1);
assert.eq("abc`def`xxx", s1);

//
// fix`var s2\s=\s"xxjj"`>>
//
var s2 = `xxjj`;
assert.eq('xxjj', s2);

//
// fix`var s3\s=\s"hello `world`"`>>
//
var s3 = `hello \`world\``

// ----------------------------------------------------------
//
// fix`var s = ""\s+\+ "\\n  SELECT`>>
//
function getFromDB(typecd) {
  var s = `
  SELECT
      dictcd id,
      dictnm name,
      version,
      CONCAT(dictnm, '(', IFNULL(shortkey, ''), ')') text
  FROM
      `+ orgid +`sys_mdm002 t2
  WHERE
      t2.version = (SELECT
              version
          FROM
              `+ orgid +`sys_mdm001 t1
          WHERE
              t1.typecd = t2.typecd)
  	and t2.typecd = ?
  `;

  sql.query(s, [typecd], "_children");
  var _children = sys.result._children;
  se.setCache(_CACHE_REGION_MDM_, orgid +":"+ typecd, _children, 0);
  return _children;
}




