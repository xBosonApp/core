////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-14 上午8:18
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/DBFunctions.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.been.Config;
import com.xboson.been.Witness;
import com.xboson.been.XBosonException;
import com.xboson.db.SqlResult;
import com.xboson.db.sql.SqlReader;
import com.xboson.util.SysConfig;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * 从 db 中获取数据的操作集合
 */
public class DBFunctions {

  private static final String SQL_KEYS    = "open_chain_key";
  private static final String SQL_WITNESS = "open_witness";

  private static DBFunctions instance;
  private final Config cf;


  private DBFunctions() {
    cf =  SysConfig.me().readConfig();
  }


  public static DBFunctions me() {
    if (instance == null) {
      synchronized (DBFunctions.class) {
        if (instance == null) {
          instance = new DBFunctions();
        }
      }
    }
    return instance;
  }


  /**
   * 在脚本环境中调用该方法
   */
  public KeyPair[] openChainKeys(String chain, String channel) {
    KeyPair[] keys = new KeyPair[ ITypes.LENGTH +1 ];
    Object[] parm = { chain, channel };

    try (SqlResult sr = SqlReader.query(SQL_KEYS, cf.db, parm)) {
      ResultSet rs = sr.getResult();
      while (rs.next()) {
        PublicKey pub = Btc.publicKey(rs.getString("publickey"));
        PrivateKey pri = Btc.privateKey(rs.getString("privatekey"));
        int index      = rs.getInt("type");
        keys[index]    = new KeyPair(pub, pri);
      }
      return keys;
    } catch (Exception e) {
      throw new XBosonException(e);
    }
  }


  public Witness getWitness(String witnessId) {
    Object[] parm = { witnessId };

    try (SqlResult sr = SqlReader.query(SQL_WITNESS, cf.db, parm)) {
      ResultSet rs = sr.getResult();
      if (rs.next()) {
        return new Witness(witnessId,
                rs.getString("publickey"),
                rs.getString("urlperfix"),
                rs.getString("host"),
                rs.getString("algorithm"),
                rs.getInt("port"));
      }
      return null;
    } catch (SQLException e) {
      throw new XBosonException(e);
    }
  }
}
