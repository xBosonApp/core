////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-13 下午7:34
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestBC.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.been.JsonHelper;
import com.xboson.been.XBosonException;
import com.xboson.chain.*;
import com.xboson.chain.witness.ConsensusParser;
import com.xboson.chain.witness.IConsensusUnit;
import com.xboson.db.analyze.ParseException;
import com.xboson.rpc.ClusterManager;
import com.xboson.rpc.RpcFactory;
import com.xboson.util.Tool;
import org.apache.commons.codec.binary.Hex;
import org.mapdb.DBException;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;


public class TestBC extends Test {

  private ConsensusParser cp;
  private final static String userid = "admin-pl";


  public static void main(String[] av) {
    CLUSTER_NODE_ID = 0;
    new TestBC();
  }


  public void test() throws Exception {
    try {
      btc();
      bfs();
      testConsensus();
      testOrder();
      testPeer();
      testBlockJson();
    } catch (DBException.SerializationError e) {
      fail("需要删除区块链文件, 类文件版本已经更新", e);
    }
    TestFace.waitEventLoopEmpty();
  }


  private void testBlockJson() {
    sub("Block TO json");

    BlockBasic bb = genRandomBlock(null);
    Block block = bb.createBlock();
    block.key = Tool.randomBytes(16);
    block.hash = Tool.randomBytes(16);
    block.previousHash = Tool.randomBytes(16);
    block.previousKey = Tool.randomBytes(16);
    block.create = new Date();

    block.pushSign(new SignNode(Tool.randomBytes(15), Tool.randomString(5)));
    block.pushSign(new SignNode(Tool.randomBytes(15), Tool.randomString(5)));
    block.pushSign(new SignNode(Tool.randomBytes(15), Tool.randomString(5)));
    msg(Tool.beautifyJson(Block.class, block));
  }


  public void testConsensus() throws Exception {
    sub("Test Consensus");
    cp = new ConsensusParser((String wid) -> null);
    badConsensus("x");
    badConsensus("one('d')");
    badConsensus("and 'd')");
    badConsensus("and('d'");
    badConsensus("and(('d')");

    goodConsensus(" and(\"d\", or(a, 'b', 'c'))",
            "da", true);
    goodConsensus(" and(\"f1\", or('a', 'b', 'c'))",
            "f1", false);
    goodConsensus(" and(\"t1\", or('f2', 'f3', 't4'))",
            "t1f2f3t4", true);
    goodConsensus(" or(\"f1\", or('f2', 'f3', 'f4'), and('t5', or('f6', 'f7')))",
            "f1f2f3f4t5f6f7", false);
    goodConsensus(" or(\"f1\", or('f2', 'f3', 't4'), and('t5', or('f6', 'f7')))",
            "f1f2f3t4", true);


//    String exp = " or(\"f1\", or('f2', 'f3', 't4'), and('t5', or('f6', 'f7')))";
//    IConsensusUnit cu = cp.parse(exp);
//    String js = Tool.getAdapter(IConsensusUnit.class).toJson(cu);
//    msg(js);
  }


  /**
   * 测试表达式
   * @param exp 共识表达式, 其中 id 的第一个字母, 'f' 返回 false 其他返回 true.
   * @param result 结果集列表, 将经过的 id 按顺序连接在一起
   */
  private void goodConsensus(String exp, String result, boolean r) {
    msg("check", exp, "=>", result);
    IConsensusUnit cu = cp.parse(exp);
    StringBuilder ret = new StringBuilder();

    boolean ar = cu.doAction((String id, PublicKey key, Block b) -> {
        ret.append(id);
        return id.charAt(0) != 'f';
    }, null);

    eq(r, ar, "result");
    eq(result, ret.toString(), "bad");
  }


  /**
   * 一定会抛出解析错误的表达式
   */
  private void badConsensus(String s) {
    try {
      cp.parse(s);
      fail("Cannot get exception:", s);
    } catch (ParseException e) {
      success(e.getMessage());
    }
  }


  private BlockBasic genRandomBlock(byte[] chaincodeKey) {
    if (chaincodeKey == null)
      chaincodeKey = Tool.randomBytes(10);

    BlockBasic ret = new BlockBasic(
            Tool.randomBytes(20),
            userid,
            chaincodeKey
    );
    return ret;
  }


  public void testOrder() throws Exception {
    sub("Test Order");

    String chainName = "t3";
    String channelName = "c00001";
    KeyPair[] kp = genKeys();
    IPeer o = PeerFactory.me().peer();

    if (! o.channelExists(chainName, channelName)) {
      o.createChannel(chainName, channelName, userid, null, kp);
    }
    BlockBasic b0 = genRandomBlock(null);
    byte[] k0 = o.sendBlock(chainName, channelName, b0);
    Block r0 = o.search(chainName, channelName, k0);
    msg(r0);
  }


