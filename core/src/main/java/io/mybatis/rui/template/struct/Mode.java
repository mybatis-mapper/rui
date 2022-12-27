package io.mybatis.rui.template.struct;

/**
 * 写入方式
 */
public enum Mode {
  /**
   * 一次，存在就不生成
   */
  ONCE,
  /**
   * 覆盖
   */
  OVERRIDE,
  /**
   * 合并
   */
  MERGE
}
