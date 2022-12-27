package io.mybatis.rui.template.database;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class TableRule extends Rule {
  /**
   * 目录
   */
  private String           catalog;
  /**
   * 模式，数据库名
   */
  private String           schema;
  /**
   * 正则排除列
   */
  private String           ignoreColumnsByRegex;
  /**
   * 列名规则
   */
  private List<ColumnRule> columnRules;

  public TableRule() {
  }

  public TableRule(@NonNull String name) {
    super(name);
  }

  /**
   * 是否排除列
   *
   * @param columnName 列名
   * @return
   */
  public boolean ignoreColumn(@NonNull String columnName) {
    if (StrUtil.isNotEmpty(ignoreColumnsByRegex) && columnName.matches(ignoreColumnsByRegex)) {
      return true;
    }
    if (CollectionUtil.isNotEmpty(columnRules)) {
      return columnRules.stream().filter(c -> columnName.equals(c.getName()) && c.isIgnore()).count() > 0;
    }
    return false;
  }

  /**
   * 获取替换后的名称
   *
   * @param columnName 列名
   * @return
   */
  public String getColumnReplaceName(String columnName) {
    if (CollectionUtil.isNotEmpty(columnRules)) {
      Optional<ColumnRule> any = columnRules.stream().filter(c -> columnName.equals(c.getName())).findAny();
      if (any.isPresent()) {
        return any.get().getReplaceName(columnName);
      }
    }
    return columnName;
  }

  /**
   * 获取表的完整名称
   *
   * @param tableName 表名
   * @return
   */
  public String getFullTableName(String tableName) {
    StringBuilder nameBuilder = new StringBuilder(tableName.length() * 4);
    if (StrUtil.isNotEmpty(catalog)) {
      nameBuilder.append(catalog).append(".");
    }
    if (StrUtil.isNotEmpty(schema)) {
      nameBuilder.append(schema).append(".");
    }
    nameBuilder.append(tableName);
    return nameBuilder.toString();
  }

  /**
   * 获取表的简单名称
   *
   * @param fullTableName 表名
   * @return
   */
  public String getSimpleTableName(String fullTableName) {
    int lastIndexOf = fullTableName.lastIndexOf('.');
    if (lastIndexOf > -1) {
      return fullTableName.substring(lastIndexOf + 1);
    }
    return fullTableName;
  }
}
