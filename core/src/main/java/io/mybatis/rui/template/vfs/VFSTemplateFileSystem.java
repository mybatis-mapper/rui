package io.mybatis.rui.template.vfs;

import io.mybatis.rui.VFS;
import io.mybatis.rui.template.TemplateFileSystem;

/**
 * 虚拟文件读取系统
 */
public class VFSTemplateFileSystem implements TemplateFileSystem {
  private final VFS vfs;

  public VFSTemplateFileSystem(VFS vfs) {
    this.vfs = vfs;
  }

  @Override
  public void init(String root) {

  }

  @Override
  public String readStr(String resource) {
    return vfs.read(resource);
  }

  public VFS getVfs() {
    return vfs;
  }
}
