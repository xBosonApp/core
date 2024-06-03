////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-5-9 下午7:32
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestFabircShim.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;
//
//import io.grpc.ManagedChannel;
//import org.apache.log4j.BasicConfigurator;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.hyperledger.fabric.shim.ChaincodeBase;
//import org.hyperledger.fabric.shim.ChaincodeStub;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.security.KeyFactory;
//
//
//public class TestFabircShim {
//
//  public static void main(String[] args) throws Throwable {
//    BasicConfigurator.configure();
//    // 出错说明缺少 ECDSA jar 包, 或 ECDSA 没有注册到服务
//    KeyFactory kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
//    Test.msg(kf.getAlgorithm());
//
//    Code c = new Code();
//    c.start("cc0", "10.0.0.104:7052");
//  }
//
//
//  public static abstract class ChaincodeNoThread extends ChaincodeBase {
//
//    /**
//     * 该方法不会再线程中启动 chaincode
//     *
//     * @param id chaincode id
//     * @param addr "10.0.0.104:7052" 格式的地址
//     */
//    public void start(String id, String addr) throws
//            NoSuchMethodException,
//            InvocationTargetException,
//            IllegalAccessException {
//
//      Code c = new Code();
//      Method processCommandLineOptions =
//              ChaincodeBase.class.getDeclaredMethod(
//                      "processCommandLineOptions", String[].class);
//
//      processCommandLineOptions.setAccessible(true);
//      Object arg = new String[] { "-i", id, "-a", addr };
//      processCommandLineOptions.invoke(c, arg);
//
//      ManagedChannel connection = c.newPeerClientConnection();
//      c.chatWithPeer(connection);
//    }
//  }
//
//
//  public static class Code extends ChaincodeNoThread {
//
//    @Override
//    public Response init(ChaincodeStub stub) {
//      Test.msg("CC init");
//      return newSuccessResponse();
//    }
//
//
//    @Override
//    public Response invoke(ChaincodeStub stub) {
//      Test.msg("CC invoke");
//      return newSuccessResponse();
//    }
//  }
//}
