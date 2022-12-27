package io.mybatis.rui.template.database;

import cn.hutool.core.util.StrUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j(topic = "Database")
@EqualsAndHashCode(of = "name")
public class Rule {
  /**
   * 名称, 支持 SQL 模糊，例如 % _
   */
  private @NonNull String name;
  /**
   * 查找内容，支持正则
   */
  private          String search;
  /**
   * 替换内容，支持正则引用，例如 针对 abcd, 配置 search: a(.*), replace: $1，结果为: bcd
   */
  private          String replace;

  public Rule() {
  }

  public Rule(@NonNull String name) {
    this.name = name;
  }

  /**
   * 获取替换后的名称
   *
   * @param name 要匹配的名称
   * @return
   */
  public String getReplaceName(String name) {
    if (StrUtil.isNotEmpty(search)) {
      String replace = StrUtil.isNotEmpty(this.replace) ? this.replace : "";
      String result = name.replaceAll(search, replace);
      log.trace("名称: " + name + " 替换为: " + result);
      return result;
    }
    return name;
  }

}
