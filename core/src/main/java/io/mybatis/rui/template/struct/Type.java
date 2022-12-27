package io.mybatis.rui.template.struct;

public enum Type {
  /**
   * 目录
   */
  DIR,
  /**
   * 包
   */
  PACKAGE,
  /**
   * 模板，经过模板赋值处理，当 structure 只有 file 没有 name 时，生成的文件名使用 file 名，当同时存在时，name为文件名，file为内容
   * <p>
   * 模板优先级高于静态内容
   */
  TEMPLATE,
  /**
   * 静态内容，不经过模板处理，当 structure 只有 name 没有 file 时认为是 STATIC，找不到文件就是空文件
   */
  STATIC
}
