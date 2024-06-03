////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 17-11-25 下午3:40
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/SnowflakeIdWorker.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util;

import com.xboson.been.XBosonException;


/**
 * Twitter_Snowflake
 * <br><br>
 * SnowFlake的结构如下(每部分用-分开):<br>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0
 *  - 00000 - 00000 - 000000000000
 * <br><br>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，
 * 所以id一般是正数，最高位是0 41位时间截(毫秒级)，注意，41位时间截不是存储当前时
 * 间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截) 得到的值），这里的的
 * 开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程
 * 序IdWorker类的startTime属性）。41位的时间截，可以使用69年，年
 * T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69 10位的数据机器位，可以
 * 部署在1024个节点，包括5位datacenterId和5位workerId, 12位序列，毫秒内的计数，
 * 12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号,
 * 加起来刚好64位，为一个Long型。<br>
 * <br>
 * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞
 * (由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 * <br><br>
 *
 * @see Tool#nextId() 该类的全局实例在 Tool 中缓存, 不要直接使用该类.
 */
public class SnowflakeIdWorker {

  private final long twepoch = 1511595955875l;
  private final long workerIdBits = 5L;
  private final long datacenterIdBits = 5L;
  private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
  private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
  private final long sequenceBits = 12L;
  private final long workerIdShift = sequenceBits;
  private final long datacenterIdShift = sequenceBits + workerIdBits;
  private final long sequenceMask = -1L ^ (-1L << sequenceBits);
  private final long timestampLeftShift
          = sequenceBits + workerIdBits + datacenterIdBits;
  private long workerId;
  private long datacenterId;
  private long sequence = 0L;
  private long lastTimestamp = -1L;


  /**
   * @param workerId 工作ID (0~31)
   * @param datacenterId 数据中心ID (0~31)
   */
  public SnowflakeIdWorker(long workerId, long datacenterId) {
    if (workerId > maxWorkerId || workerId < 0) {
      throw new XBosonException(
              "worker Id can't be greater than "
                      + maxWorkerId +" or less than 0");
    }
    if (datacenterId > maxDatacenterId || datacenterId < 0) {
      throw new XBosonException(
              "datacenter Id can't be greater than "
                      + maxDatacenterId +" or less than 0");
    }
    this.workerId = workerId;
    this.datacenterId = datacenterId;
  }


  /**
   * 获得下一个ID (该方法是线程安全的)
   * @return SnowflakeId
   */
  public synchronized long nextId() {
    long timestamp = System.currentTimeMillis();

    if (timestamp < lastTimestamp) {
      throw new XBosonException(
              "Clock moved backwards.  Refusing to generate id for "
                      + (lastTimestamp - timestamp) +" milliseconds");
    }

    if (lastTimestamp == timestamp) {
      sequence = (sequence + 1) & sequenceMask;
      if (sequence == 0) {
        timestamp = tilNextMillis(lastTimestamp);
      }
    } else {
      sequence = 0L;
    }

    lastTimestamp = timestamp;

    return ((timestamp - twepoch) << timestampLeftShift)
            | (datacenterId << datacenterIdShift)
            | (workerId << workerIdShift)
            | sequence;
  }


  /**
   * 阻塞到下一个毫秒，直到获得新的时间戳
   * @param lastTimestamp 上次生成ID的时间截
   * @return 当前时间戳
   */
  private long tilNextMillis(long lastTimestamp) {
    long timestamp = System.currentTimeMillis();
    while (timestamp <= lastTimestamp) {
      Tool.sleep(1);
      timestamp = System.currentTimeMillis();
    }
    return timestamp;
  }
}