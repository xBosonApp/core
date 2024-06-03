////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-4 上午10:22
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/basic/AbsFileSystemUtil.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.fs.basic;

import com.xboson.been.XBosonException;
import com.xboson.log.Log;
import com.xboson.log.LogFactory;
import com.xboson.script.lib.Path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.xboson.fs.basic.IFileAttribute.T_DIR;
import static com.xboson.fs.basic.IFileAttribute.T_FILE;


/**
 * 文件系统基础工具类, 该类并不直接实现文件系统函数, 而是提供便捷方法, 和框架实现.
 */
public abstract class AbsFileSystemUtil<ATTR extends IFileAttribute>
        implements IFileOperatorBase<ATTR> {

  protected final Log log;


  protected AbsFileSystemUtil() {
    log = LogFactory.create();
  }


  /**
   * 标准化路径格式, 去掉连续的 '////' 符号, 将 '/../' 的上层目录移除,
   * 将 '/./' 移除.
   *
   * @param path
   * @return
   */
  public final String normalize(String path) {
    if (path == null)
      return path;

    path = Path.me.normalize(path);
    //
    // 'path.length() > 1' 当目录是根目录则不做改变.
    //
    if (path.length() > 1 && path.charAt(path.length()-1) == '/') {
      path = path.substring(0, path.length()-1);
    }
    return path;
  }


  /**
   * @see Path#dirname(String)
   */
  public final String parentPath(String path) {
    return Path.me.dirname(path);
  }


  public final String parentPath(IFileAttribute a) {
    return parentPath(a.path());
  }


  /**
   * 从 file 的父节点开始创建目录, 这会检查一直到目录根节点之前的路径是否都是目录,
   * 最后创建 file 定义的节点. 在任意一部上失败都会抛出异常.
   * 该方法会直接复制路径上的 struct, 如果是文件没问题, 是目录需要检查子节点问题.
   * 该方法不发送集群消息, 本地文件系统只要收到最深层目录即可自动创建上层目录.
   */
  protected final void makeStructUntilRoot(ATTR file) {
    List<String> need_create_dir = new ArrayList<>();
    ATTR direct_parent = null;

    String pp = parentPath(file);
    while (pp != null) {
      ATTR check_fs = readAttribute(pp);
      if (check_fs == null) {
        //
        // 创建不存在的目录
        //
        need_create_dir.add(pp);
      } else if (check_fs.type() == IFileAttribute.T_FILE) {
        //
        // 不能在文件路径上创建子目录
        //
        throw new XBosonException("Contain files in " + file.path());
      } else if (check_fs.type() == T_DIR) {
        //
        // 当前向搜索到一个目录则不再继续搜索,
        // 这个目录结构的正确由创建该目录的时候保证.
        //
        direct_parent = check_fs;
        break;
      }

      pp = parentPath(pp);
    }

    final int size = need_create_dir.size();
    for (int i = size - 1; i >= 0; --i) {
      String p = need_create_dir.get(i);
      //
      // 更新父节点
      //
      if (direct_parent != null) {
        addTo(direct_parent, p);
        saveNode(direct_parent);
      }

      direct_parent = createDirNode(p);
    }

    if (direct_parent == null && ROOT.equals(file.path()) == false) {
      direct_parent = readAttribute(ROOT);
    }

    if (direct_parent != null) {
      addTo(direct_parent, file.path());
      saveNode(direct_parent);
    }
    saveNode(file);
  }


  /**
   * 移动文件/目录到另一个位置, 目标位置必须是没有对象的, 目标位置必须是目录
   * 源文件和目标文件必须都存在, 当源文件是个目录, 将同步目录中的所有数据结构.
   *
   * @see IFileOperatorBase#move(String, String) move() 方法实现框架
   * @param src 源文件
   * @param to 目标目录
   */
  protected final void moveTo(String src, String to) {
    src = normalize(src);
    to  = normalize(to);
    //
    // 不能移动自己
    //
    if (src.equalsIgnoreCase(to))
      throw new XBosonException.IOError("Sourc And Target is same.");
    //
    // 源文件必须存在
    //
    ATTR srcfs = readAttribute(src);
    if (srcfs == null)
      throw new XBosonException.NotFound(src);
    //
    // 目标文件不能存在
    //
    ATTR tofs = readAttribute(to);
    if (tofs != null)
      throw new XBosonException.IOError(
              "The target directory already exists", to);
    //
    // 存储目标文件的路径上必须是目录, 并且必须存在
    //
    String save_to_dir = Path.me.dirname(to);
    if (save_to_dir == null)
      save_to_dir = "/";

    ATTR save_dir = readAttribute(save_to_dir);

    if (save_dir == null)
      throw new XBosonException.NotFound(save_to_dir);

    if (save_dir.type() != T_DIR)
      throw new XBosonException.IOError(
              "Cannot move to non-directory", save_to_dir);
    //
    // 先创建新节点再删除老节点.
    //
    if (srcfs.type() == T_FILE) {
      moveFile(srcfs, to);
    }
    else if (srcfs.type() == T_DIR) {
      tofs = createDirNode(to);
      makeStructUntilRoot(tofs);
      //
      // 迭代所有子节点
      //
      Set<String> oldSubNodes = containFiles(srcfs);
      for (String oldName : oldSubNodes) {
        String oldPath = src + oldName;
        String newPath = to  + oldName;
        moveTo(oldPath, newPath);
      }
      deleteFile(srcfs.path());
    }
    else {
      throw new XBosonException.IOError(
              "cannot support Unknow file type");
    }
  }


  /**
   * 读取目录内容
   * @see IFileOperatorBase#readDir(String)  readDir 方法实现框架
   */
  protected final Set<ATTR> readDirContain(String path) {
    path = normalize(path);
    ATTR fs = readAttribute(path);

    if (fs == null)
      throw new XBosonException.NotFound(path);

    if (fs.type() != T_DIR)
      throw new XBosonException.IOError("Is not dir: " + path);


    Set<String> names = containFiles(fs);
    Set<ATTR> ret = new HashSet<>(names.size());

    for (String name : names) {
      fs = readAttribute(path + name);
      if (fs != null) {
        ret.add(cloneBaseName(fs));
      } else {
        log.warn("Redis file system may bad, Cannot found:",
                name, ", In dir:", path, ", But recorded.");
      }
    }
    return ret;
  }


  /**
   * 删除文件或目录
   *
   * @see IFileOperatorBase#delete(String) delete 方法实现框架
   */
  protected final void deleteFile(String file) {
    file = normalize(file);
    ATTR fs = readAttribute(file);

    if (fs == null)
      throw new XBosonException.NotFound(file);

    if (fs.type() == T_FILE) {
      removeAndUpdateParent(fs);
    }
    else /* Is dir */ {
      if (containFiles(fs).size() > 0)
        throw new XBosonException.IOError(
                "Can not delete a non-empty directory");

      removeAndUpdateParent(fs);
    }
  }


  /**
   * 该方法在底层上移动文件, 前置检查已经处理完成;
   * 实现需要创建新文件, 将文件内容移动, 删除源文件.
   *
   * @param src 一定是文件类型对象, 方法返回后该文件必须不存在.
   * @param to 目标路径, 一定不存在
   */
  protected abstract void moveFile(ATTR src, String to);


  /**
   * 返回目录中的所有元素 (子目录/文件) 的路径,
   * 路径是相对于 dir 的路径, 有前置 '/' 符号.
   *
   * @param dir 路径
   * @return 路径集合
   */
  protected abstract Set<String> containFiles(ATTR dir);


  /**
   * 创建目录类型节点, 空目录.
   */
  protected abstract ATTR createDirNode(String path);


  /**
   * 将文件/目录作为子节点添加到目录中.
   *
   * @param dir 目标目录
   * @param path 绝对路径, 必须含有目标路径.
   */
  protected abstract void addTo(ATTR dir, String path);


  /**
   * 将修改保存到文件系统.
   */
  protected abstract void saveNode(ATTR a);


  /**
   * 复制属性节点, 返回的属性 path 只有原先的名称部分, 其他属性原样复制.
   * 返回的属性无法用作其他操作, 因为 path 已经不完整.
   */
  protected abstract ATTR cloneBaseName(ATTR a);


  /**
   * 删除节点, 并删除引用该节点的父节点的引用.
   * 实现可能需要删除文件内容.
   */
  protected abstract void removeAndUpdateParent(ATTR fs);
}
