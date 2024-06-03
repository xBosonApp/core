////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-13 下午12:47
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/BlockFileSystem.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.been.Config;
import com.xboson.been.XBosonException;
import com.xboson.util.Hex;
import com.xboson.util.SysConfig;
import com.xboson.util.Tool;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;


/**
 * <a href='https://jankotek.gitbooks.io/mapdb/content/htreemap/'>MapDB</a>
 */
public class BlockFileSystem implements ITypes {

  private static final char META_PREFIX     = '_';
  private static final char CHANNEL_PREFIX  = '~';
  private static final String PATH          = "/chain";
  private static final String CHAIN_EXT     = ".chain";
  private static final String SYS_FILE      = "/system.db";
  private static final String CHAIN_CODE    = "@";
  private static final int INIT_SIZE        = 16 * 1024;
  private static final int INCREMENT_SIZE   = 1  * 1024*1024;
  private static BlockFileSystem instance;

  private final String rootDir;
  private final int increment;
  private final DB sysdb;
  private final HTreeMap.KeySet chainNames;


  public static BlockFileSystem me() {
    if (instance == null) {
      synchronized (BlockFileSystem.class) {
        if (instance == null) {
          instance = new BlockFileSystem();
        }
      }
    }
    return instance;
  }


  private BlockFileSystem() {
    Config cf   = SysConfig.me().readConfig();
    rootDir     = Tool.normalize(Tool.isNulStr(cf.chainPath)
                ? cf.configPath+PATH : cf.chainPath);
    increment   = cf.chainIncrement > 0
                ? cf.chainIncrement : INCREMENT_SIZE;

    try {
      Files.createDirectories(Paths.get(rootDir));
      sysdb       = makeDB(SYS_FILE);
      chainNames  = sysdb.hashSet("chains").createOrOpen();
    } catch (Exception e) {
      throw new XBosonException(e);
    }
  }


  /**
   * 创建/获取链, 该方法已经同步
   */
  public synchronized InnerChain getChain(String name) {
    if (Tool.isNulStr(name))
      throw new NullPointerException("name");

    InnerChain chain = new InnerChain(name);
    chainNames.add(name);
    sysdb.commit();
    return chain;
  }


  public boolean chainExists(String name) {
    return chainNames.contains(name);
  }


  public Set<String> allChainNames() {
    return Collections.unmodifiableSet(chainNames);
  }


  private DB makeDB(String fileName) {
    return DBMaker.fileDB(rootDir +'/'+ fileName)
            .allocateStartSize(INIT_SIZE)
            .allocateIncrement(increment)
            .transactionEnable()
            .make();
  }


  /**
   * 链的实现, 所有写操作都需要 commit() 来将改变写入文件系统
   */
  public class InnerChain implements AutoCloseable {
    private final String chainName;
    private HTreeMap metaMap;
    private HTreeMap signerMap;
    private DB db;


    private InnerChain(String name) {
      this.db         = makeDB(name + CHAIN_EXT);
      this.chainName  = name;
      this.metaMap    = metaTemplate("meta");
      this.signerMap  = metaTemplate("signer");
    }


    /**
     * 递交所有操作到文件
     */
    public void commit() {
      db.commit();
    }


    /**
     * 回滚操作
     */
    public void rollback() {
      db.rollback();
    }


    /**
     * 关闭底层文件系统, 未递交的操作被丢弃
     */
    public void close() {
      db.close();
      db = null;
      metaMap = null;
      signerMap = null;
    }


    @Override
    protected void finalize() throws Throwable {
      close();
    }


    /**
     * 如果通道已经存在抛出异常, 签名器将被绑定到区块, 任何对签名器类的修改都会引起异常.
     */
    public InnerChannel createChannel(String name, ISigner si, String userid) {
      return createChannel(name, si, null, userid);
    }


    /**
     * 使用完整的创世区块创建通道.
     * [为多节点同步而设计]
     */
    InnerChannel createChannel(String name, ISigner si, Block genesis) {
      return createChannel(name, si, genesis, null);
    }


    /**
     * 该方法有两种行为:
     * 1. genesis 参数为 null 时 userid 生效, 创建一个创世区块.
     * 2. 否则使用创世区块生产通道.
     */
    private InnerChannel createChannel(String name, ISigner si,
                                       Block genesis, String userid) {
      HTreeMap<byte[], Block> map = channelTemplate(name).create();
      MetaBlock gb = new MetaBlock(name, si);
      InnerChannel ch = new InnerChannel(map, gb, this, si);

      if (genesis != null) {
        si.verify(genesis);
        gb.genesisKey = ch.pushOriginal(genesis);
      }
      else if (userid != null) {
        BlockBasic genesis_b = MetaBlock.createGenesis(si);
        genesis_b.setUserid(userid);
        gb.genesisKey = ch.push(genesis_b);
      }
      else {
        throw new NullPointerException("genesis and userid both null");
      }

      si.removeGenesisPrivateKey();
      signerMap.put(name, si);
      metaMap.put(name, gb);
      return ch;
    }


    /**
     * 如果通道不存在会抛出异常
     */
    public InnerChannel openChannel(String name) {
      HTreeMap<byte[], Block> map = channelTemplate(name).open();
      MetaBlock gb = (MetaBlock) metaMap.get(name);
      ISigner signer = (ISigner) signerMap.get(name);
      return new InnerChannel(map, gb, this, signer);
    }


