package io.mybatis.rui.template.vfs;

import io.mybatis.rui.VFS;
import io.mybatis.rui.template.GenFileSystem;

import java.io.File;

/**
 * 虚拟文件写入系统
 */
public class VFSGenFileSystem implements GenFileSystem {
  private VFS vfs;

  @Override
  public void init(String root) {
    this.vfs = VFS.of(root);
  }

  @Override
  public void mkdirs(File file) {
    vfs.mkdirs(file);
  }

  @Override
  public void writeStr(String content, File file) {
    vfs.write(file, content);
  }

  @Override
  public String readStr(String resource) {
    return vfs.read(resource);
  }

  public VFS getVfs() {
    return vfs;
  }
}
