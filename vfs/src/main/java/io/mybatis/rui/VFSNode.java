package io.mybatis.rui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 真实文件的虚拟映射
 */
@Getter
public class VFSNode {
  /**
   * 字符串常量：斜杠 {@code "/"}
   */
  public static final String              SLASH = "/";
  /**
   * 文件名
   */
  protected           Path                name;
  /**
   * 文件内容
   */
  protected           byte[]              bytes;
  /**
   * 文件历史 <时间,内容>
   */
  protected           Map<String, byte[]> history;
  /**
   * 文件类型
   */
  protected           Type                type;
  /**
   * 父节点
   */
  protected transient VFSNode             parent;
  /**
   * 下级目录
   */
  protected           List<VFSNode>       files;

  protected VFSNode(Path name, Type type) {
    this.name = name;
    this.type = type;
  }

  /**
   * 获取子目录，可以多级
   *
   * @param relativePath
   * @return
   */
  public VFSNode getVFSNode(Path relativePath) {
    int nameCount = relativePath.getNameCount();
    if (nameCount > 1) {
      Path name = relativePath.getName(0);
      VFSNode vfsNode = getChild(name);
      if (vfsNode != null) {
        return vfsNode.getVFSNode(relativePath.subpath(1, nameCount));
      }
    } else if (nameCount == 1) {
      //已经到最后一级
      return getChild(relativePath);
    }
    return null;
  }

  /**
   * 添加子孙级节点
   *
   * @param node         节点信息
   * @param relativePath 节点相对路径
   */
  protected void addVFSNode(VFSNode node, Path relativePath) {
    int nameCount = relativePath.getNameCount();
    if (nameCount > 1) {
      Path name = relativePath.getName(0);
      VFSNode vfsNode = getChild(name);
      if (vfsNode == null) {
        vfsNode = new VFSNode(name, Type.DIR);
        addChild(vfsNode);
      }
      if (vfsNode.isDirectory()) {
        vfsNode.addVFSNode(node, relativePath.subpath(1, nameCount));
      } else {
        throw new RuntimeException("无法向文件 " + vfsNode.name + " 下添加子文件");
      }
    } else if (nameCount == 1) {
      addChild(node);
    }
  }

  /**
   * 添加直接子级
   *
   * @param child
   */
  private void addChild(VFSNode child) {
    if (CollUtil.isEmpty(this.files)) {
      this.files = new ArrayList<>();
    }
    if (this.files.contains(child)) {
      VFSNode same = getChild(child.name);
      if (same.type != child.type) {
        throw new RuntimeException("已经存在类型为 "
            + same.type + " 的文件，无法添加 " + child.type + " 类型");
      }
    } else {
      this.files.add(child);
      child.parent = this;
    }
  }

  /**
   * 获取直接子目录
   *
   * @param name
   * @return
   */
  private VFSNode getChild(Path name) {
    if (CollUtil.isNotEmpty(files)) {
      for (VFSNode file : files) {
        if (file.name.equals(name)) {
          return file;
        }
      }
    }
    return null;
  }

  /**
   * 读取文件内容
   *
   * @return
   */
  public String read() {
    if (isFile()) {
      return new String(this.bytes, StandardCharsets.UTF_8);
    }
    return null;
  }

  /**
   * 写入文件内容
   *
   * @param bytes
   */
  public void write(byte[] bytes) {
    //如果已经存在内容就记录到历史
    if (ArrayUtil.isNotEmpty(this.bytes)) {
      if (CollUtil.isEmpty(history)) {
        this.history = new LinkedHashMap<>();
      }
      this.history.put(DateUtil.now(), this.bytes);
    }
    this.bytes = bytes;
  }

  /**
   * 删除节点
   */
  public void delete() {
    if (this.parent != null) {
      this.parent.files.remove(this);
      this.parent = null;
      this.files = null;
      this.bytes = null;
      this.history = null;
    } else {
      throw new UnsupportedOperationException("无法删除根目录");
    }
  }

  public boolean isDirectory() {
    return Type.DIR == type;
  }

  public boolean isFile() {
    return Type.FILE == type;
  }

  /**
   * 迭代子文件
   *
   * @param action
   */
  public void forEach(Consumer<VFSNode> action) {
    if (CollUtil.isNotEmpty(files)) {
      files.forEach(action);
    }
  }

  /**
   * 迭代子文件
   *
   * @param action<next, hasNext>
   */
  public void forEach(BiConsumer<VFSNode, Boolean> action) {
    if (CollUtil.isNotEmpty(files)) {
      int size = files.size();
      for (int i = 0; i < size; i++) {
        action.accept(files.get(i), i < size - 1);
      }
    }
  }

  /**
   * 根据相对路径写入文件
   */
  public void syncDisk(String parentPath) {
    //根据当前的路径创建文件
    File file;
    if (StrUtil.isBlank(name.toString())) {
      file = FileUtil.file(parentPath);
    } else {
      file = FileUtil.file(parentPath, name.toString());
    }
    if (isDirectory()) {
      file.mkdir();
    } else if (isFile()) {
      FileUtil.writeBytes(bytes, file);
    }
    //处理子级
    forEach(node -> {
      node.syncDisk(file.getAbsolutePath());
    });
  }

  /**
   * 转换为压缩文件
   *
   * @param zos
   * @param parentPath
   */
  protected void toZip(ZipOutputStream zos, String parentPath) {
    forEach(node -> {
      try {
        String path = StrUtil.isNotEmpty(parentPath) ?
            (parentPath + node.name) : node.name.toString();
        if (node.isDirectory()) {
          path = path + SLASH;
          zos.putNextEntry(new ZipEntry(path));
          zos.closeEntry();
          //递归子节点
          node.toZip(zos, path);
        } else {
          zos.putNextEntry(new ZipEntry(path));
          IoUtil.copy(new ByteArrayInputStream(node.bytes), zos);
          zos.closeEntry();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * 输出目录结构
   *
   * @param buffer
   * @param prefix
   * @param childrenPrefix
   */
  protected void print(StringBuilder buffer, String prefix, String childrenPrefix) {
    buffer.append(prefix);
    buffer.append(name);
    buffer.append('\n');
    forEach((next, hasNext) -> {
      if (hasNext) {
        next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
      } else {
        next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
      }
    });
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VFSNode vfsNode = (VFSNode) o;
    return name.equals(vfsNode.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return name.toString();
  }

  public enum Type {
    /**
     * 目录
     */
    DIR,
    /**
     * 内容
     */
    FILE
  }
}
