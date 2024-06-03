////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-2-1 下午5:43
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/app/lib/FabricImpl.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.app.lib;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.xboson.auth.IAResource;
import com.xboson.auth.PermissionSystem;
import com.xboson.auth.impl.LicenseAuthorizationRating;
import com.xboson.script.lib.Buffer;
import com.xboson.util.ECDSA;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.Closeable;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class FabricImpl extends RuntimeUnitImpl implements IAResource {

  private static final Buffer BUF = new Buffer();
  private final com.xboson.util.ECDSA ecdsa;

  public final TimeUnit HOURS         = TimeUnit.HOURS;
  public final TimeUnit MINUTES       = TimeUnit.MINUTES;
  public final TimeUnit SECONDS       = TimeUnit.SECONDS;
  public final TimeUnit MILLISECONDS  = TimeUnit.MILLISECONDS;
  public final TimeUnit MICROSECONDS  = TimeUnit.MICROSECONDS;


  public FabricImpl() throws NoSuchAlgorithmException {
    super(null);
    ecdsa = ECDSA.me();
  }


  /**
   * {
   *   name : "channel name",
   *   peer : [ 'grpcURL', ... ],
   *   orderer: [ 'grpcURL', ... ],
   *   enrollment : {
   *     name : 'user name',
   *     mspid : '',
   *     roles : ['role', ... ],
   *     affiliation : '',
   *     account : '',
   *     privateKey : '',
   *     certificate : '',
   *   }
   * }
   */
  public Channel0 newChannel(Object conf) throws Exception {
    PermissionSystem.applyWithApp(LicenseAuthorizationRating.class, this);
    return new Channel0(new Mirror(conf));
  }


  @Override
  public String description() {
    return "app.module.fabric.functions()";
  }


  public class Channel0 implements Closeable {

    private Map<ScriptObjectMirror, Collection<ProposalResponse>> prmap;
    private HFClient client;
    private Channel channel;


    private Channel0(Mirror conf) throws Exception {
      client = HFClient.createNewInstance();
      client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
      client.setUserContext(new User0(conf));
      channel = client.newChannel(conf.string("name"));
      addPeers(conf);
      addOrderers(conf);
      channel.initialize();
      prmap = new WeakHashMap<>();
      ModuleHandleContext.autoClose(this);
    }

    private void addPeers(Mirror conf) throws Exception {
      Mirror list = conf.list("peer");
      int i = 0;
      for (String url : list.each(String.class)) {
        Peer peer = client.newPeer("peer"+ (++i), url);
        channel.addPeer(peer);
      }
    }

    private void addOrderers(Mirror conf) throws Exception {
      Mirror list = conf.list("orderer");
      int i = 0;
      for (String url : list.each(String.class)) {
        Orderer peer = client.newOrderer("orderer"+ (++i), url);
        channel.addOrderer(peer);
      }
    }

    @Override
    public void close() throws IOException {
      channel.shutdown(false);
    }

    /**
     * {
     *   chaincodeId : '',
     *   fcn : '',
     *   args : [''],
     * }
     */
    public Object queryByChaincode(Object $conf) throws Exception {
      Mirror conf = new Mirror($conf);
      QueryByChaincodeRequest req = createQCR(client, conf);
      Collection<ProposalResponse> resp = channel.queryByChaincode(req);
      ScriptObjectMirror w = wrap(resp);
      prmap.put(w, resp);
      return w;
    }

    public Object sendTransactionProposal(Object $conf) throws Exception {
      Mirror conf = new Mirror($conf);
      TransactionProposalRequest req = createTPR(client, conf);
      Collection<ProposalResponse> resp = channel.sendTransactionProposal(req);
      ScriptObjectMirror w = wrap(resp);
      prmap.put(w, resp);
      return w;
    }

    public Object sendUpgradeProposal(Object $conf) throws Exception {
      Mirror conf = new Mirror($conf);
      UpgradeProposalRequest req = createUPR(client, conf);
      Collection<ProposalResponse> resp = channel.sendUpgradeProposal(req);
      ScriptObjectMirror w = wrap(resp);
      prmap.put(w, resp);
      return w;
    }

    public Object	sendInstantiationProposal(Object $conf) throws Exception {
      Mirror conf = new Mirror($conf);
      InstantiateProposalRequest req = createIPR(client, conf);
      Collection<ProposalResponse> resp = channel.sendInstantiationProposal(req);
      ScriptObjectMirror w = wrap(resp);
      prmap.put(w, resp);
      return w;
    }

    public Object queryBlockByNumber(long blockNumber) throws Exception {
      BlockInfo binfo = channel.queryBlockByNumber(blockNumber);
      return wrap(binfo);
    }

    public Object queryBlockByTransactionID(String txID) throws Exception {
      BlockInfo binfo = channel.queryBlockByTransactionID(txID);
      return wrap(binfo);
    }

    public Object queryBlockchainInfo() throws Exception {
      BlockchainInfo info = channel.queryBlockchainInfo();
      return wrap(info);
    }

    public Object sendTransaction(ScriptObjectMirror proposalResponses)
            throws ExecutionException, InterruptedException {
      Collection<ProposalResponse> resp = prmap.get(proposalResponses);
      CompletableFuture<BlockEvent.TransactionEvent>
              transactionEvent = channel.sendTransaction(resp);
      return transactionEvent;
    }
  }


  public class User0 implements User {
    private Mirror user;

    private User0(Mirror conf) {
      user = conf.jsobj("enrollment");
    }

    @Override
    public String getName() {
      return user.string("name");
    }

    @Override
    public Set<String> getRoles() {
      Mirror list = user.list("roles");
      Set<String> ret = new HashSet<>(list.size());
      for (String role : list.each(String.class)) {
        ret.add(role);
      }
      return ret;
    }

    @Override
    public String getAccount() {
      return user.string("account");
    }

    @Override
    public String getAffiliation() {
      return user.string("affiliation");
    }

    @Override
    public Enrollment getEnrollment() {
      return new Enrollment0(user);
    }

    @Override
    public String getMspId() {
      return user.string("mspid");
    }
  }


  public class Enrollment0 implements Enrollment {
    private Mirror user;

    private Enrollment0(Mirror user) {
      this.user = user;
    }

    @Override
    public PrivateKey getKey() {
      return ecdsa.parsePrivateKey(user.string("privateKey"));
    }

    @Override
    public String getCert() {
      return user.string("certificate");
    }
  }


  private QueryByChaincodeRequest createQCR(HFClient client, Mirror conf) {
    QueryByChaincodeRequest req = client.newQueryProposalRequest();
    setConfig(req, conf);
    return req;
  }


  private TransactionProposalRequest createTPR(HFClient client, Mirror conf) {
    TransactionProposalRequest req = client.newTransactionProposalRequest();
    setConfig(req, conf);
    return req;
  }


  private UpgradeProposalRequest createUPR(HFClient client, Mirror conf) {
    UpgradeProposalRequest req = client.newUpgradeProposalRequest();
    setConfig(req, conf);
    return req;
  }


  private InstantiateProposalRequest createIPR(HFClient client, Mirror conf) {
    InstantiateProposalRequest req = client.newInstantiationProposalRequest();
    setConfig(req, conf);
    return req;
  }


  private void setConfig(TransactionRequest req, Mirror conf) {
    String id     = conf.string("chaincodeId");
    String fcn    = conf.string("fcn");
    String[] args = conf.list("args").toArray(String[].class);

    ChaincodeID cid = ChaincodeID.newBuilder().setName(id).build();
    req.setChaincodeID(cid);
    req.setFcn(fcn);
    req.setArgs(args);
  }


  private ScriptObjectMirror wrap(ProposalResponse pr) throws Exception {
    Buffer.JsBuffer payload =
            BUF.from(pr.getChaincodeActionResponsePayload());
    ScriptObjectMirror resData = createJSObject();
    resData.setMember("status", pr.getChaincodeActionResponseStatus());
    resData.setMember("message", pr.getMessage());
    resData.setMember("payload", payload);
    return resData;
  }


  private ScriptObjectMirror wrap(Collection<ProposalResponse> resps)
          throws Exception {
    ScriptObjectMirror ret = createJSList(resps.size());
    int i = -1;
    for (ProposalResponse resp : resps) {
      ScriptObjectMirror resData = wrap(resp);
      ret.setSlot(++i, resData);
    }
    return ret;
  }


  private ScriptObjectMirror wrap(BlockInfo info) throws Exception {
    ScriptObjectMirror ret = createJSObject();
    ret.setMember("block",          wrap(info.getBlock()));
    ret.setMember("blockNumber",    info.getBlockNumber());
    ret.setMember("channelId",      info.getChannelId());
    ret.setMember("dataHash",       toBase64Str(info.getDataHash()));
    ret.setMember("previousHash",   toBase64Str(info.getPreviousHash()));
    ret.setMember("envelopeInfos",  wrap(info.getEnvelopeInfos()));
    // getTransactionActionInfos 与 getEnvelopeInfos 返回相同数据所以删除
    //    ret.setMember("transactionActionInfos",
    //                  wrap(info.getTransactionActionInfos()));
    return ret;
  }


  private ScriptObjectMirror wrap(Common.Block bl) {
    ScriptObjectMirror ret = createJSObject();
    ret.setMember("header",     wrap(bl.getHeader()));
    ret.setMember("data",       wrap(bl.getData()));
    ret.setMember("meta",       wrap(bl.getMetadata()));
    ret.setMember("descriptor", wrap(bl.getDescriptorForType()));
    return ret;
  }


  private ScriptObjectMirror wrap(Iterable<BlockInfo.EnvelopeInfo> iter) {
    ScriptObjectMirror ret = createJSList();
    int i = -1;
    for (BlockInfo.EnvelopeInfo info : iter) {
      ret.setSlot(++i, wrap(info));
    }
    return ret;
  }


  private ScriptObjectMirror wrap(BlockInfo.EnvelopeInfo info) {
    ScriptObjectMirror ret = createJSObject();
    ret.setMember("channelId",      info.getChannelId());
    ret.setMember("epoch",          info.getEpoch());
    ret.setMember("timestamp",      info.getTimestamp());
    ret.setMember("transactionID",  info.getTransactionID());
    ret.setMember("validationCode", info.getValidationCode());
    ret.setMember("type",           info.getType().name());
    return ret;
  }


  private ScriptObjectMirror wrap(Common.BlockHeader head) {
    ScriptObjectMirror ret = createJSObject();
    ret.setMember("number", head.getNumber());
    ret.setMember("hash",   toBase64Str(head.getDataHash()));
    return ret;
  }


  private ScriptObjectMirror wrap(Common.BlockData data) {
    final int size = data.getDataCount();
    ScriptObjectMirror ret = createJSList(size);
    for (int i=0; i<size; ++i) {
      ret.setSlot(i, toBase64Str(data.getData(i)));
    }
    return ret;
  }


  private ScriptObjectMirror wrap(Common.BlockMetadata meta) {
    final int size = meta.getMetadataCount();
    ScriptObjectMirror ret = createJSList(size);
    for (int i=0; i<size; ++i) {
      ret.setSlot(i, toBase64Str(meta.getMetadata(i)));
    }
    return ret;
  }


  private ScriptObjectMirror wrap(Descriptors.Descriptor desc) {
    ScriptObjectMirror ret = basic(desc);
    if (desc == null) return ret;
    ret.setMember("index",           desc.getIndex());
    ret.setMember("containningType", basic(desc.getContainingType()));
    ret.setMember("nestedTypes",     wrapDescArr(desc.getNestedTypes()));
    ret.setMember("fields",          wrapDescArr(desc.getFields()));
    ret.setMember("enumTypes",       wrapDescArr(desc.getEnumTypes()));
    ret.setMember("extensions",      wrapDescArr(desc.getExtensions()));
    return ret;
  }


  private ScriptObjectMirror basic(Descriptors.GenericDescriptor desc) {
    ScriptObjectMirror ret = createJSObject();
    if (desc == null) return ret;
    ret.setMember("name",     desc.getName());
    ret.setMember("fullName", desc.getFullName());
    return ret;
  }


  private <E extends Descriptors.GenericDescriptor>
  ScriptObjectMirror wrapDescArr(List<E> list) {
    final int size = list.size();
    ScriptObjectMirror ret = createJSList(size);
    for (int i=0; i<size; ++i) {
      ret.setSlot(i, basic(list.get(i)));
    }
    return ret;
  }


  private Object toBase64Str(ByteString bs) {
    return toBase64Str(bs.toByteArray());
  }


  private Object toBase64Str(byte[] b) {
    return Base64.getEncoder().encodeToString(b);
  }


  private Object wrap(BlockchainInfo info) {
    ScriptObjectMirror ret = createJSObject();
    ret.setMember("height", info.getHeight());
    ret.setMember("currentBlockHash",
            toBase64Str(info.getCurrentBlockHash()));
    ret.setMember("previousBlockHash",
            toBase64Str(info.getPreviousBlockHash()));
    return ret;
  }
}
