////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2018年 6月 5日 15:04
// 原始文件路径: xBoson/src/com/xboson/test/js/check-stream.js
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

var if_throw_ok = require("./util.js").if_throw_ok;
var assert = require("assert");

var stream = require('streamutil');
var strstream = stream.openStringBufferOutputStream();
var b64 = stream.openBase64OutputStream(strstream);
var gzip = stream.openGzipOutputStream(b64);

gzip.write(getXmlStr());
gzip.flush();

console.log("Xml TO BASE64:", strstream.toString());


var is = stream.openStringInputStream(getbase64str());
var ib = stream.openBase64InputStream(is);
var ig = stream.openGzipInputStream(ib);
var line = stream.openLineReader(ig);

console.log("Base64 TO Xml:");
var s;
while (s = line.readLine()) {
  console.log(s);
}


console.log("ok");



function getXmlStr() {
return `<dmp>
  <datasets>
    <!--以资源【门急诊费用】举例:包含两个数据集：门诊费用主表、门诊费用明细 -->
    <!--交换标准包含的第一个数据集编码-->
    <setcode>门诊费用主表编码</setcode>
    <settype/>
    <setdetails>
      <!-- 以下内容为管理字段 -->
      <SERIALNUM_ID/>
      <TASK_ID />
      <BATCH_NUM />
      <LOCAL_ID>唯一标识一个居民/患者的ID编码</LOCAL_ID>
      <BUSINESS_ID>针对不同业务具体的流水号字段：如：门诊流水号</BUSINESS_ID>
      <BASIC_ACTIVE_ID>能在业务系统的数据表中唯一标识出一条记录，同时也能够体现出主从关系</BASIC_ACTIVE_ID>
      <DATAGENERATE_DATE>本业务数据实际产生的时间【YYYYMMDDHH24MISS】</DATAGENERATE_DATE>
      <ORGANIZATION_CODE>数据所属医疗机构的22位机构编码</ORGANIZATION_CODE>
      <ORGANIZATION_NAME>机构名称</ORGANIZATION_NAME>
      <DOMAIN_CODE/>
      <CREATE_DATE/>
      <UPDATE_DATE/>
      <ARCHIVE_DATE/>
      <RECORD_IDEN/>
      <!-- 以下内容为业务字段，请以实际发布交换标准内容为主 -->
      <WS02_01_039_001>王梅</WS02_01_039_001>
      <WS02_01_040_01>0</WS02_01_040_01>
      ...省略其余业务字段...
    </setdetails>
    <setdetails>第2-N条费用主表记录</setdetails>
  </datasets>
  <datasets>
    <!--交换标准包含的第二个数据集编码-->
    <setcode>门诊费用明细编码</setcode>
    <settype/>
    <setdetails>
      <!-- 以下内容为管理字段 -->
      <SERIALNUM_ID/>
      <TASK_ID />
      <BATCH_NUM />
      <LOCAL_ID>唯一标识一个居民/患者的ID编码</LOCAL_ID>
      <BUSINESS_ID>针对不同业务具体的流水号字段：如：门诊流水号</BUSINESS_ID>
      <BASIC_ACTIVE_ID>能在业务系统的数据表中唯一标识出一条记录，同时也能够体现出主从关系</BASIC_ACTIVE_ID>
      <DATAGENERATE_DATE>本业务数据实际产生的时间【YYYYMMDDHH24MISS】</DATAGENERATE_DATE>
      <ORGANIZATION_CODE>数据所属医疗机构的22位机构编码</ORGANIZATION_CODE>
      <ORGANIZATION_NAME>机构名称</ORGANIZATION_NAME>
      <DOMAIN_CODE/>
      <CREATE_DATE/>
      <UPDATE_DATE/>
      <ARCHIVE_DATE/>
      <RECORD_IDEN/>
      <!-- 以下内容为业务字段，请以实际发布交换标准内容为主 -->
      <WS02_01_039_001>王梅</WS02_01_039_001>
      <WS02_01_040_01>0</WS02_01_040_01>
      ...省略其余业务字段...
    </setdetails>
    <setdetails>第2-N条费用明细记录</setdetails>
  </datasets>
</dmp>`
}


function getbase64str() {
  return "H4sIAAAAAAAAAO1WXU8aQRR9tr+C/gBcpL602ZBs2Y1sKkvDYpv2ZWMKD03a1" +
         "ERe+gbqYlsBsVWplqpYraZVwPi1suKfcT6WJ/9C7+4suEJN2nd5YebOuWfOnblzs" +
         "nzy7UTo3gCfHE+PT6bSkzAe4O/7/cjcto5nSLN0mSm1y7sks23VP1lHZ3Rx9zKzg" +
         "IwLdDH3COd1XPqNjC1k/CJLDVKotb/lrs5XIaGLRoZpVSEn6w2Sr0Vq5nx+f3e75" +
         "hYpbJKNWTybY6x0dYbu7SEj4+Wm58t0I+umgdxX75KpUP9uDMZzHYSLTr+fSHGdS" +
         "TKVHn/9xqnXUeCDipExh3M6rp0ho0lrVVrK4f0yqR27Qgd4VYrLwqgyFtVkkQv5n" +
         "FhCUJ/A1NeZPxYS4YgGmG5kNBYWRgESwot1KAiqtOo5Vhk+0EljgSNTP6yMDiXLY" +
         "kd7N8llHVNlRVJVO9L+/AHXQWMBl/LIWMWfqlg/Ra0vkE+Os6RxhOdPmXC4C/xzq" +
         "nsj3VWe8/J1dKtyWBPCCfmZZEet6Rau7DJ+emhSc93md24CjhgZ+95q8GzTHn+vW" +
         "rUGbi1dnedBGymfoLN1m2dr3ZZXbDgwE5lFrB8CJ8jo2ZRJEYWEMCIpUlxISBpMp" +
         "BCp7DElTACurbVXdNTcoYuOqvJJu3wEnfoCftGoKEYiweGorKrQqjzXz8Z2icVHB" +
         "EV+KSTkmKKFYyLs4pCTjxl8sIbzJl0uk0qTrM3AFsEgahXcmXtD/fl/4VWEqK3ez" +
         "sOlAt1p9OQ5627VsaggM6ZO54TjUkc0xzpw7Kl4HWEgIR6O2MfnjcWlcCwuwolKC" +
         "ndri7u943ZK3qqfAoAdLZ5fwMb0jVd5nWV238NzNRDUAkNa4MFDLRAYCtHiHNnUe" +
         "a43fhM8HIC/UMADYxEbNTg4SCtZurSN9RPUWvFKhCX77XI3H6/3LYNhBP2K3YUeM" +
         "2Ad2ZPGcx7D6ze/29yomf8vN2I2d+dGd25050Z3bvRvbgRj+CL7A5Yn+V2XCQAA"
}