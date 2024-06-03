////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-3 下午2:40
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/mongo/MongoFileAttr.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.mongo;

import com.mongodb.client.model.Filters;
import com.xboson.fs.basic.AbsFileAttr;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Collection;


public class MongoFileAttr extends AbsFileAttr {

  /** 文件内容节点 id */
  public final ObjectId content_id;

  private Bson filter_id;


  public MongoFileAttr(MongoFileAttr fs) {
    super(fs);
    content_id = fs.content_id;
  }


  public MongoFileAttr(org.bson.Document doc) {
    super(doc.getString("path"),
            doc.getInteger("type"),
            doc.getLong("lastModify"));

    this.content_id = doc.getObjectId("content_id");

    if (type == T_DIR) {
      super.dir_contain.addAll((Collection) doc.get("contains"));
    }
  }


  public MongoFileAttr(String path, int type, long lastModify, ObjectId id) {
    super(path, type, lastModify);
    this.content_id = id;
  }


  public final MongoFileAttr cloneNewContnet(ObjectId id) {
    return new MongoFileAttr(path, type, System.currentTimeMillis(), id);
  }


  public static MongoFileAttr createFile(String file, ObjectId id) {
    return new MongoFileAttr(file, T_FILE, System.currentTimeMillis(), id);
  }


  public static MongoFileAttr createDir(String path) {
    return new MongoFileAttr(path, T_DIR, System.currentTimeMillis(), null);
  }


  public org.bson.Document toDocument() {
    org.bson.Document doc = new org.bson.Document();
    doc.append("path", path);
    doc.append("_id", path);
    doc.append("type", type);
    doc.append("lastModify", lastModify);
    doc.append("contains", dir_contain);
    doc.append("content_id", content_id);
    return doc;
  }


  public Bson toFilterID() {
    if (filter_id == null) {
      filter_id = Filters.eq("_id", path);
    }
    return filter_id;
  }
}
