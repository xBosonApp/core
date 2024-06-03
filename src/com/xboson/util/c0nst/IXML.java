////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-6-7 下午3:48
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/c0nst/IXML.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util.c0nst;

public interface IXML {

  String PF_SOAP_TAG    = "soap:";

  String NS             = "xmlns";
  String NS_PF          = NS +":";

  String NS_XSI         = "xmlns:xsi";
  String NS_XSI_URI     = "http://www.w3.org/2001/XMLSchema-instance";

  String NS_XSD         = "xmlns:xsd";
  String NS_XSD_URI     = "http://www.w3.org/2001/XMLSchema";

  String NS_SOAP        = "xmlns:soap";
  String NS_SOAP_URI    = "http://schemas.xmlsoap.org/soap/envelope/";
  String NS_SOAP12_URI  = "http://www.w3.org/2003/05/soap-envelope";

  String TAG_S_ENVELOPE = PF_SOAP_TAG +"Envelope";
  String TAG_S_BODY     = PF_SOAP_TAG +"Body";
  String TAG_S_HEADER   = PF_SOAP_TAG +"Header";
  String TAG_S_FAULT    = PF_SOAP_TAG +"Fault";

  String XML_HEAD       = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

}
