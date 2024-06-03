//
// 该文件配合 TestApi.java
// org : a297dfacd7a84eab9656675f61750078
// app : a9943b0fb1e141b3a3ce7e886d407f5b
// mod : test_double
//
console.log(">>>> ----------------- Test http Functions.", http);
var assert = require("assert");

console.log("schema:", http.schema());
console.log("domain:", http.domain());
console.log("port:", http.port());
console.log("method:", http.method());
console.log("headers:", JSON.stringify( http.headers() ));
console.log("uri:", http.uri());


var r = http.get("http://api.weixin.qq.com/cgi-bin/token", {
  grant_type: "client_credential",
  appid: "APPID",
  secret: "APPSECRET"
}, "json");


console.log(JSON.stringify(r, 0, 2));
console.log("http ok");