package io.mybatis.rui.model;

import lombok.Getter;

/**
 * 特殊文本的处理，目前用于表和字段注释，当出现换行时默认会替换换行为单行，想使用默认值时使用 .o
 */
@Getter
public class Text {
  /**
   * 原始值
   */
  private String o;
  /**
   * 默认展示值
   */
  private String val;

  public Text(String text) {
    this.o = text != null ? text : "";
    this.val = this.o.replaceAll("[\\r\\n]", "");
  }

  @Override
  public String toString() {
    return val;
  }
}
