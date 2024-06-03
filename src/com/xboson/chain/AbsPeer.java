////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-17 上午8:39
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/AbsPeer.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.been.ChainEvent;
import com.xboson.been.XBosonException;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.util.Hex;
import com.xboson.util.LocalLock;
import com.xboson.util.WeakMemCache;

import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 不要直接操作 BlockFileSystem 而是通过本类的 get 方法从缓存中获取
 */
public abstract class AbsPeer implements IPeer, IPeerLocal {

  private WeakMemCache<String, BlockFileSystem.InnerChain> chainCache;
  private WeakMemCache<ChainKey, BlockFileSystem.InnerChannel> channelCache;
  private ISignerProvider sp;
  /** 对于读写操作的锁对象 */
  protected final ReadWriteLock lock;


  protected AbsPeer() {
    this.sp           = new DefaultSignerProvider();
    this.lock         = new ReentrantReadWriteLock(false);
    this.chainCache   = new WeakMemCache<>(new CreateChain());
    this.channelCache = new WeakMemCache<>(new CreateChannel());
  }


  /**
   * 该方法带有缓存, 但没有锁
   */
  protected BlockFileSystem.InnerChain getChain(String chainName) {
    return chainCache.getOrCreate(chainName);
  }


  /**
   * 该方法带有缓存, 但没有锁
   */
  protected BlockFileSystem.InnerChannel getChannel(String chain, String channel) {
    BlockFileSystem.InnerChain c = getChain(chain);
    ChainKey key = new ChainKey(c, channel);
    return channelCache.getOrCreate(key);
  }


  private class CreateChain implements
          WeakMemCache.ICreator<String, BlockFileSystem.InnerChain> {
    @Override
    public BlockFileSystem.InnerChain create(String chainName) {
      return BlockFileSystem.me().getChain(chainName);
    }
  }


  private class CreateChannel implements
          WeakMemCache.ICreator<ChainKey, BlockFileSystem.InnerChannel> {
    @Override
    public BlockFileSystem.InnerChannel create(ChainKey init) {
      return init.chain.openChannel(init.channel);
    }
  }


  private class ChainKey {
    BlockFileSystem.InnerChain chain;
    String channel;
    int hash;

    ChainKey(BlockFileSystem.InnerChain chain, String channel) {
      this.hash     = (chain.getName() + channel).hashCode();
      this.chain    = chain;
      this.channel  = channel;
    }

    @Override
    public int hashCode() {
      return hash;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof ChainKey) {
        ChainKey ck = (ChainKey) o;
        return chain.equals(ck.chain) && channel.equals(ck.channel);
      }
      return false;
    }
  }


  @Override
  public Block search(String chainName, String channelName, byte[] key) {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      return getChannel(chainName, channelName).search(key);
    }
  }


  @Override
  public byte[] worldState(String chainName, String channelName) {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      return getChannel(chainName, channelName).worldState();
    }
  }


  @Override
  public byte[] lastBlockKey(String chainName, String channelName) {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      return getChannel(chainName, channelName).lastBlockKey();
    }
  }


  @Override
  public byte[] genesisKey(String chain, String channel) throws RemoteException {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      return getChannel(chain, channel).genesisKey();
    }
  }


  @Override
  public String[] allChainNames() {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      Set<String> names = BlockFileSystem.me().allChainNames();
      return names.toArray(new String[names.size()]);
    }
  }


  @Override
  public String[] allChannelNames(String chain) {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      Set<String> names = getChain(chain).allChannelNames();
      return names.toArray(new String[names.size()]);
    }
  }


  public ChainEvent[] allChainSetting() {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      List<ChainEvent> ret = new ArrayList<>();
      BlockFileSystem bfs = BlockFileSystem.me();

      for (String chain: bfs.allChainNames()) {
        BlockFileSystem.InnerChain ca = getChain(chain);

        for (String channel : ca.allChannelNames()) {
          BlockFileSystem.InnerChannel ch = getChannel(chain, channel);
          ret.add( new ChainEvent(chain, channel,
                  ch.getConsensusExp(), ch.getKeyPairs()) );
        }
      }
      return ret.toArray(new ChainEvent[ret.size()]);
    }
  }


  @Override
  public boolean channelExists(String chain, String channel) {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      if (BlockFileSystem.me().chainExists(chain)) {
        return getChain(chain).channelExists(channel);
      }
      return false;
    }
  }


  @Override
  public byte[] getChaincodeKey(String chain, String channel, String path, String hash)
          throws RemoteException {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      return getChannel(chain, channel).getChaincodeKey(path, hash);
    }
  }


  @Override
  public int size(String chain, String channel) throws RemoteException {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      return getChannel(chain, channel).size();
    }
  }


  @Override
  public PublicKey getWitnessPublicKey(String chain, String channel, String wid) {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      return getChannel(chain, channel).getWitnessPublicKey(wid);
    }
  }


  protected byte[] sendBlockLocal(String chain, String channel, BlockBasic b) {
    try (LocalLock _ = new LocalLock(lock.writeLock())) {
      BlockFileSystem.InnerChannel ch = getChannel(chain, channel);
      byte[] key = ch.push(b);
      ch.commit();
      return key;
    }
  }


  protected void createChannelLocal(String chainName, String channelName,
                                    String userid, String exp, KeyPair[] kp) {
    try (LocalLock _ = new LocalLock(lock.writeLock())) {
      BlockFileSystem.InnerChain ca = getChain(chainName);
      ca.createChannel(channelName,
              getSigner(chainName, channelName, exp, kp), userid);
      ca.commit();
    }
  }


  /**
   * 验证块的有效性, 成功返回 true
   */
  protected boolean verify(String chain, String channel, Block b) {
    try (LocalLock _ = new LocalLock(lock.readLock())) {
      return getChannel(chain, channel).verify(b);
    }
  }


  /**
   * 使用注册的签名提供商创建一个签名器
   * @param chain 链
   * @param channel 通道
   * @param exp 共识表达式
   * @return
   */
  protected ISigner getSigner(String chain, String channel,
                              String exp, KeyPair[] kp) {
    return sp.getSigner(chain, channel, exp, kp);
  }


  public void registerSignerProvider(ISignerProvider sp) {
    if (sp == null) throw new XBosonException.BadParameter(
        "ISignerProvider sp", "is null");
    this.sp = sp;
  }


  @Override
  public void waitOver() {
    try (LocalLock _ = new LocalLock(lock.writeLock())) {}
  }


  public static class DefaultSignerProvider implements ISignerProvider {
    @Override
    public ISigner getSigner(String chain, String channel,
                             String exp, KeyPair[] kp) {
      return new NoneSigner();
    }
  }


  /**
   * 空签名器, 不执行签名, 总是验证成功
   */
  public static class NoneSigner implements ISigner {
    private transient Log log;

    public NoneSigner() {
      log = LogFactory.create("chain-none-signer");
    }

    @Override
    public void sign(Block block) {
      log.debug("sign block", Hex.lowerHex(block.key));
    }

    @Override
    public boolean verify(Block block) {
      log.debug("verify block", Hex.lowerHex(block.key));
      return true;
    }

    @Override
    public void deliver(Block block) {
      log.debug("deliver block", Hex.lowerHex(block.key));
    }

    @Override
    public PublicKey getWitnessPublicKey(String wid) {
      return null;
    }

    private void readObject(ObjectInputStream i) {
      log = LogFactory.create("chain-no-signer");
    }
  }

}
