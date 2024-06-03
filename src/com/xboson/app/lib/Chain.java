////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-19 上午11:08
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/Chain.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.xboson.app.AppContext;
import com.xboson.auth.IAResource;
import com.xboson.auth.PermissionSystem;
import com.xboson.auth.impl.LicenseAuthorizationRating;
import com.xboson.been.Config;
import com.xboson.been.LoginUser;
import com.xboson.been.XBosonException;
import com.xboson.chain.*;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.script.lib.Bytes;
import com.xboson.util.CreatorFromUrl;
import com.xboson.util.Hex;
import com.xboson.util.SysConfig;
import com.xboson.util.c0nst.IConstant;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.security.KeyPair;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * 区块链 api
 */
public class Chain extends RuntimeUnitImpl implements IAResource {

  private static final String SQL_OPEN    = "open_chain";
  private static final String SQL_DISABLE = "disable_chain";
  private static final String ID_PREFIX   = "id";
  private static final String NAME_PREFIX = "name";

  public final int TYPE_LENGTH = ITypes.LENGTH;
  public final int GENESIS     = ITypes.GENESIS;

  private CreatorFromUrl<String[]> namesCreator;
  private Config cf;


  public Chain() {
    super(null);
    cf = SysConfig.me().readConfig();
    namesCreator = new CreatorFromUrl<>();

    namesCreator.reg(ID_PREFIX,
            (id, p, u, d) -> getChainConfig(id, null));
    namesCreator.reg(NAME_PREFIX,
            (name, p, u, d) -> getChainConfig(null, name));
  }


  /**
   * 数据库中有记录而磁盘上找不到文件, 会将数据库中的标记为无效.
   */
  public Object open(String exp) throws Exception {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    String[] cf = namesCreator.create(exp);
    if (cf == null) {
      throw new XBosonException("Cannot open chain from [DB]: "+ exp);
    }

    IPeer peer = PeerFactory.me().peer();
    if (! peer.channelExists(cf[0], cf[1])) {
      disableChain(cf[2]);
      throw new XBosonException("Cannot open chain from [FS]: "+ exp);
    }
    return new ChainImpl(cf[0], cf[1], peer);
  }


  public void create(String chain, String channel,
                     String consensusExp, KeyPairJs[] ks) throws Exception
  {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    IPeer peer = PeerFactory.me().peer();
    if (peer.channelExists(chain, channel)) {
      throw new XBosonException("channel/chain exists: "+ channel +'/'+ chain);
    }

    String uid = AppContext.me().who().identification();
    KeyPair[] kps = new KeyPair[ks.length];
    for (int i=0; i<kps.length; ++i) {
      kps[i] = ks[i].toKeyPair();
    }
    peer.createChannel(chain, channel, uid, consensusExp, kps);
  }


  /**
   * 生成公钥/私钥对 对象
   */
  public KeyPairJs generateKeyPair() {
    return new KeyPairJs();
  }


  @Override
  public String description() {
    return "app.module.chain.peer.platform()";
  }


  /**
   * 打开链, 如果链设置的角色属性为空, 则任何人都可以打开该链,
   * 否则只有该角色的用户可以打开链
   * @param chain_id
   * @param name
   * @return [physical_chain, physical_channel]
   */
  private String[] getChainConfig(String chain_id, String name) {
    LoginUser user = (LoginUser) AppContext.me().who();
    Object[] parm = { chain_id, name, user.userid, user.pid };

    try (SqlResult sr = SqlReader.query(SQL_OPEN, cf.db, parm)) {
      ResultSet rs = sr.getResult();
      if (rs.next()) {
        return new String[] {
                rs.getString("physical_chain"),
                rs.getString("physical_channel"),
                rs.getString("chain_id"),
        };
      }
      return null;
    } catch (SQLException e) {
      throw new XBosonException.XSqlException(e);
    }
  }


  private boolean disableChain(String chain_id) {
    Object[] parm = { chain_id };
    try (SqlResult sr = SqlReader.query(SQL_DISABLE, cf.db, parm)) {
      return sr.getUpdateCount() == 1;
    }
  }


  public class ChainImpl {
    private String chain;
    private String channel;
    private IPeer peer;


    private ChainImpl(String chain, String channel, IPeer peer) {
      this.peer    = peer;
      this.chain   = chain;
      this.channel = channel;
    }


    public Bytes genesisKey() throws Exception {
      return new Bytes(peer.genesisKey(chain, channel));
    }


    public Bytes lastBlockKey() throws Exception {
      return new Bytes(peer.lastBlockKey(chain, channel));
    }


    public Bytes worldState() throws Exception {
      return new Bytes(peer.worldState(chain, channel));
    }


    public int size() throws Exception {
      return peer.size(chain, channel);
    }


    public Object search(Bytes k) throws Exception {
      Block b = peer.search(chain, channel, k.bin());
      if (b == null) return null;

      ScriptObjectMirror ret = createJSObject();
      ret.put("key",          k);
      ret.put("hash",         new Bytes(b.hash));
      ret.put("previousKey",  new Bytes(b.previousKey));
      ret.put("previousHash", new Bytes(b.previousHash));
      ret.put("create",       b.create);
      ret.put("userid",       b.getUserId());
      ret.put("type",         b.type);
      ret.put("sign",         wrap(b.sign));

      switch (b.type) {
        case ITypes.CHAINCODE_CONTENT:
          ret.put("apiPath", b.getApiPath());
          ret.put("apiHash", b.getApiHash());
          // no break;

        case ITypes.GENESIS:
          ret.put("data", new Bytes(b.getData()));
          break;

        default:
          ret.put("data", new String(b.getData()));
          ret.put("chaincodeKey", new Bytes(b.getChaincodeKey()));
          break;
      }
      return ret;
    }


    public Object search(String key) throws Exception {
      return search(new Bytes(key));
    }


    public Object push(String data) throws Exception {
      AppContext app  = AppContext.me();
      String aPath    = app.getCurrentApiPath();
      String aHash    = app.getOriginalApiHash();
      String userid   = app.who().identification();
      byte[] cckey    = peer.getChaincodeKey(chain, channel, aPath, aHash);

      if (cckey == null) {
        String codeContent = app.getOriginalApiCode();
        byte[] codeBuf = Hex.parse(codeContent);
        BlockBasic code = new BlockBasic(codeBuf, userid, aPath, aHash);
        code.type = ITypes.CHAINCODE_CONTENT;
        cckey = peer.sendBlock(chain, channel, code);
      }

      BlockBasic block = new BlockBasic(
              data.getBytes(IConstant.CHARSET), userid, cckey);

      byte[] key = peer.sendBlock(chain, channel, block);
      return new Bytes(key);
    }


    public boolean verify(String key) {
      return verify(new Bytes(key));
    }


    public boolean verify(Bytes b) {
      throw new UnsupportedOperationException("待实现..");
    }
  }


  public class KeyPairJs {
    public final String publicKey;
    public final String privateKey;

    private KeyPairJs() {
      Btc b       = new Btc();
      publicKey   = b.publicKeyStr();
      privateKey  = b.privateKeyStr();
    }

    private KeyPair toKeyPair() {
      return new KeyPair(Btc.publicKey(publicKey),
                         Btc.privateKey(privateKey));
    }
  }


  private Object wrap(SignNode root) {
    ScriptObjectMirror ret = createJSObject();
    while (root != null) {
      ret.put(root.id, new Bytes(root.sign));
      root = root.next;
    }
    return ret;
  }
}
