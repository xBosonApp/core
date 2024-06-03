////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-30 下午5:17
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/service/Witness.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.service;

import com.xboson.auth.IAResource;
import com.xboson.auth.IAWho;
import com.xboson.auth.PermissionSystem;
import com.xboson.auth.impl.LicenseAuthorizationRating;
import com.xboson.been.CallData;
import com.xboson.been.LoginUser;
import com.xboson.been.XBosonException;
import com.xboson.chain.Block;
import com.xboson.chain.Btc;
import com.xboson.chain.IPeer;
import com.xboson.chain.PeerFactory;
import com.xboson.chain.witness.SignerProxy;
import com.xboson.chain.witness.WitnessConnect;
import com.xboson.chain.witness.WitnessFactory;
import com.xboson.db.ConnectConfig;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.event.EventLoop;
import com.xboson.j2ee.container.XPath;
import com.xboson.j2ee.container.XService;
import com.xboson.util.Hex;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import com.xboson.util.c0nst.IConstant;

import java.io.IOException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;


@XPath("/witness")
public class Witness extends XService implements IConstant, IAResource {

  private static final String ALGORITHM      = SignerProxy.SIGNER_ALGORITHM;
  private static final int GEN_RAND_DATA_LEN = 256;

  private final ConnectConfig db;
  private final IAWho anonymous;


  public Witness() {
    db = SysConfig.me().readConfig().db;
    anonymous = new LoginUser();
  }


  @Override
  public void service(CallData data) throws Exception {
    PermissionSystem.apply(anonymous, LicenseAuthorizationRating.class, this);
    subService(data, "Cannot found service");
  }


  public void register(CallData data) throws Exception {
    String algorithm = data.getString("algorithm", 1, 45);
    if (!ALGORITHM.equals(algorithm)) {
      data.xres.responseMsg("bad algorithm value", 2);
      return;
    }

    String pubkey = data.getString("publickey", 1, 999);
    String host   = data.getString("host", 1, 128);
    int port      = data.getInt("port", 1, 65535);
    String prefix = data.getString("urlperfix", 0, 128);

    WitnessConnect wc = new WitnessConnect(host, port, prefix);
    PublicKey pk = Btc.publicKey(Hex.Names.BASE64, pubkey);
    SignerProxy sp = new SignerProxy(pk, wc);

    if (verifyRemote(data, sp)) {
      String id = insertDB(host, port, pubkey, prefix);
      data.xres.bindResponse("id", id);
      data.xres.responseMsg("ok", 0);
    }
  }


  public void change(CallData data) throws Exception {
    String id     = data.getString("id", 1, 45);
    String host   = data.getString("host", 1, 128);
    int port      = data.getInt("port", 1, 65535);
    String prefix = data.getString("urlperfix", 0, 128);

    WitnessConnect wc = new WitnessConnect(host, port, prefix);
    PublicKey pk = getPubKey(id);
    if (pk == null) {
      data.xres.responseMsg("Cannot found witness: "+ id, 11);
      return;
    }

    SignerProxy sp = new SignerProxy(pk, wc);
    if (verifyRemote(data, sp)) {
      if (updateDB(id, host, port, prefix)) {
        WitnessFactory.me().update(id);
        data.xres.responseMsg("ok", 0);
      } else {
        data.xres.responseMsg("Cannot found witness: "+ id, 11);
      }
    }
  }


