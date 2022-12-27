package io.mybatis.rui.template;

import cn.hutool.core.io.FileUtil;

import java.io.File;

/**
 * 生成文件的读写文件接口
 */
public interface GenFileSystem {

  GenFileSystem DEFAULT = new GenFileSystem() {

    @Override
    public void init(String root) {

    }

    @Override
    public void mkdirs(File file) {
      file.mkdirs();
    }

    @Override
    public void writeStr(String content, File file) {
      FileUtil.writeUtf8String(content, file);
    }

    @Override
    public String readStr(String resource) {
      return FileUtil.readUtf8String(resource);
    }
  };

  /**
   * 初始化根目录
   *
   * @param root
   */
  void init(String root);

  /**
   * 创建目录
   *
   * @param file
   */
  void mkdirs(File file);

  /**
   * 写入文件
   *
   * @param content
   * @param file
   */
  void writeStr(String content, File file);

  /**
   * 读取生成的文件
   *
   * @param resource
   * @return
   */
  String readStr(String resource);
}
