////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-3 下午1:46
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/mongo/MongoFileSystem.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.xboson.been.XBosonException;
import com.xboson.fs.basic.AbsFileSystemUtil;
import com.xboson.script.IVisitByScript;
import com.xboson.script.lib.Path;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import static com.xboson.fs.basic.IFileAttribute.T_FILE;


/**
 * 文件结构会被删除, 文件内容 (的多个版本) 将永久保留.
 */
public class MongoFileSystem extends AbsFileSystemUtil<MongoFileAttr>
        implements IMongoFileSystem, IVisitByScript {

  public static final String STRUCT_SUFFIX = ".struct";

  private MongoCollection<Document> struct;
  private MongoDatabase db;
  private GridFSBucket fs;
  private String diskName;


  MongoFileSystem(MongoDatabase db, String diskName) {
    this.fs       = GridFSBuckets.create(db, diskName);
    this.diskName = diskName;
    this.db       = db;
    this.struct   = db.getCollection(diskName + STRUCT_SUFFIX);
  }


  @Override
  public MongoFileAttr readAttribute(String path) {
    path = normalize(path);
    Bson id = Filters.eq("_id", path);
    for (org.bson.Document doc : struct.find(id)) {
      return new MongoFileAttr(doc);
    }
    return null;
  }


  @Override
  public Set<MongoFileAttr> readDir(String path) {
    return readDirContain(path);
  }


  @Override
  public void move(String src, String to) {
    super.moveTo(src, to);
  }


  @Override
  public void delete(String path) {
    deleteFile(path);
  }


  @Override
  public long modifyTime(String path) {
    path = normalize(path);
    Bson id = Filters.eq("_id", path);
    for (org.bson.Document doc : struct.find(id)) {
      return doc.getLong("lastModify");
    }
    return -1;
  }


  @Override
  public void makeDir(String path) {
    MongoFileAttr attr = readAttribute(path);
    if (attr == null) {
      attr = MongoFileAttr.createDir(path);
      makeStructUntilRoot(attr);
    }
  }


  @Override
  public InputStream openInputStream(String file) {
    MongoFileAttr attr = readAttribute(file);
    if (attr == null)
      throw new XBosonException.NotFound(file);
    if (attr.type != T_FILE)
      throw new XBosonException.IOError("Is not file", file);

    return fs.openDownloadStream(attr.content_id);
  }


  @Override
  public OutputStream openOutputStream(String file) {
    MongoFileAttr attr = readAttribute(file);
    GridFSUploadStream out = fs.openUploadStream(file);
    if (attr == null) {
      attr = MongoFileAttr.createFile(file, out.getObjectId());
      makeStructUntilRoot(attr);
    } else {
      attr = attr.cloneNewContnet(out.getObjectId());
    }
    saveNode(attr);
    return out;
  }


  @Override
  protected void moveFile(MongoFileAttr src, String to) {
    MongoFileAttr tofs = new MongoFileAttr(
            to, src.type, src.lastModify, src.content_id);
    saveNode(tofs);
    deleteFile(src.path);
  }


  @Override
  protected Set<String> containFiles(MongoFileAttr dir) {
    return dir.dir_contain;
  }


  @Override
  protected MongoFileAttr createDirNode(String path) {
    return MongoFileAttr.createDir(path);
  }


  @Override
  protected void addTo(MongoFileAttr dir, String path) {
    dir.addChildStruct(path);
  }


  @Override
  protected void saveNode(MongoFileAttr a) {
    UpdateOptions opt = new UpdateOptions().upsert(true);
    struct.replaceOne(a.toFilterID(), a.toDocument(), opt);
  }


  @Override
  protected MongoFileAttr cloneBaseName(MongoFileAttr a) {
    String basename = Path.me.basename(a.path);
    MongoFileAttr fs = new MongoFileAttr(
            basename, a.type, a.lastModify, a.content_id);
    if (fs.dir_contain != null)
      fs.dir_contain.addAll(a.dir_contain);
    return fs;
  }


  @Override
  protected void removeAndUpdateParent(MongoFileAttr fs) {
    struct.deleteOne(fs.toFilterID());
    String parentPath = parentPath(fs);
    if (parentPath != null) {
      MongoFileAttr parent = readAttribute(parentPath);
      parent.removeChild(fs.path);
      saveNode(parent);
    }
  }
}