  private KeyPair[] genKeys() throws Exception {
    KeyPair[] ret = new KeyPair[ITypes.LENGTH];
    for (int i=0; i<ret.length; ++i) {
      ret[i] = Btc.genRandomKeyPair();
    }
    return ret;
  }


  public void testPeer() throws Exception {
    sub("Test Peer");

    try {
      final String chain0 = "t2";
      final String ch0 = "c0";
      final String RPC = "test.block.order";

      String nodeid = ClusterManager.me().localNodeID();
      Order o = new Order();
      RpcFactory.me().bind(o, RPC);
      Peer p  = new Peer(() -> (IPeer) RpcFactory.me().lookup(nodeid, RPC));


      try {
        p.createChannel(chain0, ch0, userid, null, genKeys());
      } catch (Exception e) {
        msg(e.getMessage());
      }

      BlockBasic b0 = new BlockBasic(Tool.randomBytes(10),
              "u1", "api", "0");
      byte[] k0 = p.sendBlock(chain0, ch0, b0);

      BlockBasic bc0 = o.search(chain0, ch0, k0);
      ok(Arrays.equals(bc0.getData(), b0.getData()), "data");

      String[] chains = o.allChainNames();
      for (String chain : chains) {
        String[] channels = o.allChannelNames(chain);
        for (String channel : channels) {
          msg("Channel:", channel, ", Chain:", chain);
        }
      }
    } catch (XBosonException.Remote e) {
      warn("这个测试可能引起 RPC 异常, 因为远程对象在 PeerFactory 中注册过", e);
    }
  }


  public void btc() throws Exception {
    sub("BTC wallet");

    Btc btc = new Btc();

    byte[] publicKey  = btc.publicKey();
    byte[] privateKey = btc.privateKey();

    msg("public key", Hex.encodeHexString(publicKey), publicKey.length);
    msg("private key", Hex.encodeHexString(privateKey), privateKey.length);
    msg("bitcoinAddress", btc.wallet());

    String s = Base58.encode(privateKey);
    byte[] t = Base58.decode(s);
    // msg(Arrays.toString(privateKey), s, Arrays.toString(t));
    ok(Arrays.equals(t, privateKey), "base 58 codec");

    String pubs = btc.publicKeyStr();
    String pris = btc.privateKeyStr();
    byte[] pri = Btc.privateKey(pris).getEncoded();
    byte[] pub = Btc.publicKey(pubs).getEncoded();

    ok(Arrays.equals(pri, privateKey), "private key encode");
    ok(Arrays.equals(pub, publicKey), "public key encode");
    msg("Public KEY string:", pubs, '[', pubs.length(), ']');
    msg("Private KEY string:", pris, '[', pris.length(), ']');
  }


  public void bfs() {
    sub("block file system");

    ISigner signer = new AbsPeer.NoneSigner();
    BlockFileSystem bc = BlockFileSystem.me();
    BlockFileSystem.InnerChain chain = bc.getChain("test");

    try {
      chain.createChannel("ch0", signer, "user");
    } catch (Exception e) {
      msg(e.getMessage());
    }

    BlockFileSystem.InnerChannel ch = chain.openChannel("ch0");
    BlockBasic b0 = new BlockBasic(Tool.randomBytes(10),
            userid, "/api", "0");

    byte[] b0key = null;

    try {
      b0key = ch.push(b0);
    } catch(VerifyException ve) {
      msg(ve.getMessage());
    }

    byte[] oldworld = ch.worldState();
    Block pre = ch.search(ch.lastBlockKey());
    BlockBasic bb = genRandomBlock(b0key);
    byte[] key = ch.push(bb);
    msg("push", Arrays.toString(key), ch.size());
    chain.commit();

    Block b = ch.search(key);
    ok(Arrays.equals(bb.getData(), b.getData()), "data field");
    byte[] world = ch.lastBlockKey();
    ok(Arrays.equals(b.key, world), "world 1");
    ok(Arrays.equals(b.previousHash, oldworld), "world 2");
    ok(Arrays.equals(b.previousKey, pre.key), "previous key");
    ok(Arrays.equals(b.previousHash, pre.hash), "previous hash");

    msg("search", JsonHelper.toJSON(b));
    msg("world", JsonHelper.toJSON(ch.worldState()));
    msg("last-block", JsonHelper.toJSON(world));

    chain.close();
  }


  /**
   * 启动一个独立进程用于集群测试
   */
  public static class TestClient extends Test {
    public static void main(String[] av) {
      new TestClient();
    }
    public void test() throws Exception {
      sub("start client process");
      Thread t = new Thread() {
        public void run() {
          sub("Process start...");
          for (;;) {
            Tool.sleep(1000);
          }
        }
      };
      t.start();
    }
  }
}
