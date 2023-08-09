package io.mybatis.rui;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class VFS extends VFSNode {
  private Path path;
  private File file;

  private VFS(Path path) {
    super(path.getFileName() != null ? path.getFileName() : path, Type.DIR);
    this.path = path;
  }

  /**
   * 创建VFS
   *
   * @param file 根路径
   * @return
   */
  public static VFS of(File file) {
    VFS vfs = new VFS(file.toPath());
    vfs.file = file;
    return vfs;
  }

  /**
   * 创建VFS
   *
   * @param path 根路径
   * @return
   */
  public static VFS of(Path path) {
    return new VFS(path);
  }

  /**
   * 创建VFS
   *
   * @param path 根路径
   * @return
   */
  public static VFS of(String path) {
    return new VFS(toPath(path));
  }

  /**
   * 相对路径转换
   *
   * @param relativePath 相对路径
   * @return
   */
  public static Path toPath(String relativePath) {
    return Paths.get(relativePath);
  }

  /**
   * 加载指定文件到VFS
   *
   * @param file 文件
   * @return
   */
  public static VFS load(File file) {
    if (isZip(file)) {
      return loadZip(file);
    } else if (file.isDirectory()) {
      return loadFolder(file);
    } else {
      throw new IllegalArgumentException("VFS 加载支持目录和 zip 压缩文件，不支持其他类型文件的加载");
    }
  }

  /**
   * 加载文件夹（目录）
   *
   * @param folder 文件夹（目录）
   * @return
   */
  private static VFS loadFolder(File folder) {
    VFS vfs = new VFS(folder.toPath());
    vfs.file = folder;
    //递归加载所有子文件
    if (folder.exists() && folder.isDirectory()) {
      loadFolderFiles(vfs, folder.listFiles());
    }
    return vfs;
  }

  /**
   * 添加子文件
   *
   * @param vfs
   * @param files
   */
  private static void loadFolderFiles(VFS vfs, File[] files) {
    for (File file : files) {
      if (file.isFile()) {
        vfs.write(file, FileUtil.readBytes(file));
      } else {
        vfs.mkdirs(file);
        loadFolderFiles(vfs, file.listFiles());
      }
    }
  }

  /**
   * 加载zip文件到VFS
   *
   * @param file zip文件
   * @return
   */
  private static VFS loadZip(File file) {
    try (ZipFile zipFile = new ZipFile(file)) {
      VFS vfs = VFS.of("");
      vfs.file = file;
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        loadZipEntry(vfs, zipFile, entries.nextElement());
      }
      return vfs;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 添加zipEntry
   *
   * @param vfs
   * @param zipEntry
   */
  private static void loadZipEntry(VFS vfs, ZipFile zipFile, ZipEntry zipEntry) {
    if (zipEntry.isDirectory()) {
      vfs.mkdirs(zipEntry.getName());
    } else {
      try (InputStream inputStream = zipFile.getInputStream(zipEntry);) {
        vfs.write(zipEntry.getName(), IoUtil.readBytes(inputStream));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * 是否为压缩包文件
   *
   * @param file
   * @return
   */
  private static boolean isZip(File file) {
    return !file.isDirectory() && file.getName().toLowerCase().endsWith(".zip");
  }

  /**
   * 相对路径
   *
   * @param file 文件
   * @return
   */
  public Path relativize(File file) {
    return path.relativize(file.toPath());
  }

  /**
   * 检查相对路径
   *
   * @param relativePath 相对路径
   */
  private void checkRelativePath(Path relativePath) {
    if (relativePath.getNameCount() > 0) {
      if (relativePath.getName(0).toString().equals("..")) {
        throw new RuntimeException(relativePath + " 超出当前虚拟文件系统的范围");
      }
    }
  }

  /**
   * 返回文件的类型
   *
   * @param file 文件
   * @return
   */
  public Type fileType(File file) {
    return file.isDirectory() ? Type.DIR : Type.FILE;
  }

  /**
   * 查找节点
   *
   * @param relativePath 相对路径
   * @return
   */
  protected VFSNode findVFSNode(Path relativePath) {
    return findVFSNode(relativePath, null, false);
  }

  /**
   * 查找指定类型节点
   *
   * @param relativePath 相对路径
   * @param type         文件类型
   * @return
   */
  protected VFSNode findVFSNode(Path relativePath, Type type) {
    return findVFSNode(relativePath, type, false);
  }

  /**
   * 查找节点
   *
   * @param relativePath      相对路径
   * @param type              文件类型
   * @param createIfNotExists 如果不存在就创建
   * @return
   */
  protected VFSNode findVFSNode(Path relativePath, Type type, boolean createIfNotExists) {
    checkRelativePath(relativePath);
    VFSNode node = getVFSNode(relativePath);
    if (node != null && type != null && node.type != type) {
      if (createIfNotExists) {
        throw new RuntimeException("已经存在类型为 " + node.type
            + " 的文件，无法创建 " + type + "类型的同名文件");
      }
      return null;
    }
    if (node == null && createIfNotExists) {
      node = new VFSNode(relativePath.getFileName(), type);
      addVFSNode(node, relativePath);
    }
    return node;
  }

  /**
   * 文件是否存在
   *
   * @param file 文件
   * @return true 存在，false 不存在
   */
  public boolean exists(File file) {
    return exists(relativize(file));
  }

  /**
   * 文件是否存在
   *
   * @param relativePath 相对路径
   * @return true 存在，false 不存在
   */
  public boolean exists(String relativePath) {
    return exists(toPath(relativePath));
  }

  /**
   * 文件是否存在
   *
   * @param relativePath 相对路径
   * @return true 存在，false 不存在
   */
  private boolean exists(Path relativePath) {
    return getVFSNode(relativePath) != null;
  }

  /**
   * 读取文件内容
   *
   * @param file 文件
   * @return 文件内容
   */
  public String read(File file) {
    return read(relativize(file));
  }

  /**
   * 读取文件内容
   *
   * @param relativePath 相对路径
   * @return 文件内容
   */
  public String read(String relativePath) {
    return read(toPath(relativePath));
  }

  /**
   * 读取相对路径的文件内容
   *
   * @param relativePath 相对路径
   * @return 文件内容
   */
  public String read(Path relativePath) {
    VFSNode vfsNode = findVFSNode(relativePath, Type.FILE);
    return vfsNode != null ? vfsNode.read() : null;
  }

  /**
   * 写入文件内容
   *
   * @param file  文件
   * @param bytes 内容
   */
  public void write(File file, byte[] bytes) {
    write(relativize(file), bytes);
  }

  /**
   * 写入文件内容
   *
   * @param relativePath 相对路径
   * @param bytes        内容
   */
  public void write(String relativePath, byte[] bytes) {
    write(toPath(relativePath), bytes);
  }

  /**
   * 写入相对文件内容
   *
   * @param relativePath 相对路径
   * @param bytes        文件内容
   */
  public void write(Path relativePath, byte[] bytes) {
    findVFSNode(relativePath, Type.FILE, true).write(bytes);
  }

  /**
   * 写入文件内容
   *
   * @param file    文件
   * @param content 内容
   */
  public void write(File file, String content) {
    write(relativize(file), content);
  }

  /**
   * 写入文件内容
   *
   * @param relativePath 相对路径
   * @param content      内容
   */
  public void write(String relativePath, String content) {
    write(toPath(relativePath), content);
  }

  /**
   * 写入相对文件内容
   *
   * @param relativePath 相对路径
   * @param content      文件内容
   */
  public void write(Path relativePath, String content) {
    findVFSNode(relativePath, Type.FILE, true).write(content.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 创建指定目录
   *
   * @param file 文件
   */
  public void mkdirs(File file) {
    mkdirs(relativize(file));
  }

  /**
   * 创建指定目录
   *
   * @param relativePath 相对路径
   */
  public void mkdirs(String relativePath) {
    mkdirs(toPath(relativePath));
  }

  /**
   * 创建相对目录
   *
   * @param relativePath 相对路径
   */
  public void mkdirs(Path relativePath) {
    checkRelativePath(relativePath);
    addVFSNode(new VFSNode(relativePath.getFileName(), Type.DIR), relativePath);
  }

  /**
   * 删除指定文件
   *
   * @param file 文件
   * @return true 删除成功，false 文件不存在
   */
  public boolean delelte(File file) {
    return delelte(relativize(file));
  }

  /**
   * 删除指定文件
   *
   * @param relativePath 相对路径
   * @return true 删除成功，false 文件不存在
   */
  public boolean delelte(String relativePath) {
    return delelte(toPath(relativePath));
  }

  /**
   * 删除相对路径的文件
   *
   * @param relativePath 相对路径
   * @return true 删除成功，false 文件不存在
   */
  public boolean delelte(Path relativePath) {
    VFSNode vfsNode = findVFSNode(relativePath);
    if (vfsNode != null) {
      vfsNode.delete();
      return true;
    }
    return false;
  }

  /**
   * 输出项目结构
   *
   * @return 项目结构
   */
  public String print() {
    StringBuilder print = new StringBuilder();
    print(print, "", "");
    return print.toString();
  }

  /**
   * 写入磁盘
   */
  public void syncDisk() {
    //当前 vfs.name 也会生成目录，所以需要查找 parent 去生成
    if (file != null) {
      syncDisk(file);
    } else if (path.isAbsolute()) {
      syncDisk(path.getParent().toFile());
    } else {
      throw new RuntimeException("VFS的根路径path[ " + path + " ]为相对路径，不存在对应的物理文件，无法通过当前方法写入磁盘");
    }
  }

  /**
   * 写入到新的位置
   *
   * @param file 支持目录和压缩包
   */
  public void syncDisk(File file) {
    if (isZip(file)) {
      syncZip(file);
    } else {
      file.mkdirs();
      syncDisk(file.getAbsolutePath());
    }
  }

  /**
   * 存储为压缩包
   *
   * @param zip 压缩包
   */
  private void syncZip(File zip) {
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
      toZip(zos, "");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
