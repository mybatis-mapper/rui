package io.mybatis.rui.template;

import cn.hutool.core.io.FileUtil;

/**
 * 模板文件的读文件接口
 */
public interface TemplateFileSystem {
  TemplateFileSystem DEFAULT = new TemplateFileSystem() {
    String root;

    @Override
    public void init(String root) {
      this.root = root;
    }

    @Override
    public String readStr(String resource) {
      return FileUtil.readUtf8String(FileUtil.file(root, resource));
    }
  };

  /**
   * 初始化根目录
   *
   * @param root
   */
  void init(String root);

  /**
   * 读取资源文件
   *
   * @param resource
   * @return
   */
  String readStr(String resource);
}