  public void reqb(CallData data) throws Exception {
    String wid     = data.getString("id", 1, 45);
    String chain   = data.getString("chain", 1, 99);
    String channel = data.getString("channel", 1, 45);
    String begin   = data.getString("begin", 1, 45);
    String end     = data.getString("end", 1, 45);

    // 检查区块链是否存在
    IPeer peer = PeerFactory.me().peer();
    if (! peer.channelExists(chain, channel)) {
      data.xres.responseMsg("Chain or Channel not exists", 11);
      return;
    }

    // 检查见证者是否有资格获取区块
    PublicKey pk = peer.getWitnessPublicKey(chain, channel, wid);
    if (pk == null) {
      data.xres.responseMsg("Witnesses not access the blockchain", 1100);
      return;
    }

    // 连接/验证 见证者
    WitnessConnect conn = WitnessFactory.me().openConnection(wid);
    SignerProxy sp = new SignerProxy(pk, conn);
    if (! verifyRemote(data, sp)) {
      return;
    }

    // 检查开始区块
    Block beginb = peer.search(chain, channel, Hex.decode64(begin));
    if (beginb == null) {
      data.xres.responseMsg("Block not found "+ begin, 11);
      return;
    }

    // 尝试确定结束区块
    byte[] endkey = null;
    if (Tool.notNulStr(end)) {
      endkey = Hex.decode64(end);
    }
    if (endkey == null) {
      endkey = peer.genesisKey(chain, channel);
    }

    sendBlockTo(conn, beginb, endkey, chain, channel);
    data.xres.responseMsg("ok", 0);
  }


  /**
   * 异步方式发送指定范围的区块到见证者
   */
  private void sendBlockTo(WitnessConnect conn, Block begin, byte[] endkey,
                           String chain, String channel) {
    EventLoop.me().add(() -> {
      try {
        IPeer peer = PeerFactory.me().peer();
        Block b = begin;
        int count = 0;

        for (;;) {
          if (! conn.doDeliver(b, chain, channel)) {
            log.warn("Send block fail", b);
            return;
          }
          ++count;

          if (Arrays.equals(b.key, endkey) || b.previousKey.length == 0)
            break;

          b = peer.search(chain, channel, b.previousKey);
          if (b == null)
            break;
        }
        log.debug("Send All(", count, ") Blocks to witness");
      } catch (Exception e) {
        log.error("Method request block (reqb)", e.getMessage());
      }
    });
  }


  private boolean verifyRemote(CallData data, SignerProxy sp) throws IOException {
    try {
      byte[] rand = Tool.randomBytes(GEN_RAND_DATA_LEN);
      byte[] sign = sp.sign(rand);
      if (! sp.verify(rand, sign)) {
        data.xres.responseMsg("Signature verification failed", 3);
        return false;
      }
      return true;
    } catch (IOException e) {
      data.xres.responseMsg("Network unreachable,"+ e.getMessage(), 4);

    } catch (XBosonException e) {
      data.xres.responseMsg("Error: "+ e.getMessage(), 5);
    }
    return false;
  }


  private PublicKey getPubKey(String id) {
    String sql = "open_witness_pk.sql";
    Object[] parm = { id };

    try (SqlResult sr = SqlReader.query(sql, db, parm)) {
      ResultSet rs = sr.getResult();
      if (rs.next()) {
        String key = rs.getString(1);
        return Btc.publicKey(Hex.Names.BASE64, key);
      }
      return null;
    } catch (SQLException e) {
      throw new XBosonException(e);
    }
  }


  private String insertDB(String host, int port, String pubkey, String prefix) {
    String wnid = Tool.uuid.ds();
    Object[] parm = { wnid, host, port, pubkey, prefix, ALGORITHM };
    String sql = "create_witness.sql";

    try (SqlResult sr = SqlReader.query(sql, db, parm)) {
      return wnid;
    }
  }


  private boolean updateDB(String id, String host, int port, String prefix) {
    Object[] parm = { host, port, prefix, id };
    String sql = "change_witness.sql";

    try (SqlResult sr = SqlReader.query(sql, db, parm)) {
      return sr.getUpdateCount() > 0;
    }
  }


  @Override
  public boolean needLogin() {
    return false;
  }


  @Override
  public String logName() {
    return "bc-witness-service";
  }


  @Override
  public String description() {
    return "app.module.chain.peer.platform()";
  }

}
