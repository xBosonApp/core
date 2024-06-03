////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-2-1 下午12:07
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestFabric.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.util.ECDSA;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;


public class TestFabric extends Test {

  private HFClient client;

  /**
   * https://stackoverflow.com/questions/44585909/hyperledger-java-sdk-working-example
   * https://github.com/hyperledger/fabric-sdk-java
   * https://github.com/hyperledger/fabric-samples
   */
  public void test() throws Throwable {
    try {
      connect();
    } catch(Exception e) {
      warn("Cannot found Fabric server", e);
    }
  }


  private void connect() throws Exception {
    sub("Connect to server");
    CryptoSuite cs = CryptoSuite.Factory.getCryptoSuite();
    client = HFClient.createNewInstance();
    client.setCryptoSuite(cs);
    client.setUserContext(new FabricUser());

    Peer p = client.newPeer(
            "peer", "grpc://10.0.0.7:7051");
    Channel channel = client.newChannel("mychannel");
    Orderer ord = client.newOrderer(
            "OA", "grpc://10.0.0.7:7050");
    channel.addOrderer(ord);
    channel.addPeer(p);
    channel.initialize();
    msg("Fabric ok", p, channel);

    queryFabcar(channel, "CAR1");
  }


  private void queryFabcar(Channel channel, String key) throws Exception {
    sub("Querying for", key);

    QueryByChaincodeRequest req = client.newQueryProposalRequest();
    ChaincodeID cid = ChaincodeID.newBuilder().setName("fabcar").build();
    req.setChaincodeID(cid);
    req.setFcn("queryCar");
    req.setArgs(new String[] { key });
    Collection<ProposalResponse> resps = channel.queryByChaincode(req);

    for (ProposalResponse resp : resps) {
      String payload = new String(resp.getChaincodeActionResponsePayload());
      msg("response: " + payload);
    }
  }



  public static void main(String[] a) throws Throwable {
    new TestFabric();
  }


  public static class FabricUser implements User, Enrollment {

    public FabricUser() throws IOException, GeneralSecurityException {
    }

    @Override
    public String getName() {
      return "user1";
    }


    @Override
    public Set<String> getRoles() {
      return Collections.emptySet();
    }


    @Override
    public String getAccount() {
      return null;
    }


    @Override
    public String getAffiliation() {
      return null;
    }


    @Override
    public Enrollment getEnrollment() {
      return this;
    }


    @Override
    public String getMspId() {
      return "Org1MSP";
    }


    @Override
    public PrivateKey getKey() {
      return ECDSA.me().parsePrivateKey(key);
    }


    @Override
    public String getCert() {
      return cert;
    }
  }


  private final static String cert = "-----BEGIN CERTIFICATE-----\n" +
          "MIICjzCCAjWgAwIBAgIUDBfrKy2R7Vi4v59hHoaR5Y4nniwwCgYIKoZIzj0EAwIw\n" +
          "czELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNh\n" +
          "biBGcmFuY2lzY28xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMT\n" +
          "E2NhLm9yZzEuZXhhbXBsZS5jb20wHhcNMTgwMjAxMDkwMzAwWhcNMTkwMjAxMDkw\n" +
          "ODAwWjBCMTAwDQYDVQQLEwZjbGllbnQwCwYDVQQLEwRvcmcxMBIGA1UECxMLZGVw\n" +
          "YXJ0bWVudDExDjAMBgNVBAMTBXVzZXIxMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcD\n" +
          "QgAEoAhOmOixnDx+XzReX2Zh5UzgqvqBbRJ5e486r4KXtt28JGyOQySQtXjJBeBD\n" +
          "yCvRgepEgHZ12WRmgjO4wDiKWaOB1zCB1DAOBgNVHQ8BAf8EBAMCB4AwDAYDVR0T\n" +
          "AQH/BAIwADAdBgNVHQ4EFgQUP2j/ub/qtY0LWnfsPy1Tkq+dH4QwKwYDVR0jBCQw\n" +
          "IoAgQjmqDc122u64ugzacBhR0UUE0xqtGy3d26xqVzZeSXwwaAYIKgMEBQYHCAEE\n" +
          "XHsiYXR0cnMiOnsiaGYuQWZmaWxpYXRpb24iOiJvcmcxLmRlcGFydG1lbnQxIiwi\n" +
          "aGYuRW5yb2xsbWVudElEIjoidXNlcjEiLCJoZi5UeXBlIjoiY2xpZW50In19MAoG\n" +
          "CCqGSM49BAMCA0gAMEUCIQCkRhlYr1EkjfBeLFFuQN70zwmGliUdlpC35g0Q+ITB\n" +
          "QQIgOU6XdxRuqq5F5XKuz+YioqW8NUjtGKc39RmzDyI9suw=\n" +
          "-----END CERTIFICATE-----";

  private final static String key = "-----BEGIN PRIVATE KEY-----\n" +
          "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgI3lxkVeWEahFcPn/\n" +
          "OtrRu2ybyF6LMb5w+91bmEMcNQ2hRANCAASgCE6Y6LGcPH5fNF5fZmHlTOCq+oFt\n" +
          "Enl7jzqvgpe23bwkbI5DJJC1eMkF4EPIK9GB6kSAdnXZZGaCM7jAOIpZ\n" +
          "-----END PRIVATE KEY-----";
}