    public Set<String> allChannelNames() {
      return Collections.unmodifiableSet(metaMap.keySet());
    }


    public boolean channelExists(String name) {
      return db.exists(CHANNEL_PREFIX + name);
    }


    private DB.HashMapMaker channelTemplate(String name) {
      return db.hashMap(CHANNEL_PREFIX + name)
              .keySerializer(Serializer.BYTE_ARRAY)
              .valueSerializer(SerializerBlock.me)
              .layout(16, 128, 4)
              .counterEnable();
    }


    private HTreeMap metaTemplate(String name) {
      return db.hashMap(META_PREFIX + name)
              .keySerializer(Serializer.STRING)
              .valueSerializer(Serializer.JAVA)
              .createOrOpen();
    }


    public String getName() {
      return chainName;
    }
  }


  /**
   * 通道的实现, 所有写操作都需要 commit() 来将改变写入文件系统
   */
  public class InnerChannel {
    private Map<byte[], Block> map;
    private Map<String, byte[]> chaincode;
    private MetaBlock gb;
    private InnerChain chain;
    private ISigner signer;


    private InnerChannel(Map<byte[], Block> map, MetaBlock gb,
                         InnerChain ic, ISigner si) {
      this.map       = map;
      this.gb        = gb;
      this.chain     = ic;
      this.signer    = si;
      this.chaincode = ic.metaTemplate(CHAIN_CODE + gb.channelName);
    }


    /**
     * 推入新块, 返回 key
     */
    public byte[] push(BlockBasic b) {
      return push(b.createBlock());
    }


    /**
     * 块必须经由该方法上链
     */
    protected byte[] push(Block b) {
      do {
        b.generateKey();
      } while(map.containsKey(b.key));

      String codeName = b.type == CHAINCODE_CONTENT ? checkChaincode(b) : null;
      b.create       = new Date();
      b.previousHash = gb.worldStateHash;
      b.previousKey  = gb.lastBlockKey;

      signer.sign(b);
      b.computeHash();
      signer.deliver(b);
      pushOriginal(b);

      if (b.type == CHAINCODE_CONTENT) {
        chaincode.put(codeName, b.key);
      }
      return b.key;
    }


    /**
     * 不执行验证/生成步骤, 直接将区块上链.
     * [为多节点同步而设计]
     */
    byte[] pushOriginal(Block b) {
      if (!Arrays.equals(b.previousHash, gb.worldStateHash))
        throw new VerifyException("bad previous hash", b.previousHash);

      if (!Arrays.equals(b.previousKey, gb.lastBlockKey))
        throw new VerifyException("bad previous key", b.previousKey);

      if (b.key == null || map.containsKey(b.key))
        throw new VerifyException("key conflict", b.key);

      map.put(b.key, b);

      gb.worldStateHash = b.hash;
      gb.lastBlockKey   = b.key;
      chain.metaMap.put(gb.channelName, gb);
      return b.key;
    }


    /**
     * 使用 key 查询并返回一个数据块, 查询前该块将被验证
     * 验证失败将抛出异常.
     */
    public Block search(byte[] key) {
      Block b = map.get(key);
      if (b != null && (! signer.verify(b)) ) {
        throw new VerifyException("Key-Hex: "+ Hex.lowerHex(key));
      }
      return b;
    }


    public String getConsensusExp() {
      return gb.consensusExp;
    }


    public KeyPair[] getKeyPairs() {
      return gb.keys;
    }


    public PublicKey getWitnessPublicKey(String witnessID) {
      return signer.getWitnessPublicKey(witnessID);
    }


    /**
     * 验证数据块
     * @return 成功返回 true
     */
    public boolean verify(Block b) {
      return signer.verify(b);
    }


    public byte[] worldState() {
      return Arrays.copyOf(gb.worldStateHash, gb.worldStateHash.length);
    }


    public byte[] lastBlockKey() {
      return Arrays.copyOf(gb.lastBlockKey, gb.lastBlockKey.length);
    }


    public byte[] genesisKey() {
      return Arrays.copyOf(gb.genesisKey, gb.genesisKey.length);
    }


    public byte[] getChaincodeKey(String path, String hash) {
      return chaincode.get(path + CHAIN_CODE + hash);
    }


    /**
     * 检查链码块有效性, 并返回链码 key 缓存的名字
     */
    public String checkChaincode(BlockBasic b) {
      if (b.type != CHAINCODE_CONTENT)
        throw new VerifyException("Bad type");

      if (Tool.isNulStr(b.apiPath))
        throw new VerifyException("Block.apiPath is null");

      if (Tool.isNulStr(b.apiHash))
        throw new VerifyException("Block.apiHash is null");

      if (b.data == null || b.data.length < 1)
        throw new VerifyException("Chain code context is empty");

      String codeName = b.apiPath + CHAIN_CODE + b.apiHash;

      if (chaincode.containsKey(codeName))
        throw new VerifyException("is exists "+ codeName);

      return codeName;
    }


    public int size() {
      return map.size();
    }



    /**
     * 递交所有操作到文件, 注意这将递交在链上的所有操作
     */
    public void commit() {
      chain.commit();
    }


    /**
     * 回滚操作, 这将回滚链上所有未递交的操作
     */
    public void rollback() {
      chain.rollback();
    }
  }
}
