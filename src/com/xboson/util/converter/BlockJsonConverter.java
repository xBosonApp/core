////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-8-15 下午5:07
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/util/converter/BlockJsonConverter.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.util.converter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.xboson.chain.Block;
import com.xboson.chain.SignNode;
import com.xboson.util.Hex;

import javax.annotation.Nullable;
import java.io.IOException;


public class BlockJsonConverter extends JsonAdapter<Block> {

  @Nullable
  @Override
  public Block fromJson(JsonReader r) throws IOException {
    throw new UnsupportedOperationException();
  }


  @Override
  public void toJson(JsonWriter w, @Nullable Block b)
          throws IOException {
    w.beginObject();
    w.name("key")
            .value(Hex.encode64(b.key));
    w.name("hash")
            .value(Hex.encode64(b.hash));
    // 创世区块为空
    w.name("previousHash")
            .value(bin(b.previousHash));
    w.name("previousKey")
            .value(bin(b.previousKey));
    w.name("create")
            .value(b.create.getTime());

    w.name("data")
            .value(bin(b.getData()));
    w.name("userid")
            .value(b.getUserId());
    // 链码块为空
    w.name("chaincodeKey")
            .value(bin(b.getChaincodeKey()));
    w.name("apiPath")
            .value(b.getApiPath());
    w.name("apiHash")
            .value(b.getApiHash());
    w.name("type")
            .value(b.type);

    sign(w, b.sign);
    w.endObject();
  }


  private void sign(JsonWriter w, SignNode n) throws IOException {
    w.name("sign");
    w.beginArray();
    while (n != null) {
      w.beginObject();
      w.name("id").value(n.id);
      w.name("si").value(Hex.encode64(n.sign));
      w.endObject();
      n = n.next;
    }
    w.endArray();
  }


  /**
   * 允许参数为空, 并返回空.
   */
  private String bin(byte[] b) {
    if (b == null) return null;
    return Hex.encode64(b);
  }


  public void registerAdapter(Moshi.Builder builder) {
    builder.add(Block.class, this);
  }
}
