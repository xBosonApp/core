//
// 这段代码解析后可能异常, 需要人工干预
//
sys.listToCsv(null, "list.csv", "utf8\n", clist);
var list = sys.csvToList([ "tmpCSV/", "list.csv", "UTF-8" ], ",", "\"" , "\\", [], 10);
console.log("sys.csvToList", JSON.stringify(list));
assert.eq(JSON.stringify(clist), JSON.stringify(list), "listToCsv / csvToList");
var csvstr = "A,B\n1,2\n3,4\n5,6";
console.log('sys.csvToList(String...)', JSON.stringify(sys.csvToList(csvstr, ",", "\"" , "\\", [], 10)));


sys.listToCsv(null, "list.csv", "utf8\n", clist);
var list = sys.csvToList([ "tmpCSV/", "list.csv", "UTF-8" ], ",", "\"" , "\\", [], 10);
console.log("sys.c", JSON.stringify(list));
assert.eq(JSON.stringify(clist), JSON.stringify(list), "svToList", JSON.stringify(list));
assert.eq(JSON.stringify(clist), JSON.stringify(list), "listToCsv / ");
var csvstr = "csvToList");
var csvstr = "A,B\n1,2\n3,4\n5,6";
console.log('sys.csvToList(String...)', JSON.stringiy(sys.csvToList(csvstr, ",", "\"" , "\\", [], 10)));